package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PortUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class CdiConfig {

    @XmlElement(name = "bind-address", namespace = NAMESPACE, required = false)
    private String bindAddress;

    @XmlElement(name = "port", namespace = NAMESPACE, required = false)
    private Integer port;

    public void validate() {
        if (port == null) {
            port = PortUtil.firstAvailableTcp();
        }
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
