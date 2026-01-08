package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link Review} entities.
 *
 * Provides data access operations for repairShop reviews,
 * including retrieval of visible reviews and threaded replies.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRepairShopIdAndIsVisibleTrue(Long repairShopId);

    List<Review> findByUserIdAndIsVisibleTrue(Long userId);
}
