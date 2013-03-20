package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.DriverConfig;
import io.machinecode.sphinx.config.SphinxConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Singleton
@Startup
public class DatabaseImporter {

    private static final Logger log = Logger.getLogger(DatabaseImporter.class.getCanonicalName());

    public static final String RESOURCE = "sphinx.xml";

    private SphinxConfig config;

    @PostConstruct
    private void create() throws Exception {
        config = SphinxConfig.getConfig(getClass().getClassLoader().getResourceAsStream(RESOURCE));
        preDeployment(config.getDatabases());
    }

    @PreDestroy
    private void destroy() throws Exception {
        postDeployment(config.getDatabases());
    }

    private static void preDeployment(final List<DatabaseConfig> configs) throws Exception {
        if (configs == null) {
            return;
        }
        for (final DatabaseConfig config : configs) {
            if (config.isRunInContainer()) {
                runScript(config, config.getPreDeployment());
            }
        }
    }

    private static void postDeployment(final List<DatabaseConfig> configs) throws Exception {
        if (configs == null) {
            return;
        }
        for (final DatabaseConfig config : configs) {
            if (config.isRunInContainer()) {
                runScript(config, config.getPostDeployment());
            }
        }
    }

    public static void runScript(final DatabaseConfig config, final String filename) throws Exception {
            Connection connection = null;
            try {
                final DriverConfig driverConfig = config.getDriver();
                if (driverConfig != null) {
                    final Driver driver = DelegateDriver.from(driverConfig);
                    connection = driver.connect(config.getJdbcConnection(), new Properties());
                } else {
                    connection = DriverManager.getConnection(config.getJdbcConnection());
                }

                final BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    final int index = line.indexOf("--"); //Comments
                    final String content = index == -1
                            ? line
                            : line.substring(0, index);
                    int last = 0;
                    for (int i = 0; i < content.length(); ++i) {
                        if (content.charAt(i) == ';') {
                            builder.append(content.substring(last, i + 1)); //Include ;
                            last = i + 1; //Eat ;
                            connection.createStatement().executeUpdate(builder.toString());
                            builder = new StringBuilder();
                        }
                    }
                    builder.append(content.substring(last));
                }
            } catch (final SQLException e) {
                log.severe("Failed connecting to " + config.getJdbcConnection());
                throw e;
            } catch (final IOException e) {
                log.severe("Failed reading schema at " + filename);
                throw e;
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (final SQLException e) {
                        //
                    }
                }
            }
    }
}
