package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.dto.review.ReviewThreadResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse create(ReviewCreateRequest request);

    ReviewResponse getById(Long id);

    List<ReviewResponse> list(
            Long repairShopId,
            Long userId
    );

    List<ReviewResponse> listCurrentUserReviews();

    List<ReviewThreadResponse> listThreaded(
            Long repairShopId
    );

    void delete(Long id);

    void restore(Long id);
}