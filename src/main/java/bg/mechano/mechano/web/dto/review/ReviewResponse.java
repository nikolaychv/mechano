package bg.mechano.mechano.web.dto.review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long repairShopId,
        Long userId,
        Long parentReviewId,
        short ratingOverall,
        String commentText,
        Instant createdAt
) {
}
