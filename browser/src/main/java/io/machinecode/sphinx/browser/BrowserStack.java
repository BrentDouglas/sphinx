package io.machinecode.sphinx.browser;

import io.machinecode.sphinx.browser.http.Http;
import io.machinecode.sphinx.browser.http.Request;
import io.machinecode.sphinx.browser.http.Response;
import io.machinecode.sphinx.browser.response.Base;
import io.machinecode.sphinx.browser.response.Browser;
import io.machinecode.sphinx.browser.response.Browsers;
import io.machinecode.sphinx.browser.response.Failure;
import io.machinecode.sphinx.browser.response.Status;
import io.machinecode.sphinx.browser.response.Worker;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;

import java.io.IOException;
import java.util.Map;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class BrowserStack {

    static final String URL = "http://api.browserstack.com/3/";

    private final ObjectMapper mapper;
    private final String credentials;

    public BrowserStack(final String username, final String password) {
        this.credentials = "Basic " + new Base64(true).encodeToString((username + ":" + password).getBytes());
        this.mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    public Browsers browsers() throws IOException {
        return run(mapper, request(Http.GET, "browsers?flat=true"), Browsers.class);
    }

    public Worker worker(final Browser browser, final int timeout, final String url) throws IOException {
        final Request request = request(Http.POST, "worker", browser.parameters());
        if (timeout >= 3600) {
            request.data("timeout", Integer.toString(3600));
        } else if (timeout > 0) {
            request.data("timeout", Integer.toString(timeout));
        }
        request.data("url", url);
        return run(mapper, request, Worker.class);
    }

    public Worker worker(final Browser browser, final String url) throws IOException {
        return worker(browser, -1, url);
    }

    public Status status() throws IOException {
        return run(mapper, request(Http.GET, "status"), Status.class);
    }

    public <T extends Base> Request request(final String method, final String url, final Map<String, String> parameters) throws IOException {
        return Http.request(method, URL + url)
                .data(parameters)
                .cookie("Authorization", credentials);
    }

    public <T extends Base> Request request(final String method, final String url) throws IOException {
        return Http.request(method, URL + url)
                .cookie("Authorization", credentials);
    }

    public <T extends Base> T run(final ObjectMapper mapper, final Request request, final Class<T> clazz) throws IOException {
        final Response response = request
                .send();
        if (response.code() == 200) {
            if (clazz == null) {
                return null;
            }
            final T that = mapper.readValue(response.data(), clazz);
            that.setMapper(mapper)
                    .setBrowserStack(this);
            return that;
        } else if (response.code() == 422) {
            throw new ResponseException(mapper.readValue(response.data(), Failure.class));
        } else {
            throw new ResponseException(response.code());
        }
    }
}
