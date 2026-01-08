package bg.mechano.mechano.web.dto.repairshoptype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RepairShopTypeUpdateRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 5000)
        String description
) {
}