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
import ua.deti.tqs.components.JwtUtils;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.services.CustomUserDetailsService;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.utils.SecurityUtils;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.deti.tqs.utils.Constants.API_V1;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private SecurityUtils securityUtils;


    private User testUser;
    private User testUserInvalid;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        testUserInvalid = new User();
        testUserInvalid.setId(2);
        testUserInvalid.setName("");
        testUserInvalid.setEmail("");
        testUserInvalid.setPassword("");
        testUserInvalid.setRole(null);
    }

    @Test
    void whenCreateValidUser_thenReturnCreatedUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        when(userService.loginUser(any())).thenReturn(new LoginResponse(testUser.getId(), testUser.getName(), testUser.getEmail(), testUser.getRole(), "112", 112L));
        when (SecurityUtils.getAuthenticatedUser()).thenReturn(testUser);

        when(customUserDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        mockMvc.perform(post(STR."/\{API_V1}public/user-table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void whenCreateInvalidUser_thenReturnBadRequest() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(null);

        mockMvc.perform(post(STR."/\{API_V1}public/user-table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserInvalid)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void whenUpdateValidUser_thenReturnUpdatedUser() throws Exception {
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);
        when(userService.loginUser(any())).thenReturn(new LoginResponse(testUser.getId(), testUser.getName(),
                testUser.getEmail(), testUser.getRole(), "112", 112L));

        mockMvc.perform(put(STR."/\{API_V1}private/user-table", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        verify(userService, times(1)).updateUser(eq(testUser.getId()), any(User.class));
    }

    @Test
    void whenUpdateUserWithInvalidId_thenReturnNotFound() throws Exception {
        int invalidId = 999;
        when(userService.updateUser(eq(invalidId), any(User.class))).thenReturn(null);

        mockMvc.perform(put(STR."/\{API_V1}private/user-table", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(invalidId), any(User.class));
    }

    @Test
    void whenDeleteExistingUser_thenReturnOk() throws Exception {
        when(userService.deleteUser(testUser.getId())).thenReturn(true);

        mockMvc.perform(delete(STR."/\{API_V1}private/user-table", testUser.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(testUser.getId());
    }

    @Test
    void whenDeleteNonExistingUser_thenReturnNotFound() throws Exception {
        int invalidId = 999;
        when(userService.deleteUser(invalidId)).thenReturn(false);

        mockMvc.perform(delete(STR."/\{API_V1}private/user-table", invalidId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(invalidId);
    }
}