package bg.mechano.mechano.web.dto.repairshop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RepairShopCreateRequest(
        @NotNull
        Long ownerId,

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 150)
        String city,

        @Size(max = 255)
        String address,

        @Size(max = 50)
        String phone,

        @Size(max = 255)
        String email,

        @Size(max = 255)
        String website,

        String description,

        BigDecimal priceRangeMin,
        BigDecimal priceRangeMax
) {
}