package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import ua.deti.tqs.entities.*;
import ua.deti.tqs.entities.types.*;
import ua.deti.tqs.repositories.*;
import ua.deti.tqs.utils.Constants;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionIT {

    @LocalServerPort
    private int port;

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ChargingSpotRepository spotRepository;
    @Autowired
    private ChargingStationRepository stationRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private Vehicle vehicle;
    private ChargingSpot spot;
    private Session activeSession;
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

        user = userRepository.findById(userId).orElseThrow();

        user.setId(loginResponse.getId());

        vehicle = new Vehicle();
        vehicle.setBrand("Tesla");
        vehicle.setModel("Model 3");
        vehicle.setUser(user);
        vehicle.setLicensePlate("ABC123");

        vehicle = vehicleRepository.saveAndFlush(vehicle);

        User operator = new User();
        operator.setName("Operator");
        operator.setEmail("operator@example.com");
        operator.setPassword("operatorPass123");
        operator = userRepository.saveAndFlush(operator);

        ChargingStation station = new ChargingStation();
        station.setName("Test Station");
        station.setOperator(operator);
        station.setLat(new BigDecimal("38.711601"));
        station.setLon(new BigDecimal("-9.160241"));
        station = stationRepository.saveAndFlush(station);

        spot = new ChargingSpot();
        spot.setStation(station);
        spot.setPricePerKwh(new BigDecimal("0.45"));
        spot.setPowerKw(new BigDecimal("150.00"));
        spot = spotRepository.saveAndFlush(spot);

        activeSession = new Session();
        activeSession.setVehicle(vehicle);
        activeSession.setChargingSpot(spot);
        activeSession.setStartTime(Instant.now().minus(10, ChronoUnit.MINUTES));
        activeSession.setDuration(30);
        activeSession.setTotalCost(new BigDecimal("0.00"));
        activeSession = sessionRepository.saveAndFlush(activeSession);
    }

    @AfterEach
    void cleanDatabase() {
        sessionRepository.deleteAll();
        spotRepository.deleteAll();
        stationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void whenGetSessionsByUser_thenReturnSessions() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get(Constants.API_V1 + "private/session")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].vehicle.brand", equalTo("Tesla"))
                .body("[0].chargingSpot.powerKw", equalTo(150.00f));
    }

    @Test
    void whenGetSessionsByStation_thenReturnSessions() {
        given()
                .when()
                .header("Authorization", "Bearer " + jwtToken)
                .get(Constants.API_V1 + "private/session/station/" + spot.getStation().getId())
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].uuid", notNullValue());
    }

    @Test
    void whenCreateValidSession_thenReturnCreatedSession() {
        Session newSession = new Session();
        newSession.setChargingSpot(spot);
        newSession.setDuration(30);
        newSession.setVehicle(vehicle);
        newSession.setStartTime(Instant.now());
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(newSession)
                .when()
                .post(Constants.API_V1 + "private/session")
                .then()
                .statusCode(200)
                .body("uuid", notNullValue())
                .body("totalCost", equalTo(new BigDecimal("0.45")
                        .multiply(new BigDecimal("150"))
                        .multiply(new BigDecimal("30"))
                        .divide(new BigDecimal("3600"), RoundingMode.HALF_UP)));
    }

    @Test
    void whenCreateInvalidSession_thenReturnNotFound() {
        Session invalidSession = new Session();
        invalidSession.setChargingSpot(null);
        invalidSession.setVehicle(null);
        invalidSession.setStartTime(Instant.now());
        invalidSession.setDuration(30);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(invalidSession)
                .when()
                .post(Constants.API_V1 + "private/session")
                .then()
                .statusCode(404);
    }

    @Test
    void whenDeleteExistingSession_thenReturnOk() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/session/" + activeSession.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void whenDeleteNonExistingSession_thenReturnNotFound() {
        int invalidId = 9999;

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/session/" + invalidId)
                .then()
                .statusCode(404);
    }

    @Test
    void whenDeleteUnauthorizedSession_thenReturnNotFound() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setName("Other User");
        otherUser.setPassword("otherpassword");
        otherUser = userRepository.saveAndFlush(otherUser);

        Vehicle otherVehicle = new Vehicle();
        otherVehicle.setBrand("Ford");
        otherVehicle.setModel("Mustang");
        otherVehicle.setUser(otherUser);
        otherVehicle.setLicensePlate("XYZ789");
        otherVehicle = vehicleRepository.saveAndFlush(otherVehicle);


        Session otherSession = new Session();
        otherSession.setVehicle(otherVehicle);
        otherSession.setChargingSpot(spot);
        otherSession.setStartTime(Instant.now());
        otherSession.setDuration(30);
        otherSession.setTotalCost(new BigDecimal("0.00"));
        otherSession = sessionRepository.saveAndFlush(otherSession);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(Constants.API_V1 + "private/session/" + otherSession.getId())
                .then()
                .statusCode(404);
    }
}