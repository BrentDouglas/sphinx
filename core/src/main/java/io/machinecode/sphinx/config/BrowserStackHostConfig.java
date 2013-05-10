package io.machinecode.sphinx.config;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class BrowserStackHostConfig {

    @XmlElement(name = "hostname", namespace = NAMESPACE, required = true)
    private String hostname;

    @XmlElement(name = "port", namespace = NAMESPACE, required = true)
    private int port;

    @XmlElement(name = "ssl", namespace = NAMESPACE, required = true)
    private boolean ssl;

    public void validate() {
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(final boolean ssl) {
        this.ssl = ssl;
    }
}
