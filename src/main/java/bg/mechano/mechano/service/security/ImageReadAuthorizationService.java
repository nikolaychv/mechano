package bg.mechano.mechano.service.security;

import bg.mechano.mechano.domain.entity.Booking;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.BookingRepository;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageReadAuthorizationService {

    private final CurrentUserService currentUserService;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public void authorize(ImageAsset asset) {
        switch (asset.getOwnerType()) {
            case USER_AVATAR,
                 REPAIR_SHOP_COVER,
                 REVIEW -> {
                // These image types are visible to authenticated users.
            }

            case BOOKING -> authorizeBookingImage(asset);
        }
    }

    private void authorizeBookingImage(ImageAsset asset) {
        Booking booking = bookingRepository
                .findById(asset.getOwnerId())
                .filter(value ->
                        value.getDeletedAt() == null
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking not found: "
                                        + asset.getOwnerId()
                        )
                );

        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUserService.isUser()
                && booking.getClient()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        if (currentUserService.isShopOwner()
                && booking.getRepairShop()
                .getOwner()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot access this booking image."
        );
    }
}