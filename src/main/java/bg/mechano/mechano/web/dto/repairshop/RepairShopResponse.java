package bg.mechano.mechano.web.dto.repairshop;

import java.math.BigDecimal;
import java.time.Instant;

public record RepairShopResponse(
        Long id,
        Long ownerId,
        String name,
        String city,
        String address,
        String phone,
        String email,
        String website,
        String description,
        BigDecimal priceRangeMin,
        BigDecimal priceRangeMax,
        boolean isActive,
        Instant createdAt
) {
}