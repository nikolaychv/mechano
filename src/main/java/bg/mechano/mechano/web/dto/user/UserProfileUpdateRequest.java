package bg.mechano.mechano.web.dto.user;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 255)
        String fullName,

        @Size(max = 50)
        String phone
) {
}