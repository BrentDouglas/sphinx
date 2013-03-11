package io.machinecode.sphinx.example.web.security;

import org.picketbox.core.authorization.impl.SimpleAuthorizationManager;
import org.picketbox.http.authentication.HTTPFormAuthentication;
import org.picketbox.http.config.ConfigurationBuilderProvider;
import org.picketbox.http.config.HTTPConfigurationBuilder;

import javax.servlet.ServletContext;

import static org.picketbox.http.resource.ProtectedResourceConstraint.NOT_PROTECTED;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Configuration implements ConfigurationBuilderProvider {

    @Override
    public HTTPConfigurationBuilder getBuilder(final ServletContext servletcontext) {
        final HTTPConfigurationBuilder builder = new HTTPConfigurationBuilder();

        builder.identityManager()
                .jpaStore();

        builder.sessionManager()
                .fileSessionStore();

        builder.authorization()
                .manager(new SimpleAuthorizationManager());

        builder.authentication()
                .mechanism(new HTTPFormAuthentication());

        builder.protectedResource()
                .resource("/index.html", NOT_PROTECTED)
                .resource("/login.html", NOT_PROTECTED)
                .resource("/error.html", NOT_PROTECTED)
                .resource("/register.html", NOT_PROTECTED)
                .resource("/rest/login/*", NOT_PROTECTED);

        return builder;
    }
}
