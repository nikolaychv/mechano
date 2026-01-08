package bg.mechano.mechano.web.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull
        Long repairShopId,

        @NotNull
        Long userId,

        @Min(1)
        @Max(5)
        short ratingOverall,

        @Size(max = 5000)
        String commentText,

        Long parentReviewId
) {
}