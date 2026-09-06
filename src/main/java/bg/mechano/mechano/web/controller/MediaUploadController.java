package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.media.MediaUploadService;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import bg.mechano.mechano.web.mapper.ImageAssetResponseMapper;
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
    private final ImageAssetResponseMapper
            imageAssetResponseMapper;

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
        return imageAssetResponseMapper.toResponse(
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
        return imageAssetResponseMapper.toResponse(
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
        return imageAssetResponseMapper.toResponse(
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
        return imageAssetResponseMapper.toResponse(
                mediaUploadService.uploadBookingImage(
                        id,
                        file
                )
        );
    }
}