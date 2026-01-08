package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents the relationship between a repairShop and a repairShop type.
 * This entity maps to the "repair_shop_service_types" table in the database.
 * It defines which repairShop types are offered by a specific repairShop, along with optional pricing details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repair_shop_service_types")
public class RepairShopServiceType {

    @EmbeddedId
    @Column(nullable = false)
    private RepairShopServiceTypeId id;

    @MapsId("repairShopId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_id", nullable = false)
    private RepairShop repairShop;

    @MapsId("repairShopTypeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_type_id", nullable = false)
    private RepairShopType repairShopType;

    @Column(name = "price_from", precision = 10, scale = 2)
    private BigDecimal priceFrom;

    @Column(name = "price_to", precision = 10, scale = 2)
    private BigDecimal priceTo;
}
