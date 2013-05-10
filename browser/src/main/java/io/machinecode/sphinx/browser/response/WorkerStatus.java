package io.machinecode.sphinx.browser.response;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class WorkerStatus extends Base {
    private String status;
    private String browser;
    private String browserVersion;
    private String os;
    private String osVersion;

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(final String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(final String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(final String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(final String osVersion) {
        this.osVersion = osVersion;
    }
}
