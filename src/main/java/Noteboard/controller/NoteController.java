package Noteboard.controller;

import Noteboard.model.Note;
import Noteboard.service.TagService;
import Noteboard.service.NoteService;
import jakarta.inject.Inject;
import java.util.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteController {

    @Inject
    NoteService noteService;

    @Inject
    TagService tagService;

    @GET
    @RolesAllowed({ "user", "admin" })
    public List<Note> getAllNotes() {
        return noteService.findAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response getNote(@PathParam("id") Long id) {
        Note note = noteService.findById(id);
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(note).build();
    }

    @POST
    @RolesAllowed({ "user", "admin" })
    public Response createNote(Note note) {
        Note created = noteService.createNote(note);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/{noteId}/tags/{tagId}")
    @RolesAllowed({ "user", "admin" })
    public Response addTagToNote(@PathParam("noteId") Long noteId, @PathParam("tagId") Long tagId) {
        Note note = tagService.addTagToNote(noteId, tagId);
        return Response.ok(note).build();
    }

    @DELETE
    @Path("/{noteId}/tags/{tagId}")
    @RolesAllowed({ "user", "admin" })
    public Response removeTagFromNote(@PathParam("noteId") Long noteId, @PathParam("tagId") Long tagId) {
        Note note = tagService.removeTagFromNote(noteId, tagId);
        return Response.ok(note).build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response updateNote(@PathParam("id") Long id, Note note) {
        Note updated = noteService.updateNote(id, note);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "user", "admin" })
    public Response deleteNote(@PathParam("id") Long id) {
        noteService.deleteNote(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
