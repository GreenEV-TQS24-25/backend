package ua.deti.tqs.utils;

import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ua.deti.tqs.entities.User;

@Component
public final class SecurityUtils {
  private SecurityUtils() {}

  public static int getAuthenticatedUserId() {
    return Objects.requireNonNull(getAuthenticatedUser()).getId();
  }

  public static User getAuthenticatedUser() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    return (User) authentication.getPrincipal();
  }
}
