package Noteboard.repository;

import Noteboard.model.ApplicationUser;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ApplicationUserRepository implements PanacheRepository<ApplicationUser> {
    public ApplicationUser findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
