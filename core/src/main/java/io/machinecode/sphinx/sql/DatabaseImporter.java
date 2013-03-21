package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.DriverConfig;
import io.machinecode.sphinx.config.SphinxConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
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

    public static final String SPHINX_XML = "sphinx.xml";
    public static final String DRIVER_JAR = "driver.jar";
    public static final String PRE_DEPLOY_SQL = "pre-deploy.sql";
    public static final String POST_DEPLOY_SQL = "post-deploy.sql";

    @PostConstruct
    private void create() throws Exception {
        final ClassLoader loader = getClass().getClassLoader();
        final SphinxConfig config = SphinxConfig.getConfig(loader.getResourceAsStream(SPHINX_XML));
        preDeployment(config.getDatabases(), loader);
    }

    @PreDestroy
    private void destroy() {
        final ClassLoader loader = getClass().getClassLoader();
        final SphinxConfig config = SphinxConfig.getConfig(loader.getResourceAsStream(SPHINX_XML));
        postDeployment(config.getDatabases(), loader);
    }

    private static void preDeployment(final List<DatabaseConfig> configs, final ClassLoader loader) throws Exception {
        if (configs == null) {
            return;
        }
        for (final DatabaseConfig config : configs) {
            if (config.isRunInContainer()) {
                final String pre = config.getPreDeployment();
                final Reader reader = new InputStreamReader(loader.getResourceAsStream("/" + config.getId() + "/" + PRE_DEPLOY_SQL));
                runScript(config, reader, new DriverProducer() {
                    @Override
                    public Driver produce() throws Exception {
                        return DelegateDriver.from(
                                config.getDriver().getDriverClass(),
                                loader.getResource("/" + config.getId() + "/" + DRIVER_JAR)
                        );
                    }
                });
            }
        }
    }

    private static void postDeployment(final List<DatabaseConfig> configs, final ClassLoader loader) {
        if (configs == null) {
            return;
        }
        for (final DatabaseConfig config : configs) {
            if (config.isRunInContainer()) {
                try {
                    final String post = config.getPostDeployment();
                    final Reader reader = new InputStreamReader(loader.getResourceAsStream("/" + config.getId() + "/" + POST_DEPLOY_SQL));
                    runScript(config, reader, new DriverProducer() {
                        @Override
                        public Driver produce() throws Exception {
                            return DelegateDriver.from(
                                    config.getDriver().getDriverClass(),
                                    loader.getResource("/" + config.getId() + "/" + DRIVER_JAR)
                            );
                        }
                    });
                } catch (final Exception e) {
                    log.severe("Failed running post deployment sql script " + config.getPostDeployment() + ": " + e.getMessage());
                }
            }
        }
    }

    public static void runScript(final DatabaseConfig config, final Reader reader, final DriverProducer producer) throws Exception {
        Connection connection = null;
        try {
            final DriverConfig driverConfig = config.getDriver();
            if (driverConfig != null) {
                final Driver driver = producer.produce();
                connection = driver.connect(config.getJdbcConnection(), new Properties());
            } else {
                connection = DriverManager.getConnection(config.getJdbcConnection());
            }

            processScript(connection, new BufferedReader(reader));

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

    private static void processScript(final Connection connection, final BufferedReader reader) throws Exception {
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
    }
}
