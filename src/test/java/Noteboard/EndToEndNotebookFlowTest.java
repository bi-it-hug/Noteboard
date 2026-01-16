package Noteboard;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EndToEndNotebookFlowTest {

    @Test
    void register_login_and_create_notebook_flow() {
        // 1) Register a new user with a unique username so tests are repeatable
        String username = "e2e_user_" + System.currentTimeMillis();
        String password = "e2e_password";

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

        // 2) Login to obtain JWT token
        String token = given()
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

        // 3) Create a notebook for this user
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "title": "My E2E Notebook",
                          "description": "Created via end-to-end test"
                        }
                        """)
                .when()
                .post("/notebooks")
                .then()
                .statusCode(201)
                .body("title", equalTo("My E2E Notebook"))
                .body("description", equalTo("Created via end-to-end test"));

        // 4) Fetch notebooks for current user and ensure at least one is returned
        given()
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/notebooks/current-user")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].title", notNullValue());
    }
}
