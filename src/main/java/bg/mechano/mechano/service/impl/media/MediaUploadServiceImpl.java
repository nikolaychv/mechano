package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.domain.entity.Booking;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.Review;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.BookingRepository;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.ReviewRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.service.media.MediaUploadService;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaUploadServiceImpl
        implements MediaUploadService {

    private final ImageAssetService imageAssetService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final RepairShopRepository repairShopRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ImageAsset uploadAvatar(
            Long userId,
            MultipartFile file
    ) {
        User user = userRepository.findById(userId)
                .filter(u ->
                        u.getDeletedAt() == null
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found: " + userId
                        )
                );

        authorizeAvatarUpload(user);

        ImageAsset asset =
                imageAssetService.upload(
                        file,
                        ImageOwnerType.USER_AVATAR,
                        user.getId()
                );

        user.setAvatarImageId(asset.getId());
        userRepository.save(user);

        return asset;
    }

    @Override
    public ImageAsset uploadRepairShopCover(
            Long repairShopId,
            MultipartFile file
    ) {
        RepairShop shop = repairShopRepository
                .findById(repairShopId)
                .filter(s ->
                        s.getDeletedAt() == null
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "RepairShop not found: "
                                        + repairShopId
                        )
                );

        authorizeRepairShopCoverUpload(shop);

        ImageAsset asset =
                imageAssetService.upload(
                        file,
                        ImageOwnerType.REPAIR_SHOP_COVER,
                        shop.getId()
                );

        shop.setCoverImageId(asset.getId());
        repairShopRepository.save(shop);

        return asset;
    }

    @Override
    public ImageAsset uploadReviewImage(
            Long reviewId,
            MultipartFile file
    ) {
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Review not found: "
                                        + reviewId
                        )
                );

        authorizeReviewImageUpload(review);

        return imageAssetService.upload(
                file,
                ImageOwnerType.REVIEW,
                review.getId()
        );
    }

    @Override
    public ImageAsset uploadBookingImage(
            Long bookingId,
            MultipartFile file
    ) {
        Booking booking = bookingRepository
                .findById(bookingId)
                .filter(b ->
                        b.getDeletedAt() == null
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking not found: "
                                        + bookingId
                        )
                );

        authorizeBookingImageUpload(booking);

        return imageAssetService.upload(
                file,
                ImageOwnerType.BOOKING,
                booking.getId()
        );
    }

    private void authorizeAvatarUpload(
            User user
    ) {
        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (user.getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot upload an avatar "
                        + "for this user."
        );
    }

    private void authorizeRepairShopCoverUpload(
            RepairShop shop
    ) {
        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUserService.isShopOwner()
                && shop.getOwner()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot upload a cover "
                        + "for this repair shop."
        );
    }

    private void authorizeReviewImageUpload(
            Review review
    ) {
        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser =
                currentUserService.getCurrentUser();

        if (currentUserService.isUser()
                && review.getUser()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You cannot upload an image "
                        + "for this review."
        );
    }

    private void authorizeBookingImageUpload(
            Booking booking
    ) {
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
                "You cannot upload an image "
                        + "for this booking."
        );
    }
}