package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MediaUploadController {

    private final ImageAssetService imageService;
    private final UserRepository userRepository;
    private final RepairShopRepository repairShopRepository;

    @PostMapping(
            value = "/users/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ImageAssetResponse uploadAvatar(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        var user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        ImageAsset asset = imageService.upload(
                file,
                ImageOwnerType.USER_AVATAR,
                id
        );

        user.setAvatarImageId(asset.getId());
        userRepository.save(user);

        return toResponse(asset);
    }

    @PostMapping(
            value = "/repair-shops/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ImageAssetResponse uploadRepairShopCover(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        var shop = repairShopRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RepairShop not found"
                        )
                );

        ImageAsset asset = imageService.upload(
                file,
                ImageOwnerType.REPAIR_SHOP_COVER,
                id
        );

        shop.setCoverImageId(asset.getId());
        repairShopRepository.save(shop);

        return toResponse(asset);
    }

    @PostMapping(
            value = "/reviews/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ImageAssetResponse uploadReviewImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        ImageAsset asset = imageService.upload(
                file,
                ImageOwnerType.REVIEW,
                id
        );

        return toResponse(asset);
    }

    @PostMapping(
            value = "/bookings/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ImageAssetResponse uploadBookingImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        ImageAsset asset = imageService.upload(
                file,
                ImageOwnerType.BOOKING,
                id
        );

        return toResponse(asset);
    }

    private ImageAssetResponse toResponse(ImageAsset asset) {
        return new ImageAssetResponse(
                asset.getId(),
                asset.getOwnerType(),
                asset.getOwnerId(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getWidth() == null
                        ? 0
                        : asset.getWidth(),
                asset.getHeight() == null
                        ? 0
                        : asset.getHeight(),
                "http://localhost:8080/api/images/"
                        + asset.getId()
                        + "/content",
                "http://localhost:8080/api/images/"
                        + asset.getId()
                        + "/thumb",
                asset.getCreatedAt()
        );
    }
}