package bg.mechano.mechano.web.dto.user;

import java.time.Instant;

public record UserResponse(
        Long id,
        Long authUserId,
        String fullName,
        String phone,
        Long avatarImageId,
        Instant createdAt
) {
}