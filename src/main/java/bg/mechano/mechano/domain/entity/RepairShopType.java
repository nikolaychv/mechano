package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a repairShop type entity in the system.
 * This entity maps to the "repair_shop_types" table in the database.
 * It stores information about the types of services offered, including their name and description.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repair_shop_types")
public class RepairShopType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;
}
