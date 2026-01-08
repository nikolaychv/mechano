package bg.mechano.mechano.web.dto.carbrand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarBrandUpdateRequest(
        @NotBlank
        @Size(max = 150)
        String name
) {}