package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.Review;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.ReviewRepository;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RepairShopRepository repairShopRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ReviewServiceImpl service;

    @Test
    void create_shouldCreateReviewForCurrentUser() {
        User user = User.builder().id(3L).build();
        RepairShop shop = RepairShop.builder().id(2L).build();
        ReviewCreateRequest request = new ReviewCreateRequest(2L, (short) 5, "  Great service  ");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(repairShopRepository.findById(2L)).thenReturn(Optional.of(shop));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            return review;
        });

        ReviewResponse response = service.create(request);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());

        Review saved = captor.getValue();

        assertEquals(3L, saved.getUser().getId());
        assertEquals(2L, saved.getRepairShop().getId());
        assertEquals(5, saved.getRatingOverall());
        assertEquals("Great service", saved.getCommentText());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getDeletedAt());

        assertEquals(10L, response.id());
        assertEquals(2L, response.repairShopId());
        assertEquals(3L, response.userId());
    }

    @Test
    void create_shouldThrowNotFoundWhenRepairShopDoesNotExist() {
        User user = User.builder().id(3L).build();
        ReviewCreateRequest request = new ReviewCreateRequest(99L, (short) 5, "Great");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(repairShopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnReviewWhenNotDeleted() {
        Review review = createReview(5L, 1L, 2L, (short) 4, "Ok", null);
        when(reviewRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(review));

        ReviewResponse response = service.getById(5L);

        assertEquals(5L, response.id());
        assertEquals(1L, response.repairShopId());
        assertEquals(2L, response.userId());
        assertEquals(4, response.ratingOverall());
    }

    @Test
    void getById_shouldThrowNotFoundWhenMissingOrDeleted() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void list_shouldUseRepairShopFilterWhenOnlyRepairShopIdIsProvided() {
        Review review = createReview(1L, 2L, 3L, (short) 5, "Great", null);

        when(reviewRepository.findByRepairShopIdAndDeletedAtIsNull(2L))
                .thenReturn(List.of(review));

        List<ReviewResponse> result = service.list(2L, null);

        verify(reviewRepository).findByRepairShopIdAndDeletedAtIsNull(2L);
        verify(reviewRepository, never()).findByUserIdAndDeletedAtIsNull(anyLong());

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).repairShopId());
    }

    @Test
    void list_shouldUseUserFilterWhenOnlyUserIdIsProvided() {
        Review review = createReview(1L, 2L, 3L, (short) 5, "Great", null);

        when(reviewRepository.findByUserIdAndDeletedAtIsNull(3L))
                .thenReturn(List.of(review));

        List<ReviewResponse> result = service.list(null, 3L);

        verify(reviewRepository).findByUserIdAndDeletedAtIsNull(3L);
        verify(reviewRepository, never())
                .findByRepairShopIdAndDeletedAtIsNull(anyLong());

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).userId());
    }

    @Test
    void list_shouldApplyRepairShopAndUserFiltersTogether() {
        Review review = createReview(1L, 2L, 3L, (short) 5, "Great", null);

        when(reviewRepository.findByRepairShopIdAndUserIdAndDeletedAtIsNull(2L, 3L))
                .thenReturn(List.of(review));

        List<ReviewResponse> result = service.list(2L, 3L);

        verify(reviewRepository)
                .findByRepairShopIdAndUserIdAndDeletedAtIsNull(2L, 3L);

        verify(reviewRepository, never())
                .findByRepairShopIdAndDeletedAtIsNull(anyLong());

        verify(reviewRepository, never())
                .findByUserIdAndDeletedAtIsNull(anyLong());

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).repairShopId());
        assertEquals(3L, result.get(0).userId());
    }

    @Test
    void list_shouldReturnNonDeletedReviewsWhenThereAreNoFilters() {
        Review review = createReview(1L, 2L, 3L, (short) 5, "Great", null);

        when(reviewRepository.findByDeletedAtIsNull()).thenReturn(List.of(review));

        List<ReviewResponse> result = service.list(null, null);

        verify(reviewRepository).findByDeletedAtIsNull();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void listCurrentUserReviews_shouldUseCurrentUserId() {
        Review review = createReview(1L, 2L, 3L, (short) 5, "Great", null);

        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(reviewRepository.findByUserIdAndDeletedAtIsNull(3L))
                .thenReturn(List.of(review));

        List<ReviewResponse> result = service.listCurrentUserReviews();

        verify(reviewRepository).findByUserIdAndDeletedAtIsNull(3L);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).userId());
    }

    @Test
    void delete_shouldAllowReviewAuthor() {
        Review review = createReview(7L, 2L, 3L, (short) 5, "Great", null);
        User currentUser = User.builder().id(3L).build();

        when(reviewRepository.findByIdAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(review));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.isUser()).thenReturn(true);

        service.delete(7L);

        assertNotNull(review.getDeletedAt());
        verify(reviewRepository).save(review);
    }

    @Test
    void delete_shouldAllowAdmin() {
        Review review = createReview(7L, 2L, 3L, (short) 5, "Great", null);

        when(reviewRepository.findByIdAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(review));
        when(currentUserService.isAdmin()).thenReturn(true);

        service.delete(7L);

        assertNotNull(review.getDeletedAt());
        verify(reviewRepository).save(review);
        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    void delete_shouldDenyDifferentUser() {
        Review review = createReview(7L, 2L, 3L, (short) 5, "Great", null);
        User differentUser = User.builder().id(4L).build();

        when(reviewRepository.findByIdAndDeletedAtIsNull(7L))
                .thenReturn(Optional.of(review));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(currentUserService.isUser()).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.delete(7L));

        assertNull(review.getDeletedAt());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowNotFoundWhenReviewDoesNotExist() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(9L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(9L));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void restore_shouldClearDeletedAtAndSave() {
        Review review = createReview(
                7L, 2L, 3L, (short) 5, "Great", Instant.now());

        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));

        service.restore(7L);

        assertNull(review.getDeletedAt());
        verify(reviewRepository).save(review);
    }

    @Test
    void restore_shouldDoNothingWhenReviewIsNotDeleted() {
        Review review = createReview(7L, 2L, 3L, (short) 5, "Great", null);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));

        service.restore(7L);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void restore_shouldThrowNotFoundWhenReviewDoesNotExist() {
        when(reviewRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.restore(7L));
        verify(reviewRepository, never()).save(any());
    }

    private Review createReview(Long id, Long repairShopId, Long userId, short rating,
                                String comment, Instant deletedAt) {
        return Review.builder()
                .id(id)
                .repairShop(RepairShop.builder().id(repairShopId).build())
                .user(User.builder().id(userId).build())
                .ratingOverall(rating)
                .commentText(comment)
                .createdAt(Instant.now())
                .deletedAt(deletedAt)
                .build();
    }
}