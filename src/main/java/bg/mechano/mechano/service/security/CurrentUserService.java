package bg.mechano.mechano.service.security;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_SHOP_OWNER = "ROLE_SHOP_OWNER";

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Long authUserId = getCurrentAuthUserId();

        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user is not linked to a Mechano user."
                        )
                );

        if (!user.isActive() || user.getDeletedAt() != null) {
            throw new AccessDeniedException(
                    "Mechano user is inactive."
            );
        }

        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Long getCurrentAuthUserId() {
        Authentication authentication = getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new AccessDeniedException(
                    "JWT authentication is required."
            );
        }

        String subject = jwtAuthentication
                .getToken()
                .getSubject();

        if (subject == null || subject.isBlank()) {
            throw new AccessDeniedException(
                    "JWT subject is missing."
            );
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException(
                    "JWT subject is invalid."
            );
        }
    }

    public boolean isAdmin() {
        return hasAuthority(ROLE_ADMIN);
    }

    public boolean isUser() {
        return hasAuthority(ROLE_USER);
    }

    public boolean isShopOwner() {
        return hasAuthority(ROLE_SHOP_OWNER);
    }

    private boolean hasAuthority(String authority) {
        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority
                                .getAuthority()
                                .equals(authority)
                );
    }

    private Authentication getAuthentication() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Authentication is required."
            );
        }

        return authentication;
    }
}