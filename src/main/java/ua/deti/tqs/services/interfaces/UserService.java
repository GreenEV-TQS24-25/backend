package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;

public interface UserService {
    User getUserById(int id);

    User getUserByEmail(String email);

    User createUser(User user);

    User updateUser(int id, User user);

    LoginResponse loginUser(LoginRequest loginRequest);

    boolean deleteUser(int id);
}
