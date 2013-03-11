package io.machinecode.sphinx.example.core.response;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import java.io.Serializable;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@RequestScoped
@Alternative
public class Credentials implements Serializable {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
