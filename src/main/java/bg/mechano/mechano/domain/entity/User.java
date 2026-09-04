package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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

    @Column(name = "auth_user_id", unique = true)
    private Long authUserId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Column(name = "avatar_image_id")
    private Long avatarImageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}