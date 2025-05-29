package ua.deti.tqs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ua.deti.tqs.entities.*;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.entities.types.SpotState;
import ua.deti.tqs.services.interfaces.SessionService;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.utils.Constants;
import ua.deti.tqs.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @MockitoBean
    private UserService userService;

    private MockedStatic<SecurityUtils> securityUtils;

    private Session testSession;
    private List<Session> testSessions;
    private User testUser;
    private ChargingStation testChargingStation;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        Vehicle testVehicle = new Vehicle();
        testVehicle.setId(1);
        testVehicle.setLicensePlate("AB-12-CD");
        testVehicle.setBrand("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setUser(testUser);

        testChargingStation = new ChargingStation();
        testChargingStation.setId(1);
        testChargingStation.setName("Test Station");
        testChargingStation.setLat(new BigDecimal("40.712776"));
        testChargingStation.setLon(new BigDecimal("-74.005974"));

        ChargingSpot testChargingSpot = new ChargingSpot();
        testChargingSpot.setId(1);
        testChargingSpot.setStation(testChargingStation);
        testChargingSpot.setPowerKw(new BigDecimal("50.00"));
        testChargingSpot.setPricePerKwh(new BigDecimal("0.30"));
        testChargingSpot.setState(SpotState.FREE);

        testSession = new Session();
        testSession.setId(1);
        testSession.setUuid(UUID.randomUUID().toString());
        testSession.setVehicle(testVehicle);
        testSession.setChargingSpot(testChargingSpot);
        testSession.setStartTime(Instant.now());
        testSession.setDuration(30);
        testSession.setTotalCost(new BigDecimal("9.00"));

        Session testSession2 = new Session();
        testSession2.setId(2);
        testSession2.setUuid(UUID.randomUUID().toString());
        testSession2.setVehicle(testVehicle);
        testSession2.setChargingSpot(testChargingSpot);
        testSession2.setStartTime(Instant.now().minusSeconds(3600));
        testSession2.setDuration(45);
        testSession2.setTotalCost(new BigDecimal("13.50"));

        testSessions = Arrays.asList(testSession, testSession2);

        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void whenGetAllSessionsByUserId_thenReturnAllSessions() throws Exception {
        when(sessionService.getAllSessionsByUserId(testUser.getId())).thenReturn(testSessions);

        mockMvc.perform(get("/" + Constants.API_V1 + "private/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testSession.getId())))
                .andExpect(jsonPath("$[0].totalCost", is(9.00)));
    }

    @Test
    void whenGetAllSessionsByUserId_thenReturnNotFound() throws Exception {
        when(sessionService.getAllSessionsByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "private/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetAllSessionsByStationId_thenReturnAllSessions() throws Exception {
        int stationId = testChargingStation.getId();
        when(sessionService.getAllSessionsByStationId(stationId)).thenReturn(testSessions);

        mockMvc.perform(get("/" + Constants.API_V1 + "private/session/station/" + stationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].chargingSpot.station.id", is(stationId)));
    }

    @Test
    void whenGetAllSessionsByStationId_thenReturnNotFound() throws Exception {
        int stationId = 99;
        when(sessionService.getAllSessionsByStationId(stationId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "private/session/station/" + stationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateValidSession_thenReturnCreatedSession() throws Exception {
        when(sessionService.createSession(eq(testUser.getId()), any(Session.class)))
                .thenReturn(testSession);

        mockMvc.perform(post("/" + Constants.API_V1 + "private/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testSession.getId())))
                .andExpect(jsonPath("$.uuid", is(testSession.getUuid())));
    }

    @Test
    void whenCreateInvalidSession_thenReturnNotFound() throws Exception {
        when(sessionService.createSession(eq(testUser.getId()), any(Session.class)))
                .thenReturn(null);

        Session invalidSession = new Session();
        invalidSession.setVehicle(null); // Invalid session without vehicle

        mockMvc.perform(post("/" + Constants.API_V1 + "private/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSession)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteExistingSession_thenReturnOk() throws Exception {
        int sessionId = testSession.getId();
        when(sessionService.deleteSession(testUser.getId(), sessionId)).thenReturn(true);

        mockMvc.perform(delete("/" + Constants.API_V1 + "private/session/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteNonExistingSession_thenReturnNotFound() throws Exception {
        int sessionId = 99;
        when(sessionService.deleteSession(testUser.getId(), sessionId)).thenReturn(false);

        mockMvc.perform(delete("/" + Constants.API_V1 + "private/session/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}