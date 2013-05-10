package io.machinecode.sphinx.browser.response;

import java.util.List;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Browsers extends Base {
    private List<Browser> browsers;

    public List<Browser> getBrowsers() {
        return browsers;
    }

    public void setBrowsers(final List<Browser> browsers) {
        this.browsers = browsers;
    }
}
