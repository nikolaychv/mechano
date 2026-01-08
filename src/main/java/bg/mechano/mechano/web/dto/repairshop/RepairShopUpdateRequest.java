package bg.mechano.mechano.web.dto.repairshop;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RepairShopUpdateRequest(
        @Size(max = 255)
        String name,

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
        BigDecimal priceRangeMax,

        Boolean isActive
) {
}