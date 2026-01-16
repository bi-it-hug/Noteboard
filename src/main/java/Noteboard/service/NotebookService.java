package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.ApplicationUser;
import Noteboard.model.Notebook;
import Noteboard.repository.ApplicationUserRepository;
import Noteboard.repository.NotebookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class NotebookService {

    @Inject
    NotebookRepository notebookRepository;

    @Inject
    ApplicationUserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    public List<Notebook> findAll() {
        return notebookRepository.listAll();
    }

    public List<Notebook> findAllForCurrentUser() {
        String username = jwt != null ? jwt.getName() : null;
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("No authenticated user found. Please log in first.");
        }
        return notebookRepository.findByUsername(username.trim());
    }

    public Notebook findById(Long id) {
        return notebookRepository.findById(id);
    }

    @Transactional
    public Notebook createNotebook(Notebook notebook) {
        if (notebook == null) {
            throw new ValidationException("Notebook cannot be null");
        }
        if (notebook.title == null || notebook.title.trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (notebook.description == null || notebook.description.trim().isEmpty()) {
            throw new ValidationException("Description is required");
        }

        String username = jwt != null ? jwt.getName() : null;
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("No authenticated user found. Please log in first.");
        }

        ApplicationUser currentUser = userRepository.findByUsername(username.trim());
        if (currentUser == null) {
            throw new ResourceNotFoundException("Authenticated user '" + username + "' not found.");
        }
        notebook.setUser(currentUser);
        notebookRepository.persist(notebook);
        return notebook;
    }

    @Transactional
    public Notebook updateNotebook(Long id, Notebook notebook) {
        Notebook existingNotebook = notebookRepository.findById(id);
        if (existingNotebook == null) {
            throw new ResourceNotFoundException("Notebook with ID " + id + " not found");
        }
        if (notebook.title != null && !notebook.title.trim().isEmpty()) {
            existingNotebook.setTitle(notebook.title.trim());
        }
        if (notebook.description != null && !notebook.description.trim().isEmpty()) {
            existingNotebook.setDescription(notebook.description.trim());
        }
        notebookRepository.persist(existingNotebook);
        return existingNotebook;
    }

    @Transactional
    public void deleteNotebook(Long id) {
        Notebook notebook = notebookRepository.findById(id);
        if (notebook == null) {
            throw new ResourceNotFoundException("Notebook with ID " + id + " not found");
        }
        notebookRepository.delete(notebook);
    }
}

