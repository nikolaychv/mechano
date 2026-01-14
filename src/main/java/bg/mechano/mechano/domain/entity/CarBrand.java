package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Represents a car brand entity in the system.
 * This entity maps to the "car_brands" table in the database.
 * It stores information about different car brands.
 *
 * Soft delete is implemented via the deletedAt field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "car_brands")
@SQLDelete(sql = "UPDATE car_brands SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class CarBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the car brand.
     * Must be unique among non-deleted records.
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Timestamp marking when the record was soft-deleted.
     * NULL means the record is active.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}