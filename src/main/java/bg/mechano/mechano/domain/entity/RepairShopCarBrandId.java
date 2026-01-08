package bg.mechano.mechano.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Represents the composite key for the RepairShopCarBrand entity.
 * This class is used to uniquely identify the relationship between a repairShop and a car brand.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@EqualsAndHashCode
public class RepairShopCarBrandId implements Serializable {
    private Long repairShopId;
    private Long carBrandId;
}
