package Noteboard.repository;

import Noteboard.model.Notebook;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.List;

@ApplicationScoped
public class NotebookRepository implements PanacheRepository<Notebook> {

    public List<Notebook> findByUsername(String username) {
        return list("user.username", username);
    }
}
