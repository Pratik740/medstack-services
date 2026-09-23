import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }
    // 1. Arrange: ensure that the test has everything it needs to run properly
    // 2. act: Test begins
    // 3. assert: Assert act result with expected behavior

    @Test
    public void shouldReturnOKWithValidToken() {



        // Arrange
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        // Act + Assert
        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        // 1. Arrange: ensure that the test has everything it needs to run properly
        // 2. act: Test begins
        // 3. assert: Assert act result with expected behavior


        // Arrange
        String loginPayload = """
                {
                    "email": "invalid_user@test.com",
                    "password": "password123"
                }
                """;

        // Act + Assert
        given()
            .contentType("application/json")
            .body(loginPayload)
            .post("/auth/login")
            .then()
            .statusCode(401);

    }
}
