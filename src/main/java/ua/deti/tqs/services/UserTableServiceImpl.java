package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.repositories.UserTableRepository;
import ua.deti.tqs.services.interfaces.UserTableService;

@Slf4j
@Service
@AllArgsConstructor
public class UserTableServiceImpl implements UserTableService {
    private final UserTableRepository userTableRepository;

    @Override
    public UserTable getUserById(int id) {
        log.debug("Fetching user with id {}", id);
        UserTable user = userTableRepository.findById(id).orElse(null);

        if (user == null) {
            log.debug("No user found with id {}", id);
            return null;
        }

        log.debug("Found user with id {}", id);
        return user;
    }

    @Override
    public UserTable createUser(UserTable user) {
        log.debug("Creating new user {}", user);
        UserTable newUser = new UserTable();

        int errorCount = 0;
        if (user.getName() == null || user.getName().isEmpty()) {
            log.debug("Invalid user name");
            errorCount++;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.debug("Invalid user email");
            errorCount++;
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.debug("Invalid user password");
            errorCount++;
        }

        if (errorCount > 0)
            return null;

        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());


        if (user.getRole() != null)
            newUser.setRole(user.getRole());
        
        log.debug("User created successfully with id {}", newUser.getId());
        return userTableRepository.save(newUser);
    }

    @Override
    public UserTable updateUser(int id, UserTable user) {
        log.debug("Updating user with id {}", id);
        UserTable existingUser = userTableRepository.findById(id).orElse(null);

        if (existingUser == null) {
            log.debug("User not found");
            return null;
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            existingUser.setName(user.getName());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            existingUser.setEmail(user.getEmail());
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }

        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }

        log.debug("User updated successfully with id {}", existingUser.getId());
        return userTableRepository.save(existingUser);
    }

    @Override
    public boolean deleteUser(int id) {
        log.debug("Deleting user with id {}", id);
        UserTable existingUser = userTableRepository.findById(id).orElse(null);

        if (existingUser == null) {
            log.debug("User with id {}, not found", id);
            return false;
        }

        userTableRepository.delete(existingUser);
        log.debug("User deleted successfully with id {}", id);
        return true;
    }
}
