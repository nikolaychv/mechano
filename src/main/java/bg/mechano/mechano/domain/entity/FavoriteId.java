package bg.mechano.mechano.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Represents a composite key for the Favorite entity.
 * This class is used to uniquely identify a favorite relationship
 * between a user and a repairShop in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@EqualsAndHashCode
public class FavoriteId implements Serializable {
    private Long userId;
    private Long repairShopId;
}
