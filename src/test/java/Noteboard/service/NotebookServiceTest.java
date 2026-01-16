package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.ApplicationUser;
import Noteboard.model.Notebook;
import Noteboard.repository.ApplicationUserRepository;
import Noteboard.repository.NotebookRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotebookServiceTest {

    private NotebookService notebookService;
    private NotebookRepository notebookRepository;
    private ApplicationUserRepository userRepository;
    private JsonWebToken jwt;

    @BeforeEach
    void setUp() {
        notebookService = new NotebookService();
        notebookRepository = mock(NotebookRepository.class);
        userRepository = mock(ApplicationUserRepository.class);
        jwt = mock(JsonWebToken.class);

        notebookService.notebookRepository = notebookRepository;
        notebookService.userRepository = userRepository;
        notebookService.jwt = jwt;
    }

    @Test
    void findAllForCurrentUser_throwsWhenNoUserInToken() {
        when(jwt.getName()).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.findAllForCurrentUser());
        assertTrue(ex.getMessage().contains("No authenticated user"));
    }

    @Test
    void findAllForCurrentUser_returnsNotebooksForUser() {
        when(jwt.getName()).thenReturn("alice");
        Notebook nb = new Notebook();
        nb.setTitle("My notebook");
        when(notebookRepository.findByUsername("alice")).thenReturn(List.of(nb));

        List<Notebook> result = notebookService.findAllForCurrentUser();

        assertEquals(1, result.size());
        assertEquals("My notebook", result.getFirst().title);
    }

    @Test
    void createNotebook_validatesInputAndPersists() {
        Notebook notebook = new Notebook();
        notebook.setTitle("  Title  ");
        notebook.setDescription("  Desc  ");

        when(jwt.getName()).thenReturn("bob");

        ApplicationUser user = new ApplicationUser();
        user.setUsername("bob");
        when(userRepository.findByUsername("bob")).thenReturn(user);

        Notebook result = notebookService.createNotebook(notebook);

        assertSame(user, result.getUser());
        verify(notebookRepository).persist(result);
    }

    @Test
    void createNotebook_throwsWhenUserNotFound() {
        Notebook notebook = new Notebook();
        notebook.setTitle("Title");
        notebook.setDescription("Desc");

        when(jwt.getName()).thenReturn("charlie");
        when(userRepository.findByUsername("charlie")).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notebookService.createNotebook(notebook));
        assertTrue(ex.getMessage().contains("Authenticated user 'charlie' not found."));
    }

    @Test
    void findAll_returnsAllNotebooks() {
        Notebook nb1 = new Notebook();
        nb1.setTitle("Notebook 1");
        Notebook nb2 = new Notebook();
        nb2.setTitle("Notebook 2");
        when(notebookRepository.listAll()).thenReturn(List.of(nb1, nb2));

        List<Notebook> result = notebookService.findAll();

        assertEquals(2, result.size());
        verify(notebookRepository).listAll();
    }

    @Test
    void findById_returnsNotebookWhenExists() {
        Notebook notebook = new Notebook();
        notebook.setId(1L);
        notebook.setTitle("Test Notebook");
        when(notebookRepository.findById(1L)).thenReturn(notebook);

        Notebook result = notebookService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Notebook", result.title);
        verify(notebookRepository).findById(1L);
    }

    @Test
    void findById_returnsNullWhenNotExists() {
        when(notebookRepository.findById(999L)).thenReturn(null);

        Notebook result = notebookService.findById(999L);

        assertNull(result);
        verify(notebookRepository).findById(999L);
    }

    @Test
    void createNotebook_throwsWhenNotebookIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(null));
        assertEquals("Notebook cannot be null", ex.getMessage());
    }

    @Test
    void createNotebook_throwsWhenTitleIsNull() {
        Notebook notebook = new Notebook();
        notebook.setDescription("Description");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(notebook));
        assertEquals("Title is required", ex.getMessage());
    }

    @Test
    void createNotebook_throwsWhenTitleIsEmpty() {
        Notebook notebook = new Notebook();
        notebook.setTitle("   ");
        notebook.setDescription("Description");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(notebook));
        assertEquals("Title is required", ex.getMessage());
    }

    @Test
    void createNotebook_throwsWhenDescriptionIsNull() {
        Notebook notebook = new Notebook();
        notebook.setTitle("Title");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(notebook));
        assertEquals("Description is required", ex.getMessage());
    }

    @Test
    void createNotebook_throwsWhenDescriptionIsEmpty() {
        Notebook notebook = new Notebook();
        notebook.setTitle("Title");
        notebook.setDescription("   ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(notebook));
        assertEquals("Description is required", ex.getMessage());
    }

    @Test
    void createNotebook_throwsWhenNoAuthenticatedUser() {
        Notebook notebook = new Notebook();
        notebook.setTitle("Title");
        notebook.setDescription("Description");

        when(jwt.getName()).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> notebookService.createNotebook(notebook));
        assertTrue(ex.getMessage().contains("No authenticated user"));
    }

    @Test
    void updateNotebook_throwsWhenNotebookNotFound() {
        Notebook notebook = new Notebook();
        notebook.setTitle("New Title");

        when(notebookRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notebookService.updateNotebook(999L, notebook));
        assertTrue(ex.getMessage().contains("Notebook with ID 999 not found"));
    }

    @Test
    void updateNotebook_updatesTitleWhenProvided() {
        Notebook existingNotebook = new Notebook();
        existingNotebook.setId(1L);
        existingNotebook.setTitle("Old Title");
        existingNotebook.setDescription("Description");

        Notebook updateNotebook = new Notebook();
        updateNotebook.setTitle("New Title");

        when(notebookRepository.findById(1L)).thenReturn(existingNotebook);

        Notebook result = notebookService.updateNotebook(1L, updateNotebook);

        assertEquals("New Title", result.title);
        assertEquals("Description", result.description);
        verify(notebookRepository).persist(existingNotebook);
    }

    @Test
    void updateNotebook_updatesDescriptionWhenProvided() {
        Notebook existingNotebook = new Notebook();
        existingNotebook.setId(1L);
        existingNotebook.setTitle("Title");
        existingNotebook.setDescription("Old Description");

        Notebook updateNotebook = new Notebook();
        updateNotebook.setDescription("New Description");

        when(notebookRepository.findById(1L)).thenReturn(existingNotebook);

        Notebook result = notebookService.updateNotebook(1L, updateNotebook);

        assertEquals("Title", result.title);
        assertEquals("New Description", result.description);
        verify(notebookRepository).persist(existingNotebook);
    }

    @Test
    void updateNotebook_ignoresEmptyTitle() {
        Notebook existingNotebook = new Notebook();
        existingNotebook.setId(1L);
        existingNotebook.setTitle("Title");
        existingNotebook.setDescription("Description");

        Notebook updateNotebook = new Notebook();
        updateNotebook.setTitle("   ");

        when(notebookRepository.findById(1L)).thenReturn(existingNotebook);

        Notebook result = notebookService.updateNotebook(1L, updateNotebook);

        assertEquals("Title", result.title);
        verify(notebookRepository).persist(existingNotebook);
    }

    @Test
    void updateNotebook_ignoresEmptyDescription() {
        Notebook existingNotebook = new Notebook();
        existingNotebook.setId(1L);
        existingNotebook.setTitle("Title");
        existingNotebook.setDescription("Description");

        Notebook updateNotebook = new Notebook();
        updateNotebook.setDescription("   ");

        when(notebookRepository.findById(1L)).thenReturn(existingNotebook);

        Notebook result = notebookService.updateNotebook(1L, updateNotebook);

        assertEquals("Description", result.description);
        verify(notebookRepository).persist(existingNotebook);
    }

    @Test
    void deleteNotebook_throwsWhenNotebookNotFound() {
        when(notebookRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notebookService.deleteNotebook(999L));
        assertTrue(ex.getMessage().contains("Notebook with ID 999 not found"));
    }

    @Test
    void deleteNotebook_deletesNotebookWhenExists() {
        Notebook notebook = new Notebook();
        notebook.setId(1L);
        notebook.setTitle("Test Notebook");
        when(notebookRepository.findById(1L)).thenReturn(notebook);

        notebookService.deleteNotebook(1L);

        verify(notebookRepository).findById(1L);
        verify(notebookRepository).delete(notebook);
    }
}
