package ua.deti.tqs.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.deti.tqs.components.AuthTokenFilter;
import ua.deti.tqs.dto.StationsSpots;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.services.interfaces.ChargingStationService;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.utils.Constants;
import ua.deti.tqs.utils.SecurityUtils;

@ActiveProfiles("test")
@WebMvcTest(ChargingStationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChargingStationControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ChargingStationService chargingStationService;

  @MockitoBean private AuthTokenFilter authTokenFilter;

  private ChargingStation testStation;
  private List<ChargingStation> testStations;

  @MockitoBean private UserService userService;

  @MockitoBean private MockedStatic<SecurityUtils> securityUtils;

  @BeforeEach
  void setUp() {
    User operator = new User();
    operator.setId(1);
    operator.setName("Test Operator");
    operator.setEmail("Test email");
    operator.setPassword("password");
    operator.setRole(Role.OPERATOR);

    testStation = new ChargingStation();
    testStation.setId(1);
    testStation.setName("Test Station");
    testStation.setLat(new BigDecimal("40.712776"));
    testStation.setLon(new BigDecimal("-74.005974"));
    testStation.setOperator(operator);
    testStation.setPhotoUrl("https://example.com/photo.jpg");

    ChargingStation testStation2 = new ChargingStation();
    testStation2.setId(2);
    testStation2.setName("Test Station 2");
    testStation2.setLat(new BigDecimal("41.878113"));
    testStation2.setLon(new BigDecimal("-87.629799"));
    testStation2.setOperator(operator);

    testStations = Arrays.asList(testStation, testStation2);

    securityUtils = mockStatic(SecurityUtils.class);
    securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(operator);
  }

  @AfterEach
  void tearDown() {
    securityUtils.close();
  }

  @Test
  void whenGetAllChargingStations_thenReturnAllStations() throws Exception {
    ChargingStation station1 = new ChargingStation();
    station1.setId(1);
    station1.setName("Station 1");

    ChargingStation station2 = new ChargingStation();
    station2.setId(2);
    station2.setName("Station 2");

    ChargingSpot spot1 = new ChargingSpot();
    spot1.setId(1);

    ChargingSpot spot2 = new ChargingSpot();
    spot2.setId(2);

    StationsSpots stationsSpots1 = new StationsSpots(station1, List.of(spot1));
    StationsSpots stationsSpots2 = new StationsSpots(station2, List.of(spot2));
    List<StationsSpots> testStationsSpots = List.of(stationsSpots1, stationsSpots2);

    when(chargingStationService.getAllChargingStations()).thenReturn(testStationsSpots);

    mockMvc
            .perform(
                    get("/" + Constants.API_V1 + "public/charging-stations/all")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].chargingStation.name", is("Station 1")))
            .andExpect(jsonPath("$[0].spots[0].id", is(1)))
            .andExpect(jsonPath("$[0].chargingStation.operator").doesNotExist());
    }

  @Test
  void whenGetAllChargingStations_thenReturnEmptyList() throws Exception {
    when(chargingStationService.getAllChargingStations()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-stations/all")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_thenReturnStations() throws Exception {
    int operatorId = 1;

    StationsSpots stationsSpots1 = new StationsSpots(testStations.get(0), Collections.emptyList());
    StationsSpots stationsSpots2 = new StationsSpots(testStations.get(1), Collections.emptyList());
    List<StationsSpots> testStationsSpots = Arrays.asList(stationsSpots1, stationsSpots2);

    when(chargingStationService.getAllChargingStationsByOperatorId(operatorId))
            .thenReturn(testStationsSpots);

    mockMvc
            .perform(
                    get("/" + Constants.API_V1 + "private/charging-stations")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].chargingStation.operator.id", is(operatorId)));
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_thenReturnNotFound() throws Exception {
    int operatorId = 99;
    when(chargingStationService.getAllChargingStationsByOperatorId(operatorId))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "private/charging-stations")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCreateChargingStation_thenReturnCreatedStation() throws Exception {
    when(chargingStationService.createChargingStation(
            any(ChargingStation.class), any(Integer.class)))
        .thenReturn(testStation);

    mockMvc
        .perform(
            post("/" + Constants.API_V1 + "private/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testStation)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is(testStation.getName())));
  }

  @Test
  void whenCreateInvalidChargingStation_thenReturnBadRequest() throws Exception {
    when(chargingStationService.createChargingStation(
            any(ChargingStation.class), any(Integer.class)))
        .thenReturn(null);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("");

    mockMvc
        .perform(
            post("/" + Constants.API_V1 + "private/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidStation)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenUpdateChargingStation_thenReturnUpdatedStation() throws Exception {
    int operatorId = 1;
    when(chargingStationService.updateChargingStation(eq(operatorId), any(ChargingStation.class)))
        .thenReturn(testStation);

    mockMvc
        .perform(
            put("/" + Constants.API_V1 + "private/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testStation)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is(testStation.getName())));
  }

  @Test
  void whenUpdateInvalidChargingStation_thenReturnBadRequest() throws Exception {
    int operatorId = 1;
    when(chargingStationService.updateChargingStation(eq(operatorId), any(ChargingStation.class)))
        .thenReturn(null);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("");

    mockMvc
        .perform(
            put("/" + Constants.API_V1 + "private/charging-stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidStation)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenDeleteExistingChargingStation_thenReturnOk() throws Exception {
    int stationId = 1;
    int operatorId = 1;
    when(chargingStationService.deleteChargingStation(stationId, operatorId)).thenReturn(true);

    mockMvc
        .perform(
            delete("/" + Constants.API_V1 + "private/charging-stations/" + stationId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void whenDeleteNonExistingChargingStation_thenReturnNotFound() throws Exception {
    int stationId = 99;
    int operatorId = 99;
    when(chargingStationService.deleteChargingStation(stationId, operatorId)).thenReturn(false);
    mockMvc
        .perform(
            delete("/" + Constants.API_V1 + "private/charging-stations/" + stationId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFilterChargingStations_returnMatchingStations() throws Exception {
    List<ConnectorType> connectorTypes = List.of(ConnectorType.CCS, ConnectorType.CHADEMO);
    when(chargingStationService.filterChargingStations(connectorTypes)).thenReturn(testStations);
    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-stations/filter?connectorTypeInputs=ccs&connectorTypeInputs=chademo")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is(testStation.getName())));
  }

  @Test
  void whenFilterChargingStations_withInvalidConnectorType_thenReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-stations/filter?connectorTypeInputs=ccs&connectorTypeInputs=abc")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenFilterChargingStations_thenReturnEmptyList() throws Exception {
    when(chargingStationService.filterChargingStations(anyList())).thenReturn(Collections.emptyList());
    mockMvc
        .perform(
            get("/" + Constants.API_V1 + "public/charging-stations/filter?connectorTypeInputs=mennekes")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
