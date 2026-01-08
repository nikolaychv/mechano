package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a favorite entity in the system.
 * This entity maps to the "favorites" table in the database.
 * It stores information about user-repairShop relationships marked as favorites.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "favorites")
public class Favorite {

    @EmbeddedId
    private FavoriteId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("repairShopId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_id", nullable = false)
    private RepairShop repairShop;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
