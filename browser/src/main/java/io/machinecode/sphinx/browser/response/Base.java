package io.machinecode.sphinx.browser.response;

import io.machinecode.sphinx.browser.BrowserStack;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Base {
    protected ObjectMapper mapper;
    protected BrowserStack browserStack;

    // Only public so BrowserStack can set them

    public Base setMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public Base setBrowserStack(final BrowserStack browserStack) {
        this.browserStack = browserStack;
        return this;
    }
}
