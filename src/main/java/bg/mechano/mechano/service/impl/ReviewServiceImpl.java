package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.Review;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.ReviewRepository;
import bg.mechano.mechano.service.ReviewService;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.dto.review.ReviewThreadResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final CurrentUserService currentUserService;

    @Override
    public ReviewResponse create(ReviewCreateRequest request) {
        User currentUser =
                currentUserService.getCurrentUser();

        RepairShop repairShop = repairShopRepository
                .findById(request.repairShopId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "RepairShop not found: "
                                        + request.repairShopId()
                        )
                );

        Review review = Review.builder()
                .repairShop(repairShop)
                .user(currentUser)
                .parentReview(null)
                .ratingOverall(request.ratingOverall())
                .commentText(
                        normalizeNullable(
                                request.commentText()
                        )
                )
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(
                reviewRepository.save(review)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Review not found: " + id
                        )
                );

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> list(
            Long repairShopId,
            Long userId
    ) {
        List<Review> reviews;

        if (repairShopId != null) {
            reviews =
                    reviewRepository
                            .findByRepairShopIdAndDeletedAtIsNull(
                                    repairShopId
                            );
        } else if (userId != null) {
            reviews =
                    reviewRepository
                            .findByUserIdAndDeletedAtIsNull(
                                    userId
                            );
        } else {
            reviews = reviewRepository
                    .findAll()
                    .stream()
                    .filter(review ->
                            review.getDeletedAt() == null
                    )
                    .toList();
        }

        return reviews
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> listCurrentUserReviews() {
        Long currentUserId =
                currentUserService.getCurrentUserId();

        return reviewRepository
                .findByUserIdAndDeletedAtIsNull(
                        currentUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewThreadResponse> listThreaded(
            Long repairShopId
    ) {
        List<Review> roots = reviewRepository
                .findByRepairShopIdAndParentReviewIsNullAndDeletedAtIsNull(
                        repairShopId
                );

        return roots
                .stream()
                .map(this::toThreadResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Review not found: " + id
                        )
                );

        authorizeDelete(review);

        review.setDeletedAt(Instant.now());

        reviewRepository.save(review);
    }

    @Override
    public void restore(Long id) {
        Review review = reviewRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Review not found: " + id
                        )
                );

        if (review.getDeletedAt() == null) {
            return;
        }

        review.setDeletedAt(null);

        reviewRepository.save(review);
    }

    private void authorizeDelete(Review review) {
        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUserService.isUser()
                && review
                .getUser()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot delete this review."
        );
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRepairShop().getId(),
                review.getUser().getId(),
                review.getParentReview() != null
                        ? review.getParentReview().getId()
                        : null,
                review.getRatingOverall(),
                review.getCommentText(),
                review.getCreatedAt()
        );
    }

    private ReviewThreadResponse toThreadResponse(
            Review review
    ) {
        List<ReviewThreadResponse> replies =
                reviewRepository
                        .findByParentReviewIdAndDeletedAtIsNull(
                                review.getId()
                        )
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

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }
}