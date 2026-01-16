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
import bg.mechano.mechano.web.dto.review.ReviewThreadResponse;
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
            parent = reviewRepository.findByIdAndDeletedAtIsNull(request.parentReviewId())
                    .orElseThrow(() -> new NotFoundException("Parent review not found: " + request.parentReviewId()));
        }

        Review review = Review.builder()
                .repairShop(repairShop)
                .user(user)
                .parentReview(parent)
                .ratingOverall(request.ratingOverall())
                .commentText(normalizeNullable(request.commentText()))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));
        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> list(Long repairShopId, Long userId) {
        List<Review> reviews;

        if (repairShopId != null) {
            reviews = reviewRepository.findByRepairShopIdAndDeletedAtIsNull(repairShopId);
        } else if (userId != null) {
            reviews = reviewRepository.findByUserIdAndDeletedAtIsNull(userId);
        } else {
            reviews = reviewRepository.findAll().stream()
                    .filter(r -> r.getDeletedAt() == null)
                    .toList();
        }

        return reviews.stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        review.setDeletedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Override
    public void restore(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        if (review.getDeletedAt() == null) {
            return; // already active
        }

        review.setDeletedAt(null);
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

    @Override
    @Transactional(readOnly = true)
    public List<ReviewThreadResponse> listThreaded(Long repairShopId) {
        List<Review> roots =
                reviewRepository.findByRepairShopIdAndParentReviewIsNullAndDeletedAtIsNull(repairShopId);

        return roots.stream()
                .map(this::toThreadResponse)
                .toList();
    }

    private ReviewThreadResponse toThreadResponse(Review review) {
        List<ReviewThreadResponse> replies =
                reviewRepository.findByParentReviewIdAndDeletedAtIsNull(review.getId())
                        .stream()
                        .map(this::toThreadResponse)
                        .toList();

        return new ReviewThreadResponse(
                review.getId(),
                review.getRepairShop().getId(),
                review.getUser().getId(),
                review.getRatingOverall(),
                review.getCommentText(),
                review.getCreatedAt(),
                replies
        );
    }
}
