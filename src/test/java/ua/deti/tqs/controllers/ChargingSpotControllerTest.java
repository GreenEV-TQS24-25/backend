package ua.deti.tqs.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.deti.tqs.components.AuthTokenFilter;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.entities.types.Sonic;
import ua.deti.tqs.entities.types.SpotState;
import ua.deti.tqs.services.interfaces.ChargingSpotService;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.utils.Constants;
import ua.deti.tqs.utils.SecurityUtils;

@ActiveProfiles("test")
@WebMvcTest(ChargingSpotController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChargingSpotControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ChargingSpotService chargingSpotService;

  @MockitoBean private UserService userService;

  @MockitoBean private AuthTokenFilter authTokenFilter;

  @MockitoBean private MockedStatic<SecurityUtils> securityUtils;
  private ChargingSpot testSpot;

  private ChargingSpot testSpot2;

  private List<ChargingSpot> testSpots;
  private ChargingStation testStation;
  private User testOperator;

  @BeforeEach
  void setUp() {
    testOperator = new User();
    testOperator.setId(1);
    testOperator.setName("Test Operator");
    testOperator.setEmail("operator@test.com");
    testOperator.setPassword("password");
    testOperator.setRole(Role.OPERATOR);

    testStation = new ChargingStation();
    testStation.setId(1);
    testStation.setName("Test Station");
    testStation.setLat(new BigDecimal("40.7128"));
    testStation.setLon(new BigDecimal("-74.0060"));
    testStation.setOperator(testOperator);
    testStation.setPhotoUrl("https://test.com/photo.jpg");

    testSpot = new ChargingSpot();
    testSpot.setId(1);
    testSpot.setStation(testStation);
    testSpot.setPowerKw(new BigDecimal("50.00"));
    testSpot.setPricePerKwh(new BigDecimal("0.30"));
    testSpot.setChargingVelocity(Sonic.FAST);
    testSpot.setConnectorType(ConnectorType.CCS);
    testSpot.setState(SpotState.FREE);

    testSpot2 = new ChargingSpot();
    testSpot2.setId(2);
    testSpot2.setStation(testStation);
    testSpot2.setPowerKw(new BigDecimal("22.00"));
    testSpot2.setPricePerKwh(new BigDecimal("0.25"));
    testSpot2.setChargingVelocity(Sonic.NORMAL);
    testSpot2.setConnectorType(ConnectorType.SAEJ1772);
    testSpot2.setState(SpotState.OCCUPIED);

    testSpots = Arrays.asList(testSpot, testSpot2);

    securityUtils = mockStatic(SecurityUtils.class);
    securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testOperator);
  }

  @AfterEach
  void tearDown() {
    securityUtils.close();
  }

  @Test
  void whenGetAllChargingSpotsByStationId_withValidId_thenReturnSpots() throws Exception {
    Mockito.when(chargingSpotService.getAllChargingSpotsByStationId(testStation.getId()))
        .thenReturn(testSpots);

    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-spots/{stationId}", testStation.getId())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(testSpot.getId())))
        .andExpect(jsonPath("$[0].powerKw", is(testSpot.getPowerKw().doubleValue())))
        .andExpect(jsonPath("$[1].id", is(testSpot2.getId())));
  }

  @Test
  void whenGetAllChargingSpotsByStationId_withInvalidId_thenReturnNotFound() throws Exception {
    int invalidStationId = 999;
    Mockito.when(chargingSpotService.getAllChargingSpotsByStationId(invalidStationId))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-spots/{stationId}", invalidStationId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCreateChargingSpot_withValidData_thenReturnCreatedSpot() throws Exception {
    Mockito.when(chargingSpotService.createChargingSpot(anyInt(), any(ChargingSpot.class)))
        .thenReturn(testSpot);

    when(userService.createUser(any(User.class))).thenReturn(testOperator);
    when(userService.loginUser(any()))
        .thenReturn(
            new LoginResponse(
                testOperator.getId(),
                testOperator.getName(),
                testOperator.getEmail(),
                testOperator.getRole(),
                "112",
                112L));
    mockMvc
        .perform(
            post("/" + Constants.API_V1 + "private/charging-spots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSpot)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(testSpot.getId())))
        .andExpect(jsonPath("$.powerKw", is(testSpot.getPowerKw().doubleValue())))
        .andExpect(jsonPath("$.connectorType", is(testSpot.getConnectorType().toString())));
  }

  @Test
  void whenCreateChargingSpot_withInvalidOperatorId_thenReturnNotFound() throws Exception {
    int invalidOperatorId = 999;
    Mockito.when(chargingSpotService.createChargingSpot(invalidOperatorId, testSpot))
        .thenReturn(null);

    mockMvc
        .perform(
            post("/" + Constants.API_V1 + "private/charging-spots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSpot)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenUpdateChargingSpot_withValidData_thenReturnUpdatedSpot() throws Exception {
    ChargingSpot updatedSpot = testSpot;
    updatedSpot.setPowerKw(new BigDecimal("75.00"));
    updatedSpot.setPricePerKwh(new BigDecimal("0.35"));

    Mockito.when(chargingSpotService.updateChargingSpot(anyInt(), any(ChargingSpot.class)))
        .thenReturn(updatedSpot);

    mockMvc
        .perform(
            put("/" + Constants.API_V1 + "private/charging-spots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedSpot)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.powerKw", is(updatedSpot.getPowerKw().doubleValue())))
        .andExpect(jsonPath("$.pricePerKwh", is(updatedSpot.getPricePerKwh().doubleValue())));
  }

  @Test
  void whenUpdateChargingSpot_withInvalidOperatorId_thenReturnNotFound() throws Exception {
    int invalidOperatorId = 999;
    Mockito.when(chargingSpotService.updateChargingSpot(invalidOperatorId, testSpot))
        .thenReturn(null);

    mockMvc
        .perform(
            put("/" + Constants.API_V1 + "private/charging-spots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSpot)))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenDeleteChargingSpot_withValidIds_thenReturnOk() throws Exception {
    Mockito.when(chargingSpotService.deleteChargingSpot(anyInt(), anyInt())).thenReturn(true);

    mockMvc
        .perform(
            delete("/" + Constants.API_V1 + "private/charging-spots/" + testSpot.getId())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void whenDeleteChargingSpot_withInvalidIds_thenReturnNotFound() throws Exception {
    int invalidId = 999;
    int invalidOperatorId = 999;
    Mockito.when(chargingSpotService.deleteChargingSpot(invalidId, invalidOperatorId))
        .thenReturn(false);

    mockMvc
        .perform(
            delete("/" + Constants.API_V1 + "private/charging-spots/" + invalidId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenUpdateChargingSpotStatus_withValidStatusByOperator_thenReturnOk() throws Exception {

    securityUtils.when(SecurityUtils::getAuthenticatedUserId).thenReturn(testOperator.getId());
    Mockito.when(chargingSpotService.updateChargingSpotStatus(
            testSpot.getId(),
            SpotState.OUT_OF_SERVICE,
            testOperator.getId()
    )).thenReturn(true);

    mockMvc.perform(
            put("/" + Constants.API_V1 + "private/charging-spots/status/" + testSpot.getId())
                    .param("status", "OUT_OF_SERVICE")
                    .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk());
  }

  @Test
  void whenUpdateChargingSpotStatus_withInvalidStatus_thenReturnBadRequest() throws Exception {
    mockMvc.perform(
            put("/" + Constants.API_V1 + "private/charging-spots/status/" + testSpot.getId())
                    .param("status", "OUT_OF_SERVICE")
                    .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound());
  }

  @Test
  void whenUpdateChargingSpotStatus_SpotNotFound_thenReturnNotFound() throws Exception {
    Mockito.when(chargingSpotService.updateChargingSpotStatus(
            testSpot.getId(),
            SpotState.OCCUPIED,
            testOperator.getId()
    )).thenReturn(false);

    mockMvc.perform(
            put("/" + Constants.API_V1 + "private/charging-spots/status/" + testSpot.getId())
                    .param("status", "OCCUPIED")
                    .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isNotFound());
  }

}
