package ua.deti.tqs.integrations;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ua.deti.tqs.components.AuthTokenFilter;
import ua.deti.tqs.components.JwtUtils;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.repositories.VehicleRepository;
import ua.deti.tqs.utils.Constants;
import ua.deti.tqs.utils.SecurityUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mockStatic;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class VehicleIT {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.username", postgres::getUsername);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private Vehicle vehicle;
    private User user;

    String BASE_URL;
    private String jwtToken;

    @BeforeEach
    void setup() {

        RestAssured.port = port;


        cleanDatabase();

        user = new User();
        user.setId(1);
        user.setName("testuser");
        user.setPassword("testpassword");
        user.setEmail("email");
//        user.setRole(Role.OPERATOR);


        Map<String, Object> body = new HashMap<>();
        body.put("password", "testpassword");
        body.put("name", "testuser");
        body.put("email", "email");
        body.put("id", 1);
        //body.put("role", Role.USER);

        LoginResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/v1/public/user-table")
                .then().extract().jsonPath().getObject("", LoginResponse.class);

        jwtToken = loginResponse.getToken();
        int userId = loginResponse.getId();
        user.setId(userId);


        vehicle = new Vehicle();
        vehicle.setId(1);
        vehicle.setBrand("Brand");
        vehicle.setModel("Model");
        vehicle.setLicensePlate("ABC123");
        vehicle.setUser(user);
    }

    @AfterEach
    void cleanDatabase() {
        userRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void whenGetAllVehiclesByUserId_ThenReturnListWithVehicles() {

//        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
//            utilities.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
        System.out.println("JWT Token: " + jwtToken);


        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/" + Constants.API_V1 + "/private/vehicles")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(1))
                .body("", hasSize(1));
    }
//    }
}
