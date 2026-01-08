package bg.mechano.mechano.web.dto.user;

import bg.mechano.mechano.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 255)
        String fullName,

        @NotBlank
        @Size(max = 100)
        String username,

        @Size(max = 50)
        String phone,

        @NotNull
        UserRole role
) {
}