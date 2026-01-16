package Noteboard.repository;

import Noteboard.model.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TagRepository implements PanacheRepository<Tag> {
    public Tag findByName(String name) {
        return find("name", name).firstResult();
    }
}
