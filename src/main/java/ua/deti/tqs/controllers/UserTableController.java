package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.services.interfaces.UserTableService;
import ua.deti.tqs.utils.Constants;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "user-table")
@Tag(name = "User Table", description = "The User Table API")
@AllArgsConstructor
public class UserTableController {
    private final UserTableService userTableService;




    @GetMapping("{userId}")
    @Operation(summary = "Get User by id", description = "Fetches a user by id.")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Not User found")
    public ResponseEntity<UserTable> getUserByUserId(@PathVariable("userId") int userId) {
        log.info("Fetching user with id {}", userId);

        UserTable user = userTableService.getUserById(userId);

        if (user == null) {
            log.warn("No user found with id {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User retrieved successfully");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Create User", description = "Creates a new user.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    public ResponseEntity<UserTable> createUser(@RequestBody UserTable user) {
        log.info("Creating new user {}", user);

        UserTable newUser = userTableService.createUser(user);

        if (newUser == null) {
            log.warn("Invalid user data");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("User created successfully");
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PutMapping("private/{userId}") // TODO Refactor this to use the context
    @Operation(summary = "Update User", description = "Updates a user.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserTable> updateUser(@PathVariable("userId") int userId, @RequestBody UserTable user) {
        log.info("Updating user with id {}", userId);

        UserTable updatedUser = userTableService.updateUser(userId, user);

        if (updatedUser == null) {
            log.warn("No user not updated");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User updated successfully");
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("private/{userId}") // TODO Refactor this to use the context
    @Operation(summary = "Delete User", description = "Deletes a user.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") int userId) {
        log.info("Deleting user with id {}", userId);

        boolean deleted = userTableService.deleteUser(userId);

        if (!deleted) {
            log.warn("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("User deleted successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
