package io.machinecode.sphinx.example.core.response;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public final class FacebookResponse {
    private int id;
    private String username;
    private String name;
    private String email;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
