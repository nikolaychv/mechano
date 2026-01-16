package bg.mechano.mechano.web.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

public record ReviewThreadResponse(
        Long id,
        Long repairShopId,
        Long userId,
        short ratingOverall,
        String commentText,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Sofia")
        Instant createdAt,

        List<ReviewThreadResponse> replies
) {
}
