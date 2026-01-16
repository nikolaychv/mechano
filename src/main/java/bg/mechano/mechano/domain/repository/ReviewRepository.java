package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Review} entities.
 *
 * Provides data access operations for repairShop reviews,
 * including retrieval of visible reviews and threaded replies.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // existing
    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    List<Review> findByRepairShopIdAndDeletedAtIsNull(Long repairShopId);

    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);

    // root reviews (parents)
    List<Review> findByRepairShopIdAndParentReviewIsNullAndDeletedAtIsNull(Long repairShopId);

    // replies for a review
    List<Review> findByParentReviewIdAndDeletedAtIsNull(Long parentReviewId);
}
