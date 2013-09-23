package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PropertyUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class DriverConfig {

    @XmlElement(name = "driver-class", namespace = NAMESPACE, required = true)
    private String driverClass;

    @XmlElement(name = "path-to-driver-jar", namespace = NAMESPACE, required = true)
    private String pathToDriverJar;

    public void validate() {
        driverClass = PropertyUtil.resolve(driverClass);
        pathToDriverJar = PropertyUtil.resolve(pathToDriverJar);
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
