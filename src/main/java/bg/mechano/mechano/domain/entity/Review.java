package bg.mechano.mechano.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a review entity in the system.
 * This entity maps to the "reviews" table in the database.
 * It stores information about user reviews for services, including ratings and comments.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_id", nullable = false)
    private RepairShop repairShop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_review_id")
    private Review parentReview;

    @Column(name = "rating_overall", nullable = false)
    private short ratingOverall;

    @Column(name = "comment_text", columnDefinition = "text")
    private String commentText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}