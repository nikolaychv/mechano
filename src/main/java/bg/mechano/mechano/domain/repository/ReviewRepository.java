package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    List<Review> findByRepairShopIdAndDeletedAtIsNull(Long repairShopId);

    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Review> findByRepairShopIdAndUserIdAndDeletedAtIsNull(Long repairShopId, Long userId);

    List<Review> findByDeletedAtIsNull();

    List<Review> findByRepairShopIdAndParentReviewIsNullAndDeletedAtIsNull(Long repairShopId);

    List<Review> findByParentReviewIdAndDeletedAtIsNull(Long parentReviewId);
}