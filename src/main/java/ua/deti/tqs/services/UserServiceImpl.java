package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ua.deti.tqs.components.JwtUtils;
import ua.deti.tqs.dto.LoginRequest;
import ua.deti.tqs.dto.LoginResponse;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.services.interfaces.UserService;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;

  @Override
  public User getUserById(int id) {
    log.debug("Fetching user with id {}", id);
    User user = userRepository.findById(id).orElse(null);

    if (user == null) {
      log.debug("No user found with id {}", id);
      return null;
    }

    log.debug("Found user with id {}", id);
    return user;
  }

  @Override
  public User getUserByEmail(String email) {
    log.debug("Fetching user with email {}", email);
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      log.debug("No user found with email {}", email);
    }
    log.debug("Found user with email {}", email);
    return user;
  }

  public LoginResponse loginUser(LoginRequest loginRequest) {
    User user = this.getUserByEmail(loginRequest.getEmail());

    if (user == null) {
      return null;
    }
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    Long expires = jwtUtils.getExpirationFromJwtToken(jwt).getTime();

    User userDetails = (User) authentication.getPrincipal();

    return new LoginResponse(
        userDetails.getId(),
        userDetails.getName(),
        userDetails.getEmail(),
        userDetails.getRole(),
        jwt,
        expires);
  }

  @Override
  public User createUser(User user) {
    log.info("Creating new user {}", user);
    User newUser = new User();

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

    if (errorCount > 0) return null;

    user.setPassword(passwordEncoder.encode(user.getPassword()));
    newUser.setName(user.getName());
    newUser.setEmail(user.getEmail());
    newUser.setPassword(user.getPassword());

    if (user.getRole() != null) newUser.setRole(user.getRole());
    log.debug("User created successfully with id {}", newUser.getId());
    return userRepository.save(newUser);
  }

  @Override
  public User updateUser(int id, User user) {
    log.debug("Updating user with id {}", id);
    User existingUser = userRepository.findById(id).orElse(null);

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
      existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    if (user.getRole() != null) {
      existingUser.setRole(user.getRole());
    }

    log.debug("User updated successfully with id {}", existingUser.getId());
    return userRepository.save(existingUser);
  }

  @Override
  public boolean deleteUser(int id) {
    log.debug("Deleting user with id {}", id);
    User existingUser = userRepository.findById(id).orElse(null);

    if (existingUser == null) {
      log.debug("User with id {}, not found", id);
      return false;
    }

    userRepository.delete(existingUser);
    log.debug("User deleted successfully with id {}", id);
    return true;
  }
}
