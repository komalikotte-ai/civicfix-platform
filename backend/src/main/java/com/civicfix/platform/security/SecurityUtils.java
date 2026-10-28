package com.civicfix.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserPrincipal)
                .map(p -> (UserPrincipal) p);
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId);
    }

    public static Optional<String> getCurrentUsername() {
        return getCurrentUserPrincipal().map(UserPrincipal::getUsername);
    }

    public static Optional<Long> getCurrentOrganizationId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getOrganizationId);
    }

    public static boolean hasRole(String role) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role) ||
                                a.getAuthority().equalsIgnoreCase("ROLE_" + role)))
                .orElse(false);
    }
}
