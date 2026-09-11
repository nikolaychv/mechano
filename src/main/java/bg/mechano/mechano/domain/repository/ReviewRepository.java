package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"repairShop", "user", "parentReview"})
    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"repairShop", "user", "parentReview"})
    List<Review> findByRepairShopIdAndDeletedAtIsNull(Long repairShopId);

    @EntityGraph(attributePaths = {"repairShop", "user", "parentReview"})
    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);

    @EntityGraph(attributePaths = {"repairShop", "user", "parentReview"})
    List<Review> findByRepairShopIdAndUserIdAndDeletedAtIsNull(Long repairShopId, Long userId);

    @EntityGraph(attributePaths = {"repairShop", "user", "parentReview"})
    List<Review> findByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"repairShop", "user"})
    List<Review> findByRepairShopIdAndParentReviewIsNullAndDeletedAtIsNull(Long repairShopId);

    @EntityGraph(attributePaths = {"repairShop", "user"})
    List<Review> findByParentReviewIdAndDeletedAtIsNull(Long parentReviewId);
}