package ua.deti.tqs.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserServiceImpl userTableService;

    private User user;
    private User userInvalid;

    private User userNull;

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
}
