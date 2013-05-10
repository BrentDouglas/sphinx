package io.machinecode.sphinx.browser.http;

import java.util.Map;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Request {

    Request cookie(final String name, final String value);

    Request data(final String name, final String value);

    Request data(final Map<String, String> data);

    Response send();
}
