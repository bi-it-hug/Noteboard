package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.Note;
import Noteboard.model.Notebook;
import Noteboard.repository.NoteRepository;
import Noteboard.repository.NotebookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteServiceTest {

    private NoteService noteService;
    private NoteRepository noteRepository;
    private NotebookRepository notebookRepository;

    @BeforeEach
    void setUp() {
        noteService = new NoteService();
        noteRepository = mock(NoteRepository.class);
        notebookRepository = mock(NotebookRepository.class);

        noteService.noteRepository = noteRepository;
        noteService.notebookRepository = notebookRepository;
    }

    @Test
    void findAll_returnsAllNotes() {
        Note note1 = new Note("Title 1", "Content 1");
        Note note2 = new Note("Title 2", "Content 2");
        when(noteRepository.listAll()).thenReturn(List.of(note1, note2));

        List<Note> result = noteService.findAll();

        assertEquals(2, result.size());
        verify(noteRepository).listAll();
    }

    @Test
    void findById_returnsNoteWhenExists() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        when(noteRepository.findById(1L)).thenReturn(note);

        Note result = noteService.findById(1L);

        assertNotNull(result);
        assertEquals("Title", result.title);
        assertEquals("Content", result.content);
        verify(noteRepository).findById(1L);
    }

    @Test
    void findById_returnsNullWhenNotExists() {
        when(noteRepository.findById(999L)).thenReturn(null);

        Note result = noteService.findById(999L);

        assertNull(result);
        verify(noteRepository).findById(999L);
    }

    @Test
    void createNote_throwsWhenNoteIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(null));
        assertEquals("Note cannot be null", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenTitleIsNull() {
        Note note = new Note();
        note.content = "Content";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Title is required", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenTitleIsEmpty() {
        Note note = new Note();
        note.title = "   ";
        note.content = "Content";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Title is required", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenContentIsNull() {
        Note note = new Note();
        note.title = "Title";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Content is required", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenContentIsEmpty() {
        Note note = new Note();
        note.title = "Title";
        note.content = "   ";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Content is required", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenNotebookIsNull() {
        Note note = new Note();
        note.title = "Title";
        note.content = "Content";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Notebook is required to create a note.", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenNotebookIdIsNull() {
        Note note = new Note();
        note.title = "Title";
        note.content = "Content";
        Notebook notebook = new Notebook();
        note.setNotebook(notebook);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> noteService.createNote(note));
        assertEquals("Notebook is required to create a note.", ex.getMessage());
    }

    @Test
    void createNote_throwsWhenNotebookNotFound() {
        Note note = new Note();
        note.title = "Title";
        note.content = "Content";
        Notebook notebook = new Notebook();
        notebook.setId(1L);
        note.setNotebook(notebook);

        when(notebookRepository.findById(1L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> noteService.createNote(note));
        assertTrue(ex.getMessage().contains("Notebook with ID 1 not found"));
    }

    @Test
    void createNote_persistsNoteWithNotebook() {
        Note note = new Note();
        note.title = "Title";
        note.content = "Content";
        Notebook notebook = new Notebook();
        notebook.setId(1L);
        note.setNotebook(notebook);

        when(notebookRepository.findById(1L)).thenReturn(notebook);

        Note result = noteService.createNote(note);

        assertSame(notebook, result.getNotebook());
        verify(notebookRepository).findById(1L);
        verify(noteRepository).persist(result);
    }

    @Test
    void updateNote_throwsWhenNoteNotFound() {
        Note note = new Note();
        note.title = "New Title";

        when(noteRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> noteService.updateNote(999L, note));
        assertTrue(ex.getMessage().contains("Note with ID 999 not found"));
    }

    @Test
    void updateNote_updatesTitleWhenProvided() {
        Note existingNote = new Note("Old Title", "Content");
        existingNote.setId(1L);
        Note updateNote = new Note();
        updateNote.title = "New Title";

        when(noteRepository.findById(1L)).thenReturn(existingNote);

        Note result = noteService.updateNote(1L, updateNote);

        assertEquals("New Title", result.title);
        assertEquals("Content", result.content);
        verify(noteRepository).persist(existingNote);
    }

    @Test
    void updateNote_updatesContentWhenProvided() {
        Note existingNote = new Note("Title", "Old Content");
        existingNote.setId(1L);
        Note updateNote = new Note();
        updateNote.content = "New Content";

        when(noteRepository.findById(1L)).thenReturn(existingNote);

        Note result = noteService.updateNote(1L, updateNote);

        assertEquals("Title", result.title);
        assertEquals("New Content", result.content);
        verify(noteRepository).persist(existingNote);
    }

    @Test
    void updateNote_updatesNotebookWhenProvided() {
        Note existingNote = new Note("Title", "Content");
        existingNote.setId(1L);
        Notebook oldNotebook = new Notebook();
        oldNotebook.setId(1L);
        existingNote.setNotebook(oldNotebook);

        Note updateNote = new Note();
        Notebook newNotebook = new Notebook();
        newNotebook.setId(2L);
        updateNote.setNotebook(newNotebook);

        when(noteRepository.findById(1L)).thenReturn(existingNote);
        when(notebookRepository.findById(2L)).thenReturn(newNotebook);

        Note result = noteService.updateNote(1L, updateNote);

        assertSame(newNotebook, result.getNotebook());
        verify(notebookRepository).findById(2L);
        verify(noteRepository).persist(existingNote);
    }

    @Test
    void updateNote_throwsWhenNewNotebookNotFound() {
        Note existingNote = new Note("Title", "Content");
        existingNote.setId(1L);
        Note updateNote = new Note();
        Notebook newNotebook = new Notebook();
        newNotebook.setId(999L);
        updateNote.setNotebook(newNotebook);

        when(noteRepository.findById(1L)).thenReturn(existingNote);
        when(notebookRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> noteService.updateNote(1L, updateNote));
        assertTrue(ex.getMessage().contains("Notebook with ID 999 not found"));
    }

    @Test
    void updateNote_ignoresEmptyTitle() {
        Note existingNote = new Note("Title", "Content");
        existingNote.setId(1L);
        Note updateNote = new Note();
        updateNote.title = "   ";

        when(noteRepository.findById(1L)).thenReturn(existingNote);

        Note result = noteService.updateNote(1L, updateNote);

        assertEquals("Title", result.title);
        verify(noteRepository).persist(existingNote);
    }

    @Test
    void updateNote_ignoresEmptyContent() {
        Note existingNote = new Note("Title", "Content");
        existingNote.setId(1L);
        Note updateNote = new Note();
        updateNote.content = "   ";

        when(noteRepository.findById(1L)).thenReturn(existingNote);

        Note result = noteService.updateNote(1L, updateNote);

        assertEquals("Content", result.content);
        verify(noteRepository).persist(existingNote);
    }

    @Test
    void deleteNote_throwsWhenNoteNotFound() {
        when(noteRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> noteService.deleteNote(999L));
        assertTrue(ex.getMessage().contains("Note with ID 999 not found"));
    }

    @Test
    void deleteNote_deletesNoteWhenExists() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        when(noteRepository.findById(1L)).thenReturn(note);

        noteService.deleteNote(1L);

        verify(noteRepository).findById(1L);
        verify(noteRepository).delete(note);
    }
}
