 package Noteboard.controller;

import Noteboard.model.ApplicationUser;
import Noteboard.service.AuthService;
import Noteboard.service.ApplicationUserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import java.util.List;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationUserController {

    @Inject
    ApplicationUserService userService;

    @Inject
    AuthService authService;

    @GET
    @RolesAllowed({ "admin" })
    public List<ApplicationUser> getAllUsers() {
        return userService.findAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response getUser(@PathParam("id") Long id) {
        ApplicationUser user = userService.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response updateUser(@PathParam("id") Long id, ApplicationUser user) {
        ApplicationUser updated = userService.updateUser(id, user);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "admin" })
    public Response deleteUser(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/register")
    public Response createUser(ApplicationUser user) {
        ApplicationUser created = userService.createUser(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        String token = authService.authenticate(request.username, request.password);
        return Response.ok(new TokenResponse(token)).build();
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class TokenResponse {
        public String token;

        public TokenResponse() {
        }

        public TokenResponse(String token) {
            this.token = token;
        }
    }
}
