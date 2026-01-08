package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.Review;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.ReviewRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.ReviewService;
import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RepairShopRepository repairShopRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse create(ReviewCreateRequest request) {
        RepairShop repairShop = repairShopRepository.findById(request.repairShopId())
                .orElseThrow(() -> new NotFoundException("RepairShop not found: " + request.repairShopId()));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));

        Review parent = null;
        if (request.parentReviewId() != null) {
            parent = reviewRepository.findById(request.parentReviewId())
                    .orElseThrow(() -> new NotFoundException("Parent review not found: " + request.parentReviewId()));
        }

        Review review = Review.builder()
                .repairShop(repairShop)
                .user(user)
                .parentReview(parent)
                .ratingOverall(request.ratingOverall())
                .commentText(normalizeNullable(request.commentText()))
                .createdAt(Instant.now())
                .isVisible(true)
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        if (!review.isVisible()) {
            throw new NotFoundException("Review not found: " + id);
        }

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> list(Long repairShopId, Long userId) {
        List<Review> reviews;

        if (repairShopId != null) {
            reviews = reviewRepository.findByRepairShopIdAndIsVisibleTrue(repairShopId);
        } else if (userId != null) {
            reviews = reviewRepository.findByUserIdAndIsVisibleTrue(userId);
        } else {
            reviews = reviewRepository.findAll().stream()
                    .filter(Review::isVisible)
                    .toList();
        }

        return reviews.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void hide(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        review.setVisible(false);
        reviewRepository.save(review);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getRepairShop().getId(),
                r.getUser().getId(),
                r.getParentReview() != null ? r.getParentReview().getId() : null,
                r.getRatingOverall(),
                r.getCommentText(),
                r.getCreatedAt()
        );
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}