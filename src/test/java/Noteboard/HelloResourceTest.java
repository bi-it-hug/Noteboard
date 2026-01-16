package Noteboard;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HelloResourceTest {

    @Test
    void helloEndpoint_returnsExpectedMessage() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(is("Hello from Quarkus REST"));
    }
}
