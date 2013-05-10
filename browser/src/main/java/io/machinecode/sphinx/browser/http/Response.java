package io.machinecode.sphinx.browser.http;

import java.io.InputStream;
import java.util.List;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public interface Response {

    int code();

    InputStream data();

    List<String> header(final String header);
}
