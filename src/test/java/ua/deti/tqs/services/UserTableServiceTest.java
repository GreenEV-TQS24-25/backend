package ua.deti.tqs.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.UserTableRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserTableServiceTest {

    @Mock
    private UserTableRepository userTableRepository;

    @InjectMocks
    private UserTableServiceImpl userTableService;

    private UserTable userTable;
    private UserTable userTableInvalid;

    private UserTable userTableNull;

    @BeforeEach
    void setUp() {
        userTable = new UserTable();
        userTable.setId(1);
        userTable.setName("User 1");
        userTable.setEmail("Email 1");
        userTable.setPassword("Password 1");
        userTable.setRole(Role.USER);

        userTableInvalid = new UserTable();
        userTableInvalid.setId(2);
        userTableInvalid.setName("");
        userTableInvalid.setEmail("");
        userTableInvalid.setPassword("");
        userTableInvalid.setRole(null);

        userTableNull = new UserTable();
        userTableNull.setId(3);
        userTableNull.setName(null);
        userTableNull.setEmail(null);
        userTableNull.setPassword(null);
        userTableNull.setRole(null);
    }

    @Test
    void whenGetUserById_thenReturnUser() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(userTable));

        UserTable found = userTableService.getUserById(1);

        assertThat(found).isEqualTo(userTable);
    }

    @Test
    void whenGetUserById_withInvalidId_thenReturnNull() {
        when(userTableRepository.findById(2)).thenReturn(Optional.empty());

        UserTable found = userTableService.getUserById(2);

        assertThat(found).isNull();
    }

    @Test
    void whenCreateUser_thenReturnUser() {
        when(userTableRepository.save(any())).thenReturn(userTable);

        UserTable created = userTableService.createUser(userTable);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("User 1");
        assertThat(created.getEmail()).isEqualTo("Email 1");
        assertThat(created.getPassword()).isEqualTo("Password 1");
        assertThat(created.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void whenCreateUser_withPartialData_thenReturnUser() {
        UserTable toCreate = new UserTable();
        toCreate.setName("User 1");
        toCreate.setEmail("Email 1");
        toCreate.setPassword("Password 1");
        toCreate.setRole(null);
        when(userTableRepository.save(any())).thenReturn(userTable);

        UserTable created = userTableService.createUser(toCreate);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("User 1");
        assertThat(created.getEmail()).isEqualTo("Email 1");
        assertThat(created.getPassword()).isEqualTo("Password 1");
        assertThat(created.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void whenCreateUser_withInvalidData_thenReturnNull() {
        UserTable created = userTableService.createUser(userTableInvalid);

        assertThat(created).isNull();
    }

    @Test
    void whenCreateUser_withNullData_thenReturnNull() {
        UserTable created = userTableService.createUser(userTableNull);

        assertThat(created).isNull();
    }

    @Test
    void whenUpdateUser_thenReturnUpdatedUser() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(userTable));
        userTable.setName("Updated User");
        userTable.setEmail("Updated Email");
        userTable.setPassword("Updated Password");
        userTable.setRole(Role.OPERATOR);
        when(userTableRepository.save(userTable)).thenReturn(userTable);

        UserTable updated = userTableService.updateUser(1, userTable);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated User");
        assertThat(updated.getEmail()).isEqualTo("Updated Email");
        assertThat(updated.getPassword()).isEqualTo("Updated Password");
        assertThat(updated.getRole()).isEqualTo(Role.OPERATOR);
    }

    @Test
    void whenUpdateUser_withInvalidId_thenReturnNull() {
        when(userTableRepository.findById(2)).thenReturn(Optional.empty());

        UserTable updated = userTableService.updateUser(2, userTable);

        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateUser_withInvalidData_thenReturnNull() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(userTable));

        userTableInvalid.setId(1);

        UserTable updated = userTableService.updateUser(1, userTableInvalid);

        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateUser_withNullData_thenReturnNull() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(userTable));

        userTableNull.setId(1);

        UserTable updated = userTableService.updateUser(1, userTableNull);

        assertThat(updated).isNull();
    }

    @Test
    void whenDeleteUser_thenReturnTrue() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(userTable));

        boolean deleted = userTableService.deleteUser(1);

        assertThat(deleted).isTrue();
    }

    @Test
    void whenDeleteUser_withInvalidId_thenReturnFalse() {
        when(userTableRepository.findById(2)).thenReturn(Optional.empty());

        boolean deleted = userTableService.deleteUser(2);

        assertThat(deleted).isFalse();
    }
}
