package Noteboard.controller;

import Noteboard.model.Notebook;
import Noteboard.service.NotebookService;
import jakarta.inject.Inject;
import java.util.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;

@Path("/notebooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotebookController {

    @Inject
    NotebookService notebookService;

    @GET
    public List<Notebook> getAllNotebooks() {
        return notebookService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getNotebook(@PathParam("id") Long id) {
        Notebook notebook = notebookService.findById(id);
        if (notebook == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(notebook).build();
    }

    @POST
    @RolesAllowed({ "user", "admin" })
    public Response createNotebook(Notebook notebook) {
        Notebook created = notebookService.createNotebook(notebook);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PATCH
    @Path("/{id}")
    public Response updateNotebook(@PathParam("id") Long id, Notebook notebook) {
        Notebook updated = notebookService.updateNotebook(id, notebook);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteNotebook(@PathParam("id") Long id) {
        notebookService.deleteNotebook(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/current-user")
    @RolesAllowed({ "user", "admin" })
    public List<Notebook> getMyNotebooks() {
        return notebookService.findAllForCurrentUser();
    }
}
