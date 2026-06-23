package pr3;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr3.server.StoreServerHTTP;
import pr4.db.ProductRepository;
import pr4.db.SqliteProductRepository;
import pr4.db.SqliteUserRepository;
import pr4.db.UserRepository;
import pr4.model.Product;
import pr4.service.ProductService;

import java.io.IOException;

class StoreServerHTTPTest {

    private static final int TEST_PORT = 8181;

    private StoreServerHTTP server;
    private int appleId;

    @BeforeEach
    void start() throws IOException {
        ProductRepository productDb = new SqliteProductRepository(":memory:");
        appleId = productDb.insert(new Product("Apple", "Fruit", "Bazar", 100, 1.5));

        UserRepository users = new SqliteUserRepository(":memory:");

        server = new StoreServerHTTP(TEST_PORT, new ProductService(productDb), users);
        server.start();

        RestAssured.port = TEST_PORT;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanUp() {
        server.stop();
    }


    private String login(String username, String password) {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password))

            .expect()
            .statusCode(200)

            .when()
            .post("/login")
            .path("token");
    }

    private String bearer() {
        return "Bearer " + login("user", "password");
    }

    @Test
    void shouldReturnTokenForValidCredentials() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"user\",\"password\":\"password\"}")

            .expect()
            .statusCode(200)
            .body("token", CoreMatchers.notNullValue())

            .when()
            .post("/login");
    }

    @Test
    void shouldRejectLoginWithWrongPassword() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"user\",\"password\":\"suchanincorrectpassword\"}")

            .expect()
            .statusCode(401)

            .when()
            .post("/login");
    }

    @Test
    void shouldRejectProtectedRouteWithoutToken() {
        RestAssured.given()
            .expect()
            .statusCode(401)

            .when()
            .get("/products/" + appleId);
    }

    @Test
    void shouldRejectProtectedRouteWithInvalidToken() {
        RestAssured.given()
            .header("Authorization", "Bearer not.a.real.token")

            .expect()
            .statusCode(401)

            .when()
            .get("/products/" + appleId);
    }

    @Test
    void shouldGetProductById() {
        RestAssured.given()
            .header("Authorization", bearer())

            .expect()
            .statusCode(200)
            .body("name", CoreMatchers.is("Apple"))
            .body("category", CoreMatchers.is("Fruit"))

            .when()
            .get("/products/" + appleId);
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() {
        RestAssured.given()
            .header("Authorization", bearer())

            .expect()
            .statusCode(404)

            .when()
            .get("/products/67890");
    }

    @Test
    void shouldCreateProduct() {
        RestAssured.given()
            .header("Authorization", bearer())
            .contentType(ContentType.JSON)
            .body("""
               {"name":"Banana","category":"Fruit","manufacturer":"Bazar","quantity":50,"price":0.8}
            """)

            .expect()
            .statusCode(201)
            .body("id", CoreMatchers.notNullValue())
            .body("name", CoreMatchers.is("Banana"))

            .when()
            .put("/products");
    }

    @Test
    void shouldRejectDuplicateName() {
        RestAssured.given()
            .header("Authorization", bearer())
            .contentType(ContentType.JSON)
            .body("""
               {"name":"Apple","category":"Fruit","manufacturer":"Bazar","quantity":10,"price":1.0}
            """)

            .expect()
            .statusCode(409)

            .when()
            .put("/products");
    }

    @Test
    void shouldUpdateProduct() {
        String auth = bearer();

        RestAssured.given()
            .header("Authorization", auth)
            .contentType(ContentType.JSON)
            .body("""
              {"name":"Apple","category":"Fruit","manufacturer":"Bazar","quantity":150,"price":2.0}
            """)

            .expect()
            .statusCode(200)
            .body("quantity", CoreMatchers.is(150))
            .body("price", CoreMatchers.is(2.0f))

            .when()
            .post("/products/" + appleId);
    }

    @Test
    void shouldDeleteProduct() {
        String auth = bearer();

        RestAssured.given()
            .header("Authorization", auth)
            .expect()
            .statusCode(204)
            .when()
            .delete("/products/" + appleId);

        // confirm it's gone
        RestAssured.given()
            .header("Authorization", auth)
            .expect()
            .statusCode(404)
            .when()
            .get("/products/" + appleId);
    }
}
