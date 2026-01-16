package bg.mechano.mechano.web.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long repairShopId,
        Long userId,
        Long parentReviewId,
        short ratingOverall,
        String commentText,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd HH:mm:ss",
                timezone = "Europe/Sofia"
        )
        Instant createdAt
) {
}
