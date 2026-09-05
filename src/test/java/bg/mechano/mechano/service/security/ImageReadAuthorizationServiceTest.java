package bg.mechano.mechano.service.security;

import bg.mechano.mechano.domain.entity.Booking;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.BookingRepository;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageReadAuthorizationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ImageReadAuthorizationService service;

    @Test
    void authorize_shouldAllowAuthenticatedImageTypes() {
        for (ImageOwnerType ownerType : new ImageOwnerType[]{
                ImageOwnerType.USER_AVATAR,
                ImageOwnerType.REPAIR_SHOP_COVER,
                ImageOwnerType.REVIEW
        }) {
            ImageAsset asset = ImageAsset.builder()
                    .ownerType(ownerType)
                    .ownerId(1L)
                    .build();

            assertDoesNotThrow(
                    () -> service.authorize(asset)
            );
        }

        verifyNoInteractions(
                currentUserService,
                bookingRepository
        );
    }

    @Test
    void authorize_shouldAllowAdminForBookingImage() {
        ImageAsset asset = bookingImage(4L);
        Booking booking = booking(
                4L,
                3L,
                2L
        );

        when(bookingRepository.findById(4L))
                .thenReturn(Optional.of(booking));

        when(currentUserService.isAdmin())
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.authorize(asset)
        );
    }

    @Test
    void authorize_shouldAllowBookingClient() {
        ImageAsset asset = bookingImage(4L);

        Booking booking = booking(
                4L,
                3L,
                2L
        );

        User currentUser = user(3L);

        when(bookingRepository.findById(4L))
                .thenReturn(Optional.of(booking));

        when(currentUserService.isAdmin())
                .thenReturn(false);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(currentUserService.isUser())
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.authorize(asset)
        );
    }

    @Test
    void authorize_shouldAllowRepairShopOwner() {
        ImageAsset asset = bookingImage(4L);

        Booking booking = booking(
                4L,
                3L,
                2L
        );

        User currentUser = user(2L);

        when(bookingRepository.findById(4L))
                .thenReturn(Optional.of(booking));

        when(currentUserService.isAdmin())
                .thenReturn(false);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(currentUserService.isUser())
                .thenReturn(false);

        when(currentUserService.isShopOwner())
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.authorize(asset)
        );
    }

    @Test
    void authorize_shouldDenyUnrelatedUser() {
        ImageAsset asset = bookingImage(4L);

        Booking booking = booking(
                4L,
                3L,
                2L
        );

        User currentUser = user(99L);

        when(bookingRepository.findById(4L))
                .thenReturn(Optional.of(booking));

        when(currentUserService.isAdmin())
                .thenReturn(false);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(currentUserService.isUser())
                .thenReturn(true);

        when(currentUserService.isShopOwner())
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.authorize(asset)
        );
    }

    @Test
    void authorize_shouldReturnNotFound_whenBookingMissing() {
        ImageAsset asset = bookingImage(999L);

        when(bookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.authorize(asset)
        );

        verifyNoInteractions(currentUserService);
    }

    private ImageAsset bookingImage(
            Long bookingId
    ) {
        return ImageAsset.builder()
                .ownerType(ImageOwnerType.BOOKING)
                .ownerId(bookingId)
                .build();
    }

    private Booking booking(
            Long bookingId,
            Long clientId,
            Long shopOwnerId
    ) {
        User client = user(clientId);
        User owner = user(shopOwnerId);

        RepairShop shop = RepairShop.builder()
                .id(1L)
                .owner(owner)
                .build();

        return Booking.builder()
                .id(bookingId)
                .client(client)
                .repairShop(shop)
                .build();
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }
}