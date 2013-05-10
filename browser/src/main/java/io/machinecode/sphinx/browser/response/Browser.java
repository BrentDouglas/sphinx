package io.machinecode.sphinx.browser.response;

import io.machinecode.sphinx.browser.http.Parameters;

import java.util.Map;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Browser extends Base implements Parameters {
    private String device;
    private String osVersion;
    private String os;
    private String browserVersion;
    private String browser;

    public String getDevice() {
        return device;
    }

    public void setDevice(final String device) {
        this.device = device;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(final String os) {
        this.os = os;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(final String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(final String browser) {
        this.browser = browser;
    }

    @Override
    public Map<String, String> parameters() {
        return (Map<String, String>) mapper.convertValue(this, Map.class);
    }
}
