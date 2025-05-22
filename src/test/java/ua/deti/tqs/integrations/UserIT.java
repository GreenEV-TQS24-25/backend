package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.utils.Constants;

import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIT {
    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        cleanDatabase();


        Map<String, Object> body = new HashMap<>();
        body.put("password", "testpassword");
        body.put("name", "testuser");
        body.put("email", "email");
        body.put("role", Role.USER);

        LoginResponse loginResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post("/api/v1/public/user-table")
                        .then()
                        .extract()
                        .jsonPath()
                        .getObject("", LoginResponse.class);
        jwtToken = loginResponse.getToken();
        int userId = loginResponse.getId();

        testUser = userRepository.findById(userId).orElseThrow();

        testUser.setId(loginResponse.getId());
    }

    @AfterEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void whenCreateValidUser_thenReturnCreated() {
        User newUser = new User();
        newUser.setName("Neww User");
        newUser.setEmail("neww@example.com");
        newUser.setPassword("newwPassword123");

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post(Constants.API_V1 + "public/user-table")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("token", notNullValue())
                .body("id", notNullValue());
    }

    @Test
    void whenCreateInvalidUser_thenReturnBadRequest() {
        User invalidUser = new User(); // Missing required fields

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post(Constants.API_V1 + "public/user-table")
                .then()
                .statusCode(400);
    }

    @Test
    void whenUpdateUserWithValidData_thenReturnUpdatedUser() {
        testUser.setName("Updated Name");
        testUser.setEmail("updated@example.com");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(testUser)
                .when()
                .put(Constants.API_V1 + "private/user-table")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"))
                .body("email", equalTo("updated@example.com"));
    }

    @Test
    void whenUpdateUserPassword_thenReturnNewToken() {
        testUser.setPassword("newPassword123");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(testUser)
                .when()
                .put(Constants.API_V1 + "private/user-table")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void whenDeleteExistingUser_thenReturnOk() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/user-table")
                .then()
                .statusCode(200);
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnToken() {
        LoginRequest validCredentials = new LoginRequest("email", "testpassword");

        given()
                .contentType(ContentType.JSON)
                .body(validCredentials)
                .when()
                .post(Constants.API_V1 + "public/user-table/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("id", equalTo(testUser.getId()));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnUnauthorized() {
        LoginRequest invalidCredentials = new LoginRequest("test@example.com", "wrongPassword");

        given()
                .contentType(ContentType.JSON)
                .body(invalidCredentials)
                .when()
                .post(Constants.API_V1 + "public/user-table/login")
                .then()
                .statusCode(401);
    }
}