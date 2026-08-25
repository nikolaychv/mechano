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
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

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
}