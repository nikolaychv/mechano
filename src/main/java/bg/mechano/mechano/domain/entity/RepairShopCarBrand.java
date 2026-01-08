package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents the relationship between a repairShop and a car brand.
 * This entity maps to the "repair_shop_car_brands" table in the database.
 * It defines which car brands are supported by a specific repairShop.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repair_shop_car_brands")
public class RepairShopCarBrand {

    @EmbeddedId
    private RepairShopCarBrandId id;

    @MapsId("repairShopId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_id", nullable = false)
    private RepairShop repairShop;

    @MapsId("carBrandId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_brand_id", nullable = false)
    private CarBrand carBrand;
}
