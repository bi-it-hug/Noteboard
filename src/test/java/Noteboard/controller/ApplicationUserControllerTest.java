package Noteboard.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ApplicationUserControllerTest {

    private String userToken;
    private String adminToken;
    private String username;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Register and login a regular user
        username = "user_test_" + System.currentTimeMillis();
        String password = "test_password";


        // Get user ID from registration response
        userId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "user"
                        }
                        """.formatted(username, password))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        userToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password))
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Register and login an admin user
        String adminUsername = "admin_test_" + System.currentTimeMillis();
        String adminPassword = "admin_password";

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "admin"
                        }
                        """.formatted(adminUsername, adminPassword))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201);

        adminToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(adminUsername, adminPassword))
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    void register_createsUserSuccessfully() {
        String newUsername = "new_user_" + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "password123",
                          "role": "user"
                        }
                        """.formatted(newUsername))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .body("username", equalTo(newUsername))
                .body("id", notNullValue());
    }

    @Test
    void register_returnsBadRequestWhenUsernameMissing() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400);
    }

    @Test
    void register_returnsBadRequestWhenPasswordMissing() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "testuser"
                        }
                        """)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400);
    }

    @Test
    void register_returnsBadRequestWhenUsernameAlreadyExists() {
        String existingUsername = "existing_" + System.currentTimeMillis();

        // Create first user
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "password123",
                          "role": "user"
                        }
                        """.formatted(existingUsername))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201);

        // Try to create duplicate
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "password123",
                          "role": "user"
                        }
                        """.formatted(existingUsername))
                .when()
                .post("/users/register")
                .then()
                .statusCode(400);
    }

    @Test
    void login_returnsTokenOnSuccess() {
        String loginUsername = "login_user_" + System.currentTimeMillis();
        String loginPassword = "login_password";

        // Register user first
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "user"
                        }
                        """.formatted(loginUsername, loginPassword))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201);

        // Login
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(loginUsername, loginPassword))
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void login_returnsBadRequestOnInvalidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "nonexistent",
                          "password": "wrongpassword"
                        }
                        """)
                .when()
                .post("/users/login")
                .then()
                .statusCode(400);
    }

    @Test
    void getAllUsers_requiresAdminRole() {
        // Regular user should not have access
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/users")
                .then()
                .statusCode(403);

        // Admin should have access
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getAllUsers_requiresAuthentication() {
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(401);
    }

    @Test
    void getUser_returnsUserWhenExists() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.intValue()))
                .body("username", equalTo(username));
    }

    @Test
    void getUser_returnsNotFoundWhenNotExists() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/users/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void getUser_requiresAuthentication() {
        given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(401);
    }

    @Test
    void updateUser_updatesUserSuccessfully() {
        String newUsername = "updated_" + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("""
                        {
                          "username": "%s"
                        }
                        """.formatted(newUsername))
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(200)
                .body("username", equalTo(newUsername));
    }

    @Test
    void updateUser_updatesPasswordSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("""
                        {
                          "password": "newpassword123"
                        }
                        """)
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(200);

        // Verify password was updated by logging in with new password
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "newpassword123"
                        }
                        """.formatted(username))
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void updateUser_returnsNotFoundWhenUserNotExists() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("""
                        {
                          "username": "updated"
                        }
                        """)
                .when()
                .patch("/users/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void updateUser_returnsBadRequestWhenNewUsernameAlreadyExists() {
        String otherUsername = "other_user_" + System.currentTimeMillis();

        // Create another user
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "password123",
                          "role": "user"
                        }
                        """.formatted(otherUsername, "password123"))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201);

        // Try to update current user with other user's username
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("""
                        {
                          "username": "%s"
                        }
                        """.formatted(otherUsername))
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(400);
    }

    @Test
    void updateUser_requiresAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "updated"
                        }
                        """)
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(401);
    }

    @Test
    void deleteUser_requiresAdminRole() {
        // Create a user to delete
        String deleteUsername = "delete_user_" + System.currentTimeMillis();
        Long deleteUserId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "password123",
                          "role": "user"
                        }
                        """.formatted(deleteUsername))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Regular user should not be able to delete
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/users/" + deleteUserId)
                .then()
                .statusCode(403);

        // Admin should be able to delete
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/users/" + deleteUserId)
                .then()
                .statusCode(204);
    }

    @Test
    void deleteUser_returnsNotFoundWhenUserNotExists() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/users/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteUser_requiresAuthentication() {
        given()
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(401);
    }
}
