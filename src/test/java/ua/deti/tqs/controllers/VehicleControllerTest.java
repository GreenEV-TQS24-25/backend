package ua.deti.tqs.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.services.interfaces.VehicleService;
import ua.deti.tqs.utils.Constants;
import ua.deti.tqs.utils.SecurityUtils;

@ActiveProfiles("test")
@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @MockitoBean
    private UserService userService;

    private MockedStatic<SecurityUtils> securityUtils;

    private Vehicle testVehicle;
    private List<Vehicle> testVehicles;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        testVehicle = new Vehicle();
        testVehicle.setId(1);
        testVehicle.setLicensePlate("AB-12-CD");
        testVehicle.setBrand("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setUser(testUser);

        Vehicle testVehicle2 = new Vehicle();
        testVehicle2.setId(2);
        testVehicle2.setLicensePlate("EF-34-GH");
        testVehicle2.setBrand("Nissan");
        testVehicle2.setModel("Leaf");
        testVehicle2.setUser(testUser);

        testVehicles = Arrays.asList(testVehicle, testVehicle2);

        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getAuthenticatedUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void whenGetAllVehiclesByUserId_thenReturnAllVehicles() throws Exception {
        when(vehicleService.getAllVehiclesByUserId(testUser.getId())).thenReturn(testVehicles);

        mockMvc.perform(get("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].licensePlate", is(testVehicle.getLicensePlate())));
    }

    @Test
    void whenGetAllVehiclesByUserId_thenReturnNotFound() throws Exception {
        when(vehicleService.getAllVehiclesByUserId(testUser.getId())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateValidVehicle_thenReturnCreatedVehicle() throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class), eq(testUser.getId())))
                .thenReturn(testVehicle);

        mockMvc.perform(post("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate", is(testVehicle.getLicensePlate())));
    }

    @Test
    void whenCreateInvalidVehicle_thenReturnBadRequest() throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class), eq(testUser.getId())))
                .thenReturn(null);

        Vehicle invalidVehicle = new Vehicle();
        invalidVehicle.setLicensePlate(""); // Invalid license plate

        mockMvc.perform(post("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVehicle)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdateValidVehicle_thenReturnUpdatedVehicle() throws Exception {
        when(vehicleService.updateVehicle(eq(testUser.getId()), any(Vehicle.class)))
                .thenReturn(testVehicle);

        mockMvc.perform(put("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate", is(testVehicle.getLicensePlate())));
    }

    @Test
    void whenUpdateInvalidVehicle_thenReturnNotFound() throws Exception {
        when(vehicleService.updateVehicle(eq(testUser.getId()), any(Vehicle.class)))
                .thenReturn(null);

        Vehicle invalidVehicle = new Vehicle();
        invalidVehicle.setLicensePlate(""); // Invalid license plate

        mockMvc.perform(put("/" + Constants.API_V1 + "private/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVehicle)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteExistingVehicle_thenReturnOk() throws Exception {
        int vehicleId = 1;
        when(vehicleService.deleteVehicle(vehicleId, testUser.getId())).thenReturn(true);

        mockMvc.perform(delete("/" + Constants.API_V1 + "private/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteNonExistingVehicle_thenReturnNotFound() throws Exception {
        int vehicleId = 99;
        when(vehicleService.deleteVehicle(vehicleId, testUser.getId())).thenReturn(false);

        mockMvc.perform(delete("/" + Constants.API_V1 + "private/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}