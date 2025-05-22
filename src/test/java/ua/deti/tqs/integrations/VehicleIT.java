package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.repositories.VehicleRepository;
import ua.deti.tqs.utils.Constants;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class VehicleIT {
  @LocalServerPort private int port;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private UserRepository userRepository;
  private Vehicle vehicle;
  private User user;
  private String jwtToken;

  @BeforeEach
  void setup() {
    cleanDatabase();
    RestAssured.port = port;

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
            .post("/" + Constants.API_V1 + "public/user-table")
            .then()
            .extract()
            .jsonPath()
            .getObject("", LoginResponse.class);
    jwtToken = loginResponse.getToken();
    int userId = loginResponse.getId();

    user = userRepository.findById(userId).orElseThrow();

    vehicle = new Vehicle();
    vehicle.setBrand("Brand");
    vehicle.setModel("Model");
    vehicle.setLicensePlate("ABC123");
    vehicle.setConnectorType(ConnectorType.SAEJ1772);
    vehicle.setUser(user);

    vehicleRepository.saveAndFlush(vehicle);
  }

  @AfterEach
  void cleanDatabase() {
    userRepository.deleteAll();
    vehicleRepository.deleteAll();
  }

  @Test
  void whenGetAllVehiclesByUserId_ThenReturnListWithVehicles() {
    given()
        .header("Authorization", "Bearer " + jwtToken)
        .when()
        .get("/" + Constants.API_V1 + "private/vehicles")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("[0].brand", equalTo(vehicle.getBrand()));
  }

  @Test
  void whenCreateValidVehicle_ThenReturnCreatedVehicle() {
    Vehicle newVehicle = new Vehicle();
    newVehicle.setBrand("BRAND");
    newVehicle.setModel("Model S");
    newVehicle.setLicensePlate("BRAND123");
    newVehicle.setConnectorType(ConnectorType.CCS);

    given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(newVehicle)
            .when()
            .post("/" + Constants.API_V1 + "private/vehicles")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("brand", equalTo(newVehicle.getBrand()))
            .body("model", equalTo(newVehicle.getModel()))
            .body("licensePlate", equalTo(newVehicle.getLicensePlate()))
            .body("connectorType", equalTo(newVehicle.getConnectorType().name()));
  }

  @Test
  void whenUpdateExistingVehicle_ThenReturnUpdatedVehicle() {

    vehicle.setBrand("UpdatedBrand");
    vehicle.setModel("UpdatedModel");
    vehicle.setLicensePlate("UPDATED123");
    vehicle.setId(vehicleRepository.findAllByUser_Id(user.getId()).orElseThrow().getFirst().getId());

    given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(vehicle)
            .when()
            .put("/" + Constants.API_V1 + "private/vehicles")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("brand", equalTo(vehicle.getBrand()))
            .body("model", equalTo(vehicle.getModel()))
            .body("licensePlate", equalTo(vehicle.getLicensePlate()));
  }

  @Test
  void whenDeleteExistingVehicle_ThenReturnOk() {
    given()
            .header("Authorization", "Bearer " + jwtToken)
            .when()
            .delete("/" + Constants.API_V1 + "private/vehicles/" + vehicle.getId())
            .then()
            .statusCode(200);
  }

  @Test
  void whenDeleteNonExistingVehicle_ThenReturnNotFound() {
    int invalidVehicleId = 9999;

    given()
            .header("Authorization", "Bearer " + jwtToken)
            .when()
            .delete("/" + Constants.API_V1 + "private/vehicles/" + invalidVehicleId)
            .then()
            .statusCode(404);
  }
}