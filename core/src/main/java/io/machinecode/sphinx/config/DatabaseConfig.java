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

    @XmlElement(name = "run-in-container", namespace = SphinxConfig.NAMESPACE, required = false)
    private boolean runInContainer;

    @XmlElement(name = "pre-deployment", namespace = SphinxConfig.NAMESPACE, required = false)
    private String preDeployment;

    @XmlElement(name = "post-deployment", namespace = SphinxConfig.NAMESPACE, required = false)
    private String postDeployment;

    @XmlElement(name = "jdbc-connection-string", namespace = SphinxConfig.NAMESPACE, required = true)
    private String jdbcConnection;

    @XmlElement(name = "driver", namespace = SphinxConfig.NAMESPACE, required = false)
    private DriverConfig driver;

    public void validate() {
        preDeployment = PathUtil.resolve(preDeployment);
        postDeployment = PathUtil.resolve(postDeployment);
        if (driver != null) {
            driver.validate();
        }
    }

    public boolean isRunInContainer() {
        return runInContainer;
    }

    public void setRunInContainer(final boolean runInContainer) {
        this.runInContainer = runInContainer;
    }

    public String getPreDeployment() {
        return preDeployment;
    }

    public void setPreDeployment(final String preDeployment) {
        this.preDeployment = preDeployment;
    }

    public String getPostDeployment() {
        return postDeployment;
    }

    public void setPostDeployment(final String postDeployment) {
        this.postDeployment = postDeployment;
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
