package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a user consent record in the system.
 * This entity maps to the "user_consents" table in the database.
 * It stores information about user consent decisions related to legal,
 * regulatory, or platform-specific requirements (e.g. terms acceptance,
 * privacy policy, marketing consent).
 *
 * Each record reflects the state of a specific consent type at a given point in time.
 * The {@code granted} flag indicates whether the consent was accepted or declined.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_consents")
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private boolean granted;

    @Column(name = "at", nullable = false)
    private Instant at;
}
