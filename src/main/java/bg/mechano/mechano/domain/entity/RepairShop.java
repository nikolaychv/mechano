package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a repairShop entity in the system.
 * This entity maps to the "repair_shops" table in the database.
 * It stores information about various services offered by users, including details such as name, location, pricing, and availability.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repair_shops")
public class RepairShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 150)
    private String city;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "price_range_min", precision = 10, scale = 2)
    private BigDecimal priceRangeMin;

    @Column(name = "price_range_max", precision = 10, scale = 2)
    private BigDecimal priceRangeMax;

    @Column(name = "cover_image_id")
    private Long coverImageId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // legacy field
    @Column(name = "media_storage_key", length = 255)
    private String mediaStorageKey;

    @Column(name = "cover_image_path", length = 500)
    private String coverImagePath;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}