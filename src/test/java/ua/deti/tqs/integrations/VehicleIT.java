package ua.deti.tqs.integrations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

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
            .post("/api/v1/public/user-table")
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

    //        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
    //            utilities.when(SecurityUtils::getAuthenticatedUser).thenReturn(user);
    System.out.println("JWT Token: " + jwtToken);

    given()
        .header("Authorization", "Bearer " + jwtToken)
        .when()
        .get("/" + Constants.API_V1 + "private/vehicles")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("", hasSize(1));
  }
  //    }
}
