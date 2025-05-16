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
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.services.interfaces.UserTableService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(UserTableController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserTableService userTableService;

    private UserTable testUser;
    private UserTable testUserInvalid;

    @BeforeEach
    void setUp() {
        testUser = new UserTable();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        testUserInvalid = new UserTable();
        testUserInvalid.setId(2);
        testUserInvalid.setName("");
        testUserInvalid.setEmail("");
        testUserInvalid.setPassword("");
        testUserInvalid.setRole(null);
    }

    @Test
    void whenGetUserById_thenReturnUser() throws Exception {
        when(userTableService.getUserById(testUser.getId())).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/user-table/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        verify(userTableService, times(1)).getUserById(testUser.getId());
    }

    @Test
    void whenGetUserByIdWithInvalidId_thenReturnNotFound() throws Exception {
        int invalidId = 999;
        when(userTableService.getUserById(invalidId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/user-table/{userId}", invalidId))
                .andExpect(status().isNotFound());

        verify(userTableService, times(1)).getUserById(invalidId);
    }

    @Test
    void whenCreateValidUser_thenReturnCreatedUser() throws Exception {
        when(userTableService.createUser(any(UserTable.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/user-table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        verify(userTableService, times(1)).createUser(any(UserTable.class));
    }

    @Test
    void whenCreateInvalidUser_thenReturnBadRequest() throws Exception {
        when(userTableService.createUser(any(UserTable.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/user-table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserInvalid)))
                .andExpect(status().isBadRequest());

        verify(userTableService, times(1)).createUser(any(UserTable.class));
    }

    @Test
    void whenUpdateValidUser_thenReturnUpdatedUser() throws Exception {
        when(userTableService.updateUser(eq(testUser.getId()), any(UserTable.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/v1/user-table/{userId}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        verify(userTableService, times(1)).updateUser(eq(testUser.getId()), any(UserTable.class));
    }

    @Test
    void whenUpdateUserWithInvalidId_thenReturnNotFound() throws Exception {
        int invalidId = 999;
        when(userTableService.updateUser(eq(invalidId), any(UserTable.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/user-table/{userId}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());

        verify(userTableService, times(1)).updateUser(eq(invalidId), any(UserTable.class));
    }

    @Test
    void whenDeleteExistingUser_thenReturnOk() throws Exception {
        when(userTableService.deleteUser(testUser.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/user-table/{userId}", testUser.getId()))
                .andExpect(status().isOk());

        verify(userTableService, times(1)).deleteUser(testUser.getId());
    }

    @Test
    void whenDeleteNonExistingUser_thenReturnNotFound() throws Exception {
        int invalidId = 999;
        when(userTableService.deleteUser(invalidId)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/user-table/{userId}", invalidId))
                .andExpect(status().isNotFound());

        verify(userTableService, times(1)).deleteUser(invalidId);
    }
}