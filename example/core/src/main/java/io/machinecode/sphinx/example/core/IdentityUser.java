package io.machinecode.sphinx.example.core;

import io.machinecode.sphinx.example.core.model.User;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class IdentityUser implements org.picketlink.idm.api.User {

    public static final IdentityUser INVALID_USER = new IdentityUser(null, null);

    private final User user;
    private final String id;

    public IdentityUser(final User user) {
        this(user, Integer.toString(user.getId()));
    }

    private IdentityUser(final User user, final String string) {
        this.user = user;
        this.id = string;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getKey() {
        return id;
    }

    public User getUser() {
        return user;
    }
}
