package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse create(ReviewCreateRequest request);

    ReviewResponse getById(Long id);

    List<ReviewResponse> list(Long repairShopId, Long userId);

    void hide(Long id);
}