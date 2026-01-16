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
}
