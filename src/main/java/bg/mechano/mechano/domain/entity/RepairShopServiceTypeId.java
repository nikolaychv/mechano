package bg.mechano.mechano.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Represents the composite key for the RepairShopServiceType entity.
 * This class uniquely identifies the relationship between
 * a RepairShop and a RepairShopType.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@EqualsAndHashCode
public class RepairShopServiceTypeId implements Serializable {

    @Column(name = "repair_shop_id", nullable = false)
    private Long repairShopId;

    @Column(name = "repair_shop_type_id", nullable = false)
    private Long repairShopTypeId;
}
