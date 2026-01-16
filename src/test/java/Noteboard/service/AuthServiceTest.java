package Noteboard.service;

import Noteboard.model.ApplicationUser;
import Noteboard.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private ApplicationUserRepository userRepository;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        userRepository = mock(ApplicationUserRepository.class);
        authService.userRepository = userRepository;
    }

    @Test
    void authenticate_throwsWhenUsernameMissing() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(null, "password"));
        assertEquals("Username is required.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate("   ", "password"));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void authenticate_throwsWhenPasswordMissing() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate("user", null));
        assertEquals("Password is required.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate("user", "   "));
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    void authenticate_throwsOnInvalidCredentials() {
        when(userRepository.findByUsername("user")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate("user", "password"));
        assertEquals("Invalid credentials.", ex.getMessage());

        ApplicationUser user = new ApplicationUser();
        user.setUsername("user");
        user.setPassword("other");
        when(userRepository.findByUsername("user")).thenReturn(user);

        ex = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate("user", "password"));
        assertEquals("Invalid credentials.", ex.getMessage());
    }

    @Test
    void authenticate_returnsJwtTokenOnSuccess() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user");
        // store bcrypt-hashed password as it would be in the database
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setRole("admin");

        when(userRepository.findByUsername("user")).thenReturn(user);

        String token = authService.authenticate("user", "password");

        assertNotNull(token);
        assertFalse(token.trim().isEmpty(), "Token should not be empty");
    }
}
