package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PathUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class DatabaseConfig {

    @XmlElement(name = "path-to-schema", namespace = SphinxConfig.NAMESPACE, required = true)
    private String pathToSchema;

    @XmlElement(name = "jdbc-connection-string", namespace = SphinxConfig.NAMESPACE, required = true)
    private String jdbcConnection;

    @XmlElement(name = "driver", namespace = SphinxConfig.NAMESPACE, required = false)
    private DriverConfig driver;

    public void validate() {
        pathToSchema = PathUtil.resolve(pathToSchema);
        if (driver != null) {
            driver.validate();
        }
    }

    public String getPathToSchema() {
        return pathToSchema;
    }

    public void setPathToSchema(final String pathToSchema) {
        this.pathToSchema = pathToSchema;
    }

    public String getJdbcConnection() {
        return jdbcConnection;
    }

    public void setJdbcConnection(final String jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    public DriverConfig getDriver() {
        return driver;
    }

    public void setDriver(final DriverConfig driver) {
        this.driver = driver;
    }
}
