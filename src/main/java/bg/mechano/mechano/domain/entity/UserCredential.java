package bg.mechano.mechano.domain.entity;

import bg.mechano.mechano.domain.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents authentication credentials associated with a user.
 * This entity maps to the "user_credentials" table in the database.
 *
 * It supports multiple authentication providers (e.g. local authentication,
 * OAuth-based providers such as Google) and ensures a unique combination
 * of provider and provider-specific user identifier.
 *
 * A user may have multiple credential records, each corresponding
 * to a different authentication provider.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_credentials",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_credentials_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
