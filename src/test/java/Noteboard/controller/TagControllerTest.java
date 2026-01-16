package Noteboard.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TagControllerTest {

    private String token;

    @BeforeEach
    void setUp() {
        // Register and login a user for each test
        String username = "tag_test_user_" + System.currentTimeMillis();
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
    }

    @Test
    void getAllTags_returnsListOfTags() {
        // Create a tag first
        createTestTag();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tags")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getTag_returnsTagWhenExists() {
        Long tagId = createTestTag();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tags/" + tagId)
                .then()
                .statusCode(200)
                .body("id", equalTo(tagId.intValue()))
                .body("name", notNullValue());
    }

    @Test
    void getTag_returnsNotFoundWhenNotExists() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tags/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void createTag_createsTagSuccessfully() {
        String tagName = "Test Tag " + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName))
                .when()
                .post("/tags")
                .then()
                .statusCode(201)
                .body("name", equalTo(tagName))
                .body("id", notNullValue());
    }

    @Test
    void createTag_returnsBadRequestWhenNameMissing() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                        }
                        """)
                .when()
                .post("/tags")
                .then()
                .statusCode(400);
    }

    @Test
    void createTag_returnsBadRequestWhenNameIsEmpty() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "   "
                        }
                        """)
                .when()
                .post("/tags")
                .then()
                .statusCode(400);
    }

    @Test
    void createTag_returnsBadRequestWhenTagNameAlreadyExists() {
        String tagName = "Duplicate Tag " + System.currentTimeMillis();

        // Create first tag
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName))
                .when()
                .post("/tags")
                .then()
                .statusCode(201);

        // Try to create duplicate
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName))
                .when()
                .post("/tags")
                .then()
                .statusCode(400);
    }

    @Test
    void updateTag_updatesTagSuccessfully() {
        Long tagId = createTestTag();

        String newName = "Updated Tag " + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(newName))
                .when()
                .patch("/tags/" + tagId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName));
    }

    @Test
    void updateTag_returnsNotFoundWhenTagNotExists() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "Updated Tag"
                        }
                        """)
                .when()
                .patch("/tags/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void updateTag_returnsBadRequestWhenNewNameAlreadyExists() {
        String tagName1 = "Tag 1 " + System.currentTimeMillis();
        String tagName2 = "Tag 2 " + System.currentTimeMillis();

        // Create two tags
        Long tagId1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName1))
                .when()
                .post("/tags")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName2))
                .when()
                .post("/tags")
                .then()
                .statusCode(201);

        // Try to update first tag with second tag's name
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName2))
                .when()
                .patch("/tags/" + tagId1)
                .then()
                .statusCode(400);
    }

    @Test
    void deleteTag_deletesTagSuccessfully() {
        Long tagId = createTestTag();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/tags/" + tagId)
                .then()
                .statusCode(204);

        // Verify tag is deleted
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tags/" + tagId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteTag_returnsNotFoundWhenTagNotExists() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/tags/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void getAllTags_requiresAuthentication() {
        given()
                .when()
                .get("/tags")
                .then()
                .statusCode(401);
    }

    @Test
    void createTag_requiresAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Test Tag"
                        }
                        """)
                .when()
                .post("/tags")
                .then()
                .statusCode(401);
    }

    private Long createTestTag() {
        String tagName = "Test Tag " + System.currentTimeMillis();
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(tagName))
                .when()
                .post("/tags")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
