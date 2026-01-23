package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.service.media.StorageService;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageAssetService imageService;
    private final StorageService storage;

    @GetMapping("/{id}")
    public ImageAssetResponse getMeta(@PathVariable Long id) {
        ImageAsset a = imageService.getById(id);
        return toResponse(a);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getContent(@PathVariable Long id) {
        ImageAsset a = imageService.getById(id);
        Resource r = storage.loadAsResource(a.getStorageKey());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(r);
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> getThumb(@PathVariable Long id) {
        ImageAsset a = imageService.getById(id);
        Resource r = storage.loadAsResource(a.getThumbStorageKey());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(r);
    }

    private ImageAssetResponse toResponse(ImageAsset a) {
        return new ImageAssetResponse(
                a.getId(),
                a.getOwnerType(),
                a.getOwnerId(),
                a.getContentType(),
                a.getSizeBytes(),
                a.getWidth() == null ? 0 : a.getWidth(),
                a.getHeight() == null ? 0 : a.getHeight(),
                "http://localhost:8080/api/images/" + a.getId() + "/content",
                "http://localhost:8080/api/images/" + a.getId() + "/thumb",
                a.getCreatedAt()
        );
    }
}