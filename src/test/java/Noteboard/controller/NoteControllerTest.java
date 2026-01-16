package Noteboard.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class NoteControllerTest {

    private String token;
    private Long notebookId;

    @BeforeEach
    void setUp() {
        // Register and login a user for each test
        String username = "note_test_user_" + System.currentTimeMillis();
        String password = "test_password";

        given()
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
                .statusCode(201);

        token = given()
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

        // Create a notebook for the notes
        notebookId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Test Notebook",
                          "description": "Test Description"
                        }
                        """)
                .when()
                .post("/notebooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void getAllNotes_returnsListOfNotes() {
        // Create a note first
        createTestNote();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/notes")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getNote_returnsNoteWhenExists() {
        Long noteId = createTestNote();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/notes/" + noteId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Test Note"))
                .body("content", equalTo("Test Content"))
                .body("id", equalTo(noteId.intValue()));
    }

    @Test
    void getNote_returnsNotFoundWhenNotExists() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/notes/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void createNote_createsNoteSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "New Note",
                          "content": "New Content",
                          "notebook": {
                            "id": %d
                          }
                        }
                        """.formatted(notebookId))
                .when()
                .post("/notes")
                .then()
                .statusCode(201)
                .body("title", equalTo("New Note"))
                .body("content", equalTo("New Content"))
                .body("id", notNullValue());
    }

    @Test
    void createNote_returnsBadRequestWhenTitleMissing() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "content": "Content",
                          "notebook": {
                            "id": %d
                          }
                        }
                        """.formatted(notebookId))
                .when()
                .post("/notes")
                .then()
                .statusCode(400);
    }

    @Test
    void createNote_returnsBadRequestWhenContentMissing() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Title",
                          "notebook": {
                            "id": %d
                          }
                        }
                        """.formatted(notebookId))
                .when()
                .post("/notes")
                .then()
                .statusCode(400);
    }

    @Test
    void createNote_returnsBadRequestWhenNotebookMissing() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Title",
                          "content": "Content"
                        }
                        """)
                .when()
                .post("/notes")
                .then()
                .statusCode(400);
    }

    @Test
    void updateNote_updatesNoteSuccessfully() {
        Long noteId = createTestNote();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Updated Note",
                          "content": "Updated Content"
                        }
                        """)
                .when()
                .patch("/notes/" + noteId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Note"))
                .body("content", equalTo("Updated Content"));
    }

    @Test
    void updateNote_returnsNotFoundWhenNoteNotExists() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Updated Note"
                        }
                        """)
                .when()
                .patch("/notes/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteNote_deletesNoteSuccessfully() {
        Long noteId = createTestNote();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/notes/" + noteId)
                .then()
                .statusCode(204);

        // Verify note is deleted
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/notes/" + noteId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteNote_returnsNotFoundWhenNoteNotExists() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/notes/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void addTagToNote_addsTagSuccessfully() {
        Long noteId = createTestNote();
        Long tagId = createTestTag();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/notes/" + noteId + "/tags/" + tagId)
                .then()
                .statusCode(200)
                .body("tags.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void removeTagFromNote_removesTagSuccessfully() {
        Long noteId = createTestNote();
        Long tagId = createTestTag();

        // Add tag first
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/notes/" + noteId + "/tags/" + tagId)
                .then()
                .statusCode(200);

        // Remove tag
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/notes/" + noteId + "/tags/" + tagId)
                .then()
                .statusCode(200);
    }

    @Test
    void getAllNotes_requiresAuthentication() {
        given()
                .when()
                .get("/notes")
                .then()
                .statusCode(401);
    }

    private Long createTestNote() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "Test Note",
                          "content": "Test Content",
                          "notebook": {
                            "id": %d
                          }
                        }
                        """.formatted(notebookId))
                .when()
                .post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Long createTestTag() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "Test Tag %d"
                        }
                        """.formatted(System.currentTimeMillis()))
                .when()
                .post("/tags")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
