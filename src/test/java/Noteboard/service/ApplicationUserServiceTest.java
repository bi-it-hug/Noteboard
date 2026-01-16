package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.ApplicationUser;
import Noteboard.repository.ApplicationUserRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationUserServiceTest {

    private ApplicationUserService userService;
    private ApplicationUserRepository userRepository;
    private JsonWebToken jwt;

    @BeforeEach
    void setUp() {
        userService = new ApplicationUserService();
        userRepository = mock(ApplicationUserRepository.class);
        jwt = mock(JsonWebToken.class);

        userService.userRepository = userRepository;
        userService.jwt = jwt;
    }

    @Test
    void findAll_returnsAllUsers() {
        ApplicationUser user1 = new ApplicationUser();
        user1.setUsername("user1");
        ApplicationUser user2 = new ApplicationUser();
        user2.setUsername("user2");
        when(userRepository.listAll()).thenReturn(List.of(user1, user2));

        List<ApplicationUser> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userRepository).listAll();
    }

    @Test
    void findById_returnsUserWhenExists() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findById(1L)).thenReturn(user);

        ApplicationUser result = userService.findById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.username);
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_returnsNullWhenNotExists() {
        when(userRepository.findById(999L)).thenReturn(null);

        ApplicationUser result = userService.findById(999L);

        assertNull(result);
        verify(userRepository).findById(999L);
    }

    @Test
    void createUser_throwsWhenUserIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(null));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void createUser_throwsWhenUsernameIsNull() {
        ApplicationUser user = new ApplicationUser();
        user.username = null;
        user.password = "password";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void createUser_throwsWhenUsernameIsEmpty() {
        ApplicationUser user = new ApplicationUser();
        user.username = "   ";
        user.password = "password";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Username is required.", ex.getMessage());
    }

    @Test
    void createUser_throwsWhenPasswordIsNull() {
        ApplicationUser user = new ApplicationUser();
        user.username = "testuser";
        user.password = null;

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    void createUser_throwsWhenPasswordIsEmpty() {
        ApplicationUser user = new ApplicationUser();
        user.username = "testuser";
        user.password = "   ";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    void createUser_throwsWhenUsernameAlreadyExists() {
        ApplicationUser user = new ApplicationUser();
        user.username = "existinguser";
        user.password = "password";
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(existingUser);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Username already exists.", ex.getMessage());
    }

    @Test
    void createUser_setsDefaultRoleWhenNotProvided() {
        ApplicationUser user = new ApplicationUser();
        user.username = "newuser";
        user.password = "password";
        user.role = null;

        when(userRepository.findByUsername("newuser")).thenReturn(null);

        ApplicationUser result = userService.createUser(user);

        assertEquals("user", result.role);
        verify(userRepository).persist(result);
    }

    @Test
    void createUser_hashesPassword() {
        ApplicationUser user = new ApplicationUser();
        user.username = "newuser";
        user.password = "plainpassword";
        user.role = "user";

        when(userRepository.findByUsername("newuser")).thenReturn(null);

        ApplicationUser result = userService.createUser(user);

        assertNotEquals("plainpassword", result.password);
        assertTrue(result.password.startsWith("$2a$") || result.password.startsWith("$2b$"));
        verify(userRepository).persist(result);
    }

    @Test
    void createUser_trimsUsernameAndPassword() {
        ApplicationUser user = new ApplicationUser();
        user.username = "  newuser  ";
        user.password = "  password  ";
        user.role = "user";

        when(userRepository.findByUsername("newuser")).thenReturn(null);

        ApplicationUser result = userService.createUser(user);

        assertEquals("newuser", result.username);
        verify(userRepository).persist(result);
    }

    @Test
    void updateUser_throwsWhenUserNotFound() {
        ApplicationUser user = new ApplicationUser();
        user.username = "newuser";

        when(userRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(999L, user));
        assertTrue(ex.getMessage().contains("User with ID 999 not found"));
    }

    @Test
    void updateUser_updatesUsernameWhenProvided() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setPassword("oldpassword");
        existingUser.setRole("user");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.username = "newuser";

        when(userRepository.findById(1L)).thenReturn(existingUser);
        when(userRepository.findByUsername("newuser")).thenReturn(null);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("newuser", result.username);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_throwsWhenNewUsernameAlreadyExists() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");

        ApplicationUser otherUser = new ApplicationUser();
        otherUser.setId(2L);
        otherUser.setUsername("newuser");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.username = "newuser";

        when(userRepository.findById(1L)).thenReturn(existingUser);
        when(userRepository.findByUsername("newuser")).thenReturn(otherUser);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.updateUser(1L, updateUser));
        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void updateUser_allowsSameUsernameForSameUser() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("sameuser");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.username = "sameuser";

        when(userRepository.findById(1L)).thenReturn(existingUser);
        when(userRepository.findByUsername("sameuser")).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("sameuser", result.username);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_updatesPasswordWhenProvided() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("user");
        existingUser.setPassword("oldhashedpassword");
        existingUser.setRole("user");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.password = "newpassword";

        when(userRepository.findById(1L)).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertNotEquals("oldhashedpassword", result.password);
        assertNotEquals("newpassword", result.password);
        assertTrue(result.password.startsWith("$2a$") || result.password.startsWith("$2b$"));
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_updatesRoleWhenProvided() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("user");
        existingUser.setRole("user");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.role = "admin";

        when(userRepository.findById(1L)).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("admin", result.role);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_ignoresEmptyUsername() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setUsername("user");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.username = "   ";

        when(userRepository.findById(1L)).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("user", result.username);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_ignoresEmptyPassword() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setPassword("oldhashedpassword");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.password = "   ";

        when(userRepository.findById(1L)).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("oldhashedpassword", result.password);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void updateUser_ignoresEmptyRole() {
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(1L);
        existingUser.setRole("user");

        ApplicationUser updateUser = new ApplicationUser();
        updateUser.role = "   ";

        when(userRepository.findById(1L)).thenReturn(existingUser);

        ApplicationUser result = userService.updateUser(1L, updateUser);

        assertEquals("user", result.role);
        verify(userRepository).persist(existingUser);
    }

    @Test
    void deleteUser_throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(999L));
        assertTrue(ex.getMessage().contains("User with ID 999 not found"));
    }

    @Test
    void deleteUser_deletesUserWhenExists() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("user");
        when(userRepository.findById(1L)).thenReturn(user);

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }
}
