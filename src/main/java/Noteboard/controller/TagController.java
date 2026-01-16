package Noteboard.controller;

import Noteboard.model.Tag;
import Noteboard.service.TagService;
import jakarta.inject.Inject;
import java.util.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagController {

    @Inject
    TagService tagService;

    @GET
    @RolesAllowed({ "user", "admin" })
    public List<Tag> getAllTags() {
        return tagService.findAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response getTag(@PathParam("id") Long id) {
        Tag tag = tagService.findById(id);
        if (tag == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(tag).build();
    }

    @POST
    @RolesAllowed({ "user", "admin" })
    public Response createTag(Tag tag) {
        Tag created = tagService.createTag(tag);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response updateTag(@PathParam("id") Long id, Tag tag) {
        Tag updated = tagService.updateTag(id, tag);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response deleteTag(@PathParam("id") Long id) {
        tagService.deleteTag(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
