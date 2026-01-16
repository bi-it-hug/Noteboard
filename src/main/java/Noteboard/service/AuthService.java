package Noteboard.service;

import Noteboard.repository.ApplicationUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import Noteboard.model.ApplicationUser;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ApplicationScoped
public class AuthService {

    @Inject
    ApplicationUserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        ApplicationUser user = userRepository.findByUsername(username.trim());

        if (user == null) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        boolean matchesHashed = passwordEncoder.matches(password, user.getPassword());
        boolean matchesLegacyPlaintext = !matchesHashed && password.equals(user.getPassword());

        if (!matchesHashed && !matchesLegacyPlaintext) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        // If password was still stored in plain text, upgrade it to a hashed value
        if (matchesLegacyPlaintext) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.persist(user);
        }

        return Jwt.upn(user.getUsername())
                .issuer("https://example.com/issuer")
                .groups(java.util.Set.of(user.getRole() != null ? user.getRole() : "user"))
                .sign();
    }
}
