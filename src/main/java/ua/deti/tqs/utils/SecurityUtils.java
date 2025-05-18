package ua.deti.tqs.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.deti.tqs.entities.User;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public static int getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }
}