package io.machinecode.sphinx.config;

import io.machinecode.sphinx.util.PropertyUtil;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

import static io.machinecode.sphinx.config.SphinxConfig.NAMESPACE;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@XmlAccessorType(FIELD)
public class BrowserStackConfig {

    @XmlElement(name = "tunnel-key", namespace = NAMESPACE, required = true)
    private String tunnelKey;

    @XmlElement(name = "api-key", namespace = NAMESPACE, required = true)
    private String apiKey;

    @XmlElement(name = "host", namespace = NAMESPACE, required = true)
    private List<BrowserStackHostConfig> hosts = new ArrayList<BrowserStackHostConfig>();

    public void validate() {
        tunnelKey = PropertyUtil.resolve(tunnelKey);
        apiKey = PropertyUtil.resolve(apiKey);
    }

    public String getTunnelKey() {
        return tunnelKey;
    }

    public void setTunnelKey(final String tunnelKey) {
        this.tunnelKey = tunnelKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public List<BrowserStackHostConfig> getHosts() {
        return hosts;
    }

    public void setHosts(final List<BrowserStackHostConfig> hosts) {
        this.hosts = hosts;
    }
}
