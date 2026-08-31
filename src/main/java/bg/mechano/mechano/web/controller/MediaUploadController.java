package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.service.media.MediaUploadService;
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

    private final MediaUploadService mediaUploadService;

    @PostMapping(
            value = "/users/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('USER', 'SHOP_OWNER', 'ADMIN')"
    )
    public ImageAssetResponse uploadAvatar(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return toResponse(
                mediaUploadService.uploadAvatar(
                        id,
                        file
                )
        );
    }

    @PostMapping(
            value = "/repair-shops/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('SHOP_OWNER', 'ADMIN')"
    )
    public ImageAssetResponse uploadRepairShopCover(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return toResponse(
                mediaUploadService
                        .uploadRepairShopCover(
                                id,
                                file
                        )
        );
    }

    @PostMapping(
            value = "/reviews/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    public ImageAssetResponse uploadReviewImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return toResponse(
                mediaUploadService.uploadReviewImage(
                        id,
                        file
                )
        );
    }

    @PostMapping(
            value = "/bookings/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('USER', 'SHOP_OWNER', 'ADMIN')"
    )
    public ImageAssetResponse uploadBookingImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return toResponse(
                mediaUploadService.uploadBookingImage(
                        id,
                        file
                )
        );
    }

    private ImageAssetResponse toResponse(
            ImageAsset asset
    ) {
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