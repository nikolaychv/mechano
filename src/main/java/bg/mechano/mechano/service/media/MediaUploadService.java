package bg.mechano.mechano.service.media;

import bg.mechano.mechano.domain.entity.ImageAsset;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadService {

    ImageAsset uploadAvatar(
            Long userId,
            MultipartFile file
    );

    ImageAsset uploadRepairShopCover(
            Long repairShopId,
            MultipartFile file
    );

    ImageAsset uploadReviewImage(
            Long reviewId,
            MultipartFile file
    );

    ImageAsset uploadBookingImage(
            Long bookingId,
            MultipartFile file
    );
}