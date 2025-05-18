package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.services.interfaces.UserService;
import ua.deti.tqs.utils.Constants;

import static ua.deti.tqs.utils.SecurityUtils.getAuthenticatedUser;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1)
@Tag(name = "User Table", description = "The User Table API")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    private static final String PUB_BASE_PATH = "public/user-table";
    private static final String PRIV_BASE_PATH = "private/user-table";

    @PostMapping(PUB_BASE_PATH)
    @Operation(summary = "Create User", description = "Creates a new user.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    public ResponseEntity<LoginResponse> createUser(@RequestBody User user) {
        log.info("Creating new user {}", user);
        String rawPassword = user.getPassword();
        User newUser = userService.createUser(user);

        if (newUser == null) {
            log.warn("Invalid user data");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("User created successfully");
        // in service make a login
        LoginRequest loginRequest = new LoginRequest(newUser.getEmail(), rawPassword);
        LoginResponse loginResponse = userService.loginUser(loginRequest);

        return new ResponseEntity<>(loginResponse, HttpStatus.CREATED);
    }

    @PutMapping(PRIV_BASE_PATH)
    @Operation(summary = "Update User", description = "Updates a user.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Object> updateUser(@RequestBody User user) {
        String rawPassword = user.getPassword();

        User authenticatedUser = getAuthenticatedUser();
        log.info("Updating user with id {}", authenticatedUser.getId());

        User updatedUser = userService.updateUser(authenticatedUser.getId(), user);

        if (updatedUser == null) {
            log.warn("No user not updated");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User updated successfully");
        // maybe a problem beacuse the password is encrypted to see later on
        if (rawPassword != null && !rawPassword.isEmpty()) {

            return ResponseEntity.ok(userService.loginUser(new LoginRequest(updatedUser.getEmail(), rawPassword)));
        }
        // se ele receber o user updated entao esta perfeito, so muda os campos que mudaram
        return ResponseEntity.ok(updatedUser);


    }

    @DeleteMapping(PRIV_BASE_PATH)
    @Operation(summary = "Delete User", description = "Deletes a user.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser() {

        User authenticatedUser = getAuthenticatedUser();
        log.info("Deleting user with id {}", authenticatedUser.getId());

        boolean deleted = userService.deleteUser(authenticatedUser.getId());

        if (!deleted) {
            log.warn("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User deleted successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping(PUB_BASE_PATH + "/login")
    @Operation(summary = "Login User", description = "Logs in a user.")
    @ApiResponse(responseCode = "200", description = "User logged in successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        log.info("Logging in user with email {}", loginRequest.getEmail());

        LoginResponse loginResponse = userService.loginUser(loginRequest);

        if (loginResponse == null) {
            log.warn("Invalid credentials");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        log.info("User logged in successfully");
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }


}
