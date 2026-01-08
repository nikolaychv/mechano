package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a car brand entity in the system.
 * This entity maps to the "car_brands" table in the database.
 * It stores information about different car brands.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "car_brands")
public class CarBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;
}
