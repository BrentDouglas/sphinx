package io.machinecode.sphinx.example.core.response;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public final class TwitterResponse {
    private int id;
    private String screenName;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(final String screenName) {
        this.screenName = screenName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
