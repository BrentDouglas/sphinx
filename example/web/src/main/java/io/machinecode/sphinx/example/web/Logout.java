package io.machinecode.sphinx.example.web;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
@Path("logout")
public class Logout {

    @POST
    @Path("/")
    @Produces(APPLICATION_JSON)
    public Response logout() {
        return Response.ok().build();
    }
}
