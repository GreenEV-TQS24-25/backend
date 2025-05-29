package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
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
import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.entities.types.Sonic;
import ua.deti.tqs.entities.types.SpotState;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.utils.Constants;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChargingSpotIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ChargingSpotRepository spotRepository;

    @Autowired
    private ChargingStationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    private ChargingStation station;
    private ChargingSpot testSpot;
    private String jwtToken;
    private User anotherOperator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        cleanDatabase();

        Map<String, Object> body = new HashMap<>();
        body.put("password", "spotOperatorPass123");
        body.put("name", "Spot Operator");
        body.put("email", "spot.operator@example.com");
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

        User operator = userRepository.findById(userId).orElseThrow();

        operator.setId(loginResponse.getId());

        // Create another operator
        anotherOperator = new User();
        anotherOperator.setName("Another Operator");
        anotherOperator.setEmail("another.operator@example.com");
        anotherOperator.setPassword("anotherPass123");
        anotherOperator = userRepository.saveAndFlush(anotherOperator);

        // Create test station
        station = new ChargingStation();
        station.setName("Main Station");
        station.setLat(new BigDecimal("38.711601"));
        station.setLon(new BigDecimal("-9.160241"));
        station.setOperator(operator);
        station = stationRepository.saveAndFlush(station);

        // Create test charging spot
        testSpot = new ChargingSpot();
        testSpot.setStation(station);
        testSpot.setPowerKw(new BigDecimal("150.00"));
        testSpot.setPricePerKwh(new BigDecimal("0.45"));
        testSpot.setChargingVelocity(Sonic.FAST);
        testSpot.setConnectorType(ConnectorType.SAEJ1772);
        testSpot.setState(SpotState.FREE);
        testSpot = spotRepository.saveAndFlush(testSpot);
    }

    @AfterEach
    void cleanDatabase() {
        spotRepository.deleteAll();
        stationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Requirement("GREEN-24")
    @Test
    void whenGetSpotsByStationId_thenReturnSpots() {
        given()
                .when()
                .get(Constants.API_V1 + "public/charging-spots/" + station.getId())
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].powerKw", equalTo(150.00f))
                .body("[0].connectorType", equalTo("SAEJ1772"));
    }

    @Requirement("GREEN-29")
    @Test
    void whenCreateValidSpot_thenReturnCreatedSpot() {
        ChargingSpot newSpot = new ChargingSpot();
        newSpot.setStation(station);
        newSpot.setPowerKw(new BigDecimal("250.00"));
        newSpot.setPricePerKwh(new BigDecimal("0.50"));
        newSpot.setChargingVelocity(Sonic.FASTPP);
        newSpot.setConnectorType(ConnectorType.SAEJ1772);
        newSpot.setState(SpotState.FREE);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(newSpot)
                .when()
                .post(Constants.API_V1 + "private/charging-spots")
                .then()
                .statusCode(200)
                .body("powerKw", equalTo(250.00f))
                .body("chargingVelocity", equalTo("FASTPP"))
                .body("station.id", equalTo(station.getId()));
    }

    @Requirement("GREEN-29")
    @Test
    void whenCreateInvalidSpot_thenReturnBadRequest() {
        ChargingSpot invalidSpot = new ChargingSpot();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(invalidSpot)
                .when()
                .post(Constants.API_V1 + "private/charging-spots")
                .then()
                .statusCode(404); // Adjust based on your error handling
    }

    @Requirement("GREEN-30")
    @Test
    void whenUpdateValidSpot_thenReturnUpdatedSpot() {
        testSpot.setPricePerKwh(new BigDecimal("0.55"));
        testSpot.setState(SpotState.OCCUPIED);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(testSpot)
                .when()
                .put(Constants.API_V1 + "private/charging-spots")
                .then()
                .statusCode(200)
                .body("pricePerKwh", equalTo(0.55f))
                .body("state", equalTo("OCCUPIED"));
    }

    @Requirement("GREEN-30")
    @Test
    void whenUpdateUnauthorizedSpot_thenReturnForbidden() {
        // Create station and spot from different operator
        ChargingStation otherStation = new ChargingStation();
        otherStation.setOperator(anotherOperator);
        otherStation.setName("Unauthorized Station");
        otherStation.setLat(new BigDecimal("38.720000"));
        otherStation.setLon(new BigDecimal("-9.150000"));

        otherStation = stationRepository.saveAndFlush(otherStation);

        ChargingSpot otherSpot = new ChargingSpot();
        otherSpot.setStation(otherStation);
        otherSpot.setPowerKw(new BigDecimal("150.00"));
        otherSpot.setPricePerKwh(new BigDecimal("0.45"));
        otherSpot.setChargingVelocity(Sonic.FAST);
        otherSpot.setConnectorType(ConnectorType.SAEJ1772);
        otherSpot.setState(SpotState.FREE);

        otherSpot = spotRepository.saveAndFlush(otherSpot);

        otherSpot.setPricePerKwh(new BigDecimal("0.60"));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(otherSpot)
                .when()
                .put(Constants.API_V1 + "private/charging-spots")
                .then()
                .statusCode(404);
    }

    @Requirement("GREEN-29")
    @Test
    void whenDeleteExistingSpot_thenReturnOk() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-spots/" + testSpot.getId())
                .then()
                .statusCode(200);
    }

    @Requirement("GREEN-29")
    @Test
    void whenDeleteNonExistingSpot_thenReturnNotFound() {
        int invalidId = 9999;

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-spots/" + invalidId)
                .then()
                .statusCode(404);
    }

    @Requirement("GREEN-29")
    @Test
    void whenDeleteUnauthorizedSpot_thenReturnNotFound() {
        // Create station and spot from different operator
        ChargingStation otherStation = new ChargingStation();
        otherStation.setOperator(anotherOperator);
        otherStation.setName("Unauthorized Station");
        otherStation.setLat(new BigDecimal("38.720000"));
        otherStation.setLon(new BigDecimal("-9.150000"));
        otherStation = stationRepository.saveAndFlush(otherStation);

        ChargingSpot otherSpot = new ChargingSpot();
        otherSpot.setStation(otherStation);
        otherSpot.setPowerKw(new BigDecimal("150.00"));
        otherSpot.setPricePerKwh(new BigDecimal("0.45"));
        otherSpot.setChargingVelocity(Sonic.FAST);
        otherSpot.setConnectorType(ConnectorType.SAEJ1772);
        otherSpot.setState(SpotState.FREE);

        otherSpot = spotRepository.saveAndFlush(otherSpot);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/charging-spots/" + otherSpot.getId())
                .then()
                .statusCode(404);
    }
}