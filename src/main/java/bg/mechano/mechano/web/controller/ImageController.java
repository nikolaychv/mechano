package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.service.media.StorageService;
import bg.mechano.mechano.service.security.ImageReadAuthorizationService;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import bg.mechano.mechano.web.mapper.ImageAssetResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageAssetService imageService;
    private final StorageService storage;
    private final ImageReadAuthorizationService
            imageReadAuthorizationService;
    private final ImageAssetResponseMapper
            imageAssetResponseMapper;

    @GetMapping("/{id}")
    public ImageAssetResponse getMeta(
            @PathVariable Long id
    ) {
        ImageAsset asset =
                getAuthorizedAsset(id);

        return imageAssetResponseMapper
                .toResponse(asset);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getContent(
            @PathVariable Long id
    ) {
        ImageAsset asset =
                getAuthorizedAsset(id);

        Resource resource =
                storage.loadAsResource(
                        asset.getStorageKey()
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                asset.getContentType()
                        )
                )
                .cacheControl(
                        cacheControl(asset)
                )
                .body(resource);
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> getThumb(
            @PathVariable Long id
    ) {
        ImageAsset asset =
                getAuthorizedAsset(id);

        if (asset.getThumbStorageKey() == null
                || asset.getThumbStorageKey()
                .isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Resource resource =
                storage.loadAsResource(
                        asset.getThumbStorageKey()
                );

        String contentType =
                asset.getThumbContentType() != null
                        ? asset.getThumbContentType()
                        : asset.getContentType();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                contentType
                        )
                )
                .cacheControl(
                        cacheControl(asset)
                )
                .body(resource);
    }

    private ImageAsset getAuthorizedAsset(
            Long id
    ) {
        ImageAsset asset =
                imageService.getById(id);

        imageReadAuthorizationService
                .authorize(asset);

        return asset;
    }

    private CacheControl cacheControl(
            ImageAsset asset
    ) {
        if (asset.getOwnerType()
                == ImageOwnerType.BOOKING) {

            return CacheControl.noStore();
        }

        return CacheControl
                .maxAge(Duration.ofDays(30))
                .cachePrivate();
    }
}