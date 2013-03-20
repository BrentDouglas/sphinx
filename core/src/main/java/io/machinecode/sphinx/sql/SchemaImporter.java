package io.machinecode.sphinx.sql;

import io.machinecode.sphinx.config.DatabaseConfig;
import io.machinecode.sphinx.config.DriverConfig;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class SchemaImporter {

    private static final Logger log = Logger.getLogger(SchemaImporter.class);

    public static void importSchema(final DatabaseConfig config) throws Exception {
        Connection connection = null;
        try {
            final DriverConfig driverConfig = config.getDriver();
            if (driverConfig != null) {
                final Driver driver = DelegateDriver.from(driverConfig);
                connection = driver.connect(config.getJdbcConnection(), new Properties());
            } else {
                connection = DriverManager.getConnection(config.getJdbcConnection());
            }

            final FileReader reader = new FileReader(new File(config.getPathToSchema()));

            final char[] buffer = new char[1024];
            StringBuilder builder = new StringBuilder();
            int read;
            while ((read = reader.read(buffer)) != -1) {
                int last = 0;
                for (int i = 0; i < read; ++i) {
                    if (buffer[i] == ';') {
                        builder.append(buffer, last, i - last + 1); //Include ;
                        last = i + 1; //Eat ;
                        connection.createStatement().executeUpdate(builder.toString());
                        builder = new StringBuilder();
                    }
                }
                builder.append(buffer, last, read - last);
            }
        } catch (final SQLException e) {
            log.error("Failed connecting to " + config.getJdbcConnection());
            throw e;
        } catch (final IOException e) {
            log.error("Failed reading schema at " + config.getPathToSchema());
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
