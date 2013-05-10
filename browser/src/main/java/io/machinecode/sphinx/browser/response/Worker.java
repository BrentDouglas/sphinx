package io.machinecode.sphinx.browser.response;

import io.machinecode.sphinx.browser.http.Http;

import java.io.IOException;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Worker extends Base {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public WorkerStatus status() throws IOException {
        return browserStack.run(mapper, browserStack.request(Http.GET, "worker/" + id), WorkerStatus.class);
    }

    public void delete() throws IOException {
        browserStack.run(mapper, browserStack.request(Http.DELETE, "worker/" + id), null);
    }
}
