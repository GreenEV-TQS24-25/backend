package ua.deti.tqs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.services.interfaces.ChargingStationService;
import ua.deti.tqs.utils.Constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ChargingStationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChargingStationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChargingStationService chargingStationService;

    private ChargingStation testStation;
    private List<ChargingStation> testStations;

    @BeforeEach
    void setUp() {
        UserTable operator = new UserTable();
        operator.setId(1);

        testStation = new ChargingStation();
        testStation.setId(1);
        testStation.setName("Test Station");
        testStation.setLat(new BigDecimal("40.712776"));
        testStation.setLon(new BigDecimal("-74.005974"));
        testStation.setOperator(operator);
        testStation.setLastMaintenance(LocalDate.now());
        testStation.setPhotoUrl("http://example.com/photo.jpg");

        ChargingStation testStation2 = new ChargingStation();
        testStation2.setId(2);
        testStation2.setName("Test Station 2");
        testStation2.setLat(new BigDecimal("41.878113"));
        testStation2.setLon(new BigDecimal("-87.629799"));
        testStation2.setOperator(operator);

        testStations = Arrays.asList(testStation, testStation2);
    }

    @Test
    void whenGetAllChargingStations_thenReturnAllStations() throws Exception {
        when(chargingStationService.getAllChargingStations()).thenReturn(testStations);

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(testStation.getName())));
    }

    @Test
    void whenGetAllChargingStations_thenReturnEmptyList() throws Exception {
        when(chargingStationService.getAllChargingStations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetAllChargingStationsByOperatorId_thenReturnStations() throws Exception {
        int operatorId = 1;
        when(chargingStationService.getAllChargingStationsByOperatorId(operatorId)).thenReturn(testStations);

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/all/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operator.id", is(operatorId)));
    }

    @Test
    void whenGetAllChargingStationsByOperatorId_thenReturnNotFound() throws Exception {
        int operatorId = 99;
        when(chargingStationService.getAllChargingStationsByOperatorId(operatorId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/all/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateChargingStation_thenReturnCreatedStation() throws Exception {
        when(chargingStationService.createChargingStation(any(ChargingStation.class))).thenReturn(testStation);

        mockMvc.perform(post("/" + Constants.API_V1 + "charging-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(testStation.getName())));
    }

    @Test
    void whenCreateInvalidChargingStation_thenReturnBadRequest() throws Exception {
        when(chargingStationService.createChargingStation(any(ChargingStation.class))).thenReturn(null);

        ChargingStation invalidStation = new ChargingStation();
        invalidStation.setName(""); // Invalid name

        mockMvc.perform(post("/" + Constants.API_V1 + "charging-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStation)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUpdateChargingStation_thenReturnUpdatedStation() throws Exception {
        int operatorId = 1;
        when(chargingStationService.updateChargingStation(eq(operatorId), any(ChargingStation.class))).thenReturn(testStation);

        mockMvc.perform(put("/" + Constants.API_V1 + "charging-stations/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(testStation.getName())));
    }

    @Test
    void whenUpdateInvalidChargingStation_thenReturnBadRequest() throws Exception {
        int operatorId = 1;
        when(chargingStationService.updateChargingStation(eq(operatorId), any(ChargingStation.class))).thenReturn(null);

        ChargingStation invalidStation = new ChargingStation();
        invalidStation.setName(""); // Invalid name

        mockMvc.perform(put("/" + Constants.API_V1 + "charging-stations/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStation)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenDeleteExistingChargingStation_thenReturnOk() throws Exception {
        int stationId = 1;
        int operatorId = 1;
        when(chargingStationService.deleteChargingStation(stationId, operatorId)).thenReturn(true);

        mockMvc.perform(delete("/" + Constants.API_V1 + "charging-stations/" + stationId + "/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteNonExistingChargingStation_thenReturnNotFound() throws Exception {
        int stationId = 99;
        int operatorId = 99;
        when(chargingStationService.deleteChargingStation(stationId, operatorId)).thenReturn(false);

        mockMvc.perform(delete("/" + Constants.API_V1 + "charging-stations/" + stationId + "/" + operatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetFilteredChargingStations_thenReturnStations() throws Exception {
        int operatorId = 1;
        List<Integer> operatorIds = List.of(1, 2);
        when(chargingStationService.getFilteredChargingStations(operatorIds)).thenReturn(testStations);

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/filtered?operatorIds=1&operatorIds=2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operator.id", is(operatorId)));
    }

    @Test
    void whenGetFilteredChargingStations_thenReturnEmptyList() throws Exception {
        List<Integer> operatorIds = List.of(3, 4, 5);
        when(chargingStationService.getFilteredChargingStations(operatorIds)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "charging-stations/filtered?operatorIds=3&operatorIds=4&operatorIds=5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}