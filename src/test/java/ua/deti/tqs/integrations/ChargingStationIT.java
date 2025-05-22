package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.utils.Constants;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChargingStationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private UserRepository userRepository;

    private User operator;
    private ChargingStation testStation;
    private String jwtToken;
    private User anotherOperator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        cleanDatabase();

        // Create another operator
        anotherOperator = new User();
        anotherOperator.setName("Another Operator");
        anotherOperator.setEmail("another@example.com");
        anotherOperator.setPassword("anotherPass123");
        anotherOperator = userRepository.saveAndFlush(anotherOperator);


        Map<String, Object> body = new HashMap<>();
        body.put("name", "Station Operator");
        body.put("email", "operator@example.com");
        body.put("password", "operatorPass123");
        body.put("role", Role.OPERATOR);

        LoginResponse loginResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .when()
                        .post("/"+ Constants.API_V1 + "public/user-table")
                        .then()
                        .extract()
                        .jsonPath()
                        .getObject("", LoginResponse.class);
        jwtToken = loginResponse.getToken();
        int userId = loginResponse.getId();

        operator = userRepository.findById(userId).orElseThrow();

        // Create test charging station
        testStation = new ChargingStation();
        testStation.setName("Main Station");
        testStation.setLat(new BigDecimal("38.711601"));
        testStation.setLon(new BigDecimal("-9.160241"));
        testStation.setOperator(operator);
        testStation = chargingStationRepository.saveAndFlush(testStation);
    }

    @AfterEach
    void cleanDatabase() {
        chargingStationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void whenGetAllChargingStations_thenReturnAllStations() {
        // Create additional station from different operator
        ChargingStation otherStation = new ChargingStation();
        otherStation.setName("Other Station");
        otherStation.setLat(new BigDecimal("38.720000"));
        otherStation.setLon(new BigDecimal("-9.150000"));
        otherStation.setOperator(anotherOperator);
        chargingStationRepository.saveAndFlush(otherStation);

        given()
                .when()
                .get(Constants.API_V1 + "public/charging-stations/all")
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("name", containsInAnyOrder("Main Station", "Other Station"));
    }

    @Test
    void whenGetStationsByOperator_thenReturnOnlyOwnedStations() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(Constants.API_V1 + "private/charging-stations")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("Main Station"));
    }

    @Test
    void whenCreateValidStation_thenReturnCreatedStation() {
        ChargingStation newStation = new ChargingStation();
        newStation.setName("New Station");
        newStation.setLat(new BigDecimal("38.715000"));
        newStation.setLon(new BigDecimal("-9.165000"));
        newStation.setOperator(operator);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(newStation)
                .when()
                .post(Constants.API_V1 + "private/charging-stations")
                .then()
                .statusCode(201)
                .body("name", equalTo("New Station"))
                .body("operator.id", equalTo(operator.getId()));
    }

    @Test
    void whenCreateInvalidStation_thenReturnBadRequest() {
        ChargingStation invalidStation = new ChargingStation(); // Missing required fields

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(invalidStation)
                .when()
                .post(Constants.API_V1 + "private/charging-stations")
                .then()
                .statusCode(400);
    }

    @Test
    void whenUpdateValidStation_thenReturnUpdatedStation() {
        testStation.setName("Updated Station");
        testStation.setLat(new BigDecimal("38.716000"));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(testStation)
                .when()
                .put(Constants.API_V1 + "private/charging-stations")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Station"))
                .body("lat", equalTo(38.716000f));
    }

    @Test
    void whenUpdateUnauthorizedStation_thenReturnBadRequest() {
        // Create station from different operator
        ChargingStation otherStation = new ChargingStation();
        otherStation.setName("Unauthorized Station");
        otherStation.setLat(new BigDecimal("38.720000"));
        otherStation.setLon(new BigDecimal("-9.150000"));
        otherStation.setOperator(anotherOperator);
        otherStation = chargingStationRepository.saveAndFlush(otherStation);

        otherStation.setName("Modified Name");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(otherStation)
                .when()
                .put(Constants.API_V1 + "private/charging-stations")
                .then()
                .statusCode(400);
    }

    @Test
    void whenDeleteExistingStation_thenReturnOk() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-stations/" + testStation.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void whenDeleteNonExistingStation_thenReturnNotFound() {
        int invalidId = 9999;

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-stations/" + invalidId)
                .then()
                .statusCode(404);
    }

    @Test
    void whenDeleteUnauthorizedStation_thenReturnNotFound() {
        // Create station from different operator
        ChargingStation otherStation = new ChargingStation();
        otherStation.setName("Unauthorized Station");
        otherStation.setOperator(anotherOperator);
        otherStation.setLat(new BigDecimal("38.720000"));
        otherStation.setLon(new BigDecimal("-9.150000"));
        otherStation = chargingStationRepository.saveAndFlush(otherStation);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-stations/" + otherStation.getId())
                .then()
                .statusCode(404);
    }
}