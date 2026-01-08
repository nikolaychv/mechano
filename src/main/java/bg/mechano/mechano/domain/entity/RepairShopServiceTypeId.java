package bg.mechano.mechano.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Represents the composite key for the RepairShopServiceType entity.
 * This class is used to uniquely identify the relationship between a repairShop and a repairShop type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@EqualsAndHashCode
public class RepairShopServiceTypeId implements Serializable {
    private Long repairShopId;
    private Long repairShopTypeId;
}

