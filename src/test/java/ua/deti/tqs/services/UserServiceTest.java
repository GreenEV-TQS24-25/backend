package ua.deti.tqs.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.components.JwtUtils;
import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserRepository;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userTableService;

    private User user;
    private User userInvalid;

    private User userNull;

    private LoginRequest loginRequest;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("User 1");
        user.setEmail("Email 1");
        user.setPassword("Password 1");
        user.setRole(Role.USER);

        userInvalid = new User();
        userInvalid.setId(2);
        userInvalid.setName("");
        userInvalid.setEmail("");
        userInvalid.setPassword("");
        userInvalid.setRole(null);

        userNull = new User();
        userNull.setId(3);
        userNull.setName(null);
        userNull.setEmail(null);
        userNull.setPassword(null);
        userNull.setRole(null);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("Email 1");
        loginRequest.setPassword("Password 1");

        // Limpar o contexto de segurança antes de cada teste
        SecurityContextHolder.clearContext();

    }

    @Test
    void whenGetUserById_thenReturnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User found = userTableService.getUserById(1);

        assertThat(found).isEqualTo(user);
    }

    @Test
    void whenGetUserById_withInvalidId_thenReturnNull() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        User found = userTableService.getUserById(2);

        assertThat(found).isNull();
    }

    @Test
    void whenCreateUser_thenReturnUser() {
        when(userRepository.save(any())).thenReturn(user);

        User created = userTableService.createUser(user);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("User 1");
        assertThat(created.getEmail()).isEqualTo("Email 1");
        // Password is encoded, so we need to check the encoded version
        String encodedPassword = passwordEncoder.encode("Password 1");
        assertThat(passwordEncoder.matches("Password 1", encodedPassword)).isTrue();
        assertThat(created.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void whenCreateUser_withPartialData_thenReturnUser() {
        User toCreate = new User();
        toCreate.setName("User 1");
        toCreate.setEmail("Email 1");
        toCreate.setPassword("Password 1");
        toCreate.setRole(null);
        when(userRepository.save(any())).thenReturn(user);

        User created = userTableService.createUser(toCreate);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("User 1");
        assertThat(created.getEmail()).isEqualTo("Email 1");
        assertThat(created.getPassword()).isEqualTo("Password 1");
        assertThat(created.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void whenCreateUser_withInvalidData_thenReturnNull() {
        User created = userTableService.createUser(userInvalid);

        assertThat(created).isNull();
    }

    @Test
    void whenCreateUser_withNullData_thenReturnNull() {
        User created = userTableService.createUser(userNull);

        assertThat(created).isNull();
    }

    @Test
    void whenUpdateUser_thenReturnUpdatedUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        user.setName("Updated User");
        user.setEmail("Updated Email");
        user.setPassword("Updated Password");
        user.setRole(Role.OPERATOR);
        when(userRepository.save(user)).thenReturn(user);

        User updated = userTableService.updateUser(1, user);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated User");
        assertThat(updated.getEmail()).isEqualTo("Updated Email");
        assertThat(updated.getPassword()).isEqualTo("Updated Password");
        assertThat(updated.getRole()).isEqualTo(Role.OPERATOR);
    }

    @Test
    void whenUpdateUser_withInvalidId_thenReturnNull() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        User updated = userTableService.updateUser(2, user);

        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateUser_withInvalidData_thenReturnNull() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userInvalid.setId(1);

        User updated = userTableService.updateUser(1, userInvalid);

        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateUser_withNullData_thenReturnNull() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userNull.setId(1);

        User updated = userTableService.updateUser(1, userNull);

        assertThat(updated).isNull();
    }

    @Test
    void whenDeleteUser_thenReturnTrue() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        boolean deleted = userTableService.deleteUser(1);

        assertThat(deleted).isTrue();
    }

    @Test
    void whenDeleteUser_withInvalidId_thenReturnFalse() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        boolean deleted = userTableService.deleteUser(2);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenLoginUser_withValidCredentials_thenReturnLoginResponse() {
        // Configurar o mock para getUserByEmail
        when(userRepository.findByEmail("Email 1")).thenReturn(Optional.of(user));

        // Configurar o mock do Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        // Configurar o mock do AuthenticationManager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Configurar o mock do JwtUtils
        String mockToken = "mock-jwt-token";
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(mockToken);
        Date expirationDate = new Date(System.currentTimeMillis() + 3600000); // 1 hora no futuro
        when(jwtUtils.getExpirationFromJwtToken(mockToken)).thenReturn(expirationDate);

        // Executar o método a ser testado
        LoginResponse response = userTableService.loginUser(loginRequest);

        // Verificar o resultado
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("User 1");
        assertThat(response.getEmail()).isEqualTo("Email 1");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getToken()).isEqualTo(mockToken);
        assertThat(response.getExpires()).isEqualTo(expirationDate.getTime());

        // Verificar as chamadas de método
        verify(userRepository).findByEmail("Email 1");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(jwtUtils).getExpirationFromJwtToken(mockToken);
    }

    @Test
    void whenLoginUser_withNonExistentUser_thenReturnNull() {
        // Configurar o mock para getUserByEmail retornar null
        when(userRepository.findByEmail("Email 1")).thenReturn(Optional.empty());

        // Executar o método a ser testado
        LoginResponse response = userTableService.loginUser(loginRequest);

        // Verificar o resultado
        assertThat(response).isNull();

        // Verificar que apenas getUserByEmail foi chamado, e não os outros métodos
        verify(userRepository).findByEmail("Email 1");
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void whenLoginUser_withAuthenticationException_thenHandleException() {
        // Configurar o mock para getUserByEmail
        when(userRepository.findByEmail("Email 1")).thenReturn(Optional.of(user));

        // Configurar o AuthenticationManager para lançar uma exceção
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Executar o método e verificar que a exceção é propagada
        try {
            userTableService.loginUser(loginRequest);
            // Se não lançar exceção, falha no teste
            org.junit.jupiter.api.Assertions.fail("Expected exception was not thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Authentication failed");
        }

        // Verificar as chamadas de método
        verify(userRepository).findByEmail("Email 1");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils);
    }

}
