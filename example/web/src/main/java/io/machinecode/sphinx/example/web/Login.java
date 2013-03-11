package io.machinecode.sphinx.example.web;

import io.machinecode.sphinx.example.core.response.FacebookResponse;
import io.machinecode.sphinx.example.core.response.TwitterResponse;

import javax.inject.Inject;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Path("login")
public class Login {

    @Inject
    private Session session;

    @POST
    @Path("/facebook")
    public Response facebook(final FacebookResponse facebookResponse) {
        try {
            return Response.ok().build();
        } catch (final Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/twitter")
    public Response facebook(final TwitterResponse twitterResponse) {
        try {
            return Response.ok().build();
        } catch (final Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/local")
    public Response post(@FormParam("username") final String email,
                         @FormParam("password") final String password,
                         @Context final HttpServletRequest request) {
        try {
            return Response.ok().build();
        } catch (final Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
