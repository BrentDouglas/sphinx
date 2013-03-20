package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PathUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class DriverConfig {

    @XmlElement(name = "driver-class", namespace = SphinxConfig.NAMESPACE, required = true)
    private String driverClass;

    @XmlElement(name = "path-to-driver-jar", namespace = SphinxConfig.NAMESPACE, required = true)
    private String pathToDriverJar;

    public void validate() {
        pathToDriverJar = PathUtil.resolve(pathToDriverJar);
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(final String driverClass) {
        this.driverClass = driverClass;
    }

    public String getPathToDriverJar() {
        return pathToDriverJar;
    }

    public void setPathToDriverJar(final String pathToDriverJar) {
        this.pathToDriverJar = pathToDriverJar;
    }
}
