package bg.mechano.mechano.domain.entity;

import bg.mechano.mechano.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a user entity in the system.
 * This entity maps to the "users" table in the database.
 * It stores core account information used for authentication, authorization,
 * and ownership of other domain entities such as services, bookings, and reviews.
 *
 * A user can have different roles (e.g. CLIENT, SERVICE_OWNER, ADMIN),
 * which define their permissions within the platform.
 * The entity also supports soft deletion via the {@code deletedAt} field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "avatar_image_id")
    private Long avatarImageId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
