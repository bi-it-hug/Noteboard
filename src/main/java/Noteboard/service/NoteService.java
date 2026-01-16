package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.Note;
import Noteboard.model.Notebook;
import Noteboard.repository.NoteRepository;
import Noteboard.repository.NotebookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class NoteService {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NotebookRepository notebookRepository;

    public List<Note> findAll() {
        return noteRepository.listAll();
    }

    public Note findById(Long id) {
        return noteRepository.findById(id);
    }

    @Transactional
    public Note createNote(Note note) {
        if (note == null) {
            throw new ValidationException("Note cannot be null");
        }
        if (note.title == null || note.title.trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (note.content == null || note.content.trim().isEmpty()) {
            throw new ValidationException("Content is required");
        }
        if (note.getNotebook() == null || note.getNotebook().getId() == null) {
            throw new ValidationException("Notebook is required to create a note.");
        }
        Notebook notebook = notebookRepository.findById(note.getNotebook().getId());
        if (notebook == null) {
            throw new ResourceNotFoundException("Notebook with ID " + note.getNotebook().getId() + " not found.");
        }

        note.setNotebook(notebook);
        noteRepository.persist(note);
        return note;
    }

    @Transactional
    public Note updateNote(Long id, Note note) {
        Note existingNote = noteRepository.findById(id);
        if (existingNote == null) {
            throw new ResourceNotFoundException("Note with ID " + id + " not found");
        }
        if (note.title != null && !note.title.trim().isEmpty()) {
            existingNote.setTitle(note.title.trim());
        }
        if (note.content != null && !note.content.trim().isEmpty()) {
            existingNote.setContent(note.content.trim());
        }
        if (note.getNotebook() != null && note.getNotebook().getId() != null) {
            Notebook notebook = notebookRepository.findById(note.getNotebook().getId());
            if (notebook == null) {
                throw new ResourceNotFoundException("Notebook with ID " + note.getNotebook().getId() + " not found");
            }
            existingNote.setNotebook(notebook);
        }
        noteRepository.persist(existingNote);
        return existingNote;
    }

    @Transactional
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id);
        if (note == null) {
            throw new ResourceNotFoundException("Note with ID " + id + " not found");
        }
        noteRepository.delete(note);
    }
}

