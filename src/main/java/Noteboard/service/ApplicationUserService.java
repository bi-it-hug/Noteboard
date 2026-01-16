package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.ApplicationUser;
import Noteboard.repository.ApplicationUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ApplicationScoped
public class ApplicationUserService {

    @Inject
    ApplicationUserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<ApplicationUser> findAll() {
        return userRepository.listAll();
    }

    public ApplicationUser findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public ApplicationUser createUser(ApplicationUser user) {
        if (user == null || user.username == null || user.username.trim().isEmpty()) {
            throw new ValidationException("Username is required.");
        }
        if (user.password == null || user.password.trim().isEmpty()) {
            throw new ValidationException("Password is required.");
        }
        if (userRepository.findByUsername(user.username) != null) {
            throw new ValidationException("Username already exists.");
        }
        if (user.role == null || user.role.trim().isEmpty()) {
            user.role = "user";
        }

        // Hash password before saving
        user.password = passwordEncoder.encode(user.password.trim());

        userRepository.persist(user);
        return user;
    }

    @Transactional
    public ApplicationUser updateUser(Long id, ApplicationUser user) {
        ApplicationUser existingUser = userRepository.findById(id);
        if (existingUser == null) {
            throw new ResourceNotFoundException("User with ID " + id + " not found");
        }
        if (user.username != null && !user.username.trim().isEmpty()) {
            ApplicationUser userWithSameUsername = userRepository.findByUsername(user.username.trim());
            if (userWithSameUsername != null && !userWithSameUsername.getId().equals(id)) {
                throw new ValidationException("Username already exists");
            }
            existingUser.setUsername(user.username.trim());
        }
        if (user.password != null && !user.password.trim().isEmpty()) {
            // Re-hash new password
            existingUser.setPassword(passwordEncoder.encode(user.password.trim()));
        }
        if (user.role != null && !user.role.trim().isEmpty()) {
            existingUser.setRole(user.role.trim());
        }
        userRepository.persist(existingUser);
        return existingUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        ApplicationUser user = userRepository.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User with ID " + id + " not found");
        }
        userRepository.delete(user);
    }
}
