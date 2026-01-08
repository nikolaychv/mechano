package bg.mechano.mechano.web.dto.user;

import bg.mechano.mechano.domain.enums.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String username,
        String phone,
        UserRole role,
        boolean isActive,
        Instant createdAt
) {
}