package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.Review;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.ReviewRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl service;

    @Test
    void getById_shouldReturnReview_whenNotDeleted() {
        Review review = Review.builder()
                .id(5L)
                .repairShop(RepairShop.builder().id(1L).build())
                .user(User.builder().id(2L).build())
                .ratingOverall((short) 4)
                .commentText("Ok")
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(reviewRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(review));

        ReviewResponse response = service.getById(5L);

        assertEquals(5L, response.id());
        assertEquals(4, response.ratingOverall());
    }

    @Test
    void getById_shouldThrowNotFound_whenDeleted() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void list_shouldUseFindByRepairShop_whenRepairShopIdProvided() {
        when(reviewRepository.findByRepairShopIdAndDeletedAtIsNull(1L)).thenReturn(List.of(
                Review.builder()
                        .id(1L)
                        .repairShop(RepairShop.builder().id(1L).build())
                        .user(User.builder().id(2L).build())
                        .deletedAt(null)
                        .createdAt(Instant.now())
                        .build()
        ));

        List<ReviewResponse> result = service.list(1L, null);

        verify(reviewRepository).findByRepairShopIdAndDeletedAtIsNull(1L);
        verify(reviewRepository, never()).findByUserIdAndDeletedAtIsNull(any());
        verify(reviewRepository, never()).findAll();

        assertEquals(1, result.size());
    }

    @Test
    void list_shouldUseFindByUser_whenUserIdProvided() {
        when(reviewRepository.findByUserIdAndDeletedAtIsNull(2L)).thenReturn(List.of(
                Review.builder()
                        .id(2L)
                        .repairShop(RepairShop.builder().id(1L).build())
                        .user(User.builder().id(2L).build())
                        .deletedAt(null)
                        .createdAt(Instant.now())
                        .build()
        ));

        List<ReviewResponse> result = service.list(null, 2L);

        verify(reviewRepository).findByUserIdAndDeletedAtIsNull(2L);
        verify(reviewRepository, never()).findByRepairShopIdAndDeletedAtIsNull(any());
        verify(reviewRepository, never()).findAll();

        assertEquals(1, result.size());
    }

    @Test
    void list_shouldReturnAllNotDeleted_whenNoFilters() {
        Review active = Review.builder()
                .id(1L)
                .repairShop(RepairShop.builder().id(1L).build())
                .user(User.builder().id(2L).build())
                .deletedAt(null)
                .createdAt(Instant.now())
                .build();

        Review deleted = Review.builder()
                .id(2L)
                .deletedAt(Instant.now())
                .build();

        when(reviewRepository.findAll()).thenReturn(List.of(active, deleted));

        List<ReviewResponse> result = service.list(null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(reviewRepository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(9L));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void restore_shouldSetDeletedAtNull_andSave() {
        Review review = Review.builder()
                .id(7L)
                .deletedAt(Instant.now())
                .build();

        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.restore(7L);

        assertNull(review.getDeletedAt());
        verify(reviewRepository).save(review);
    }

    @Test
    void restore_shouldThrowNotFound_whenMissing() {
        when(reviewRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.restore(7L));
        verify(reviewRepository, never()).save(any());
    }
}