package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.ImageAssetRepository;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.service.media.ImageProcessingService;
import bg.mechano.mechano.service.media.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageAssetServiceImpl implements ImageAssetService {

    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final MediaProperties props;
    private final ImageAssetRepository repo;
    private final StorageService storage;
    private final ImageProcessingService processing;

    @Override
    @Transactional
    public ImageAsset upload(MultipartFile file, ImageOwnerType ownerType, Long ownerId) {
        validate(file);

        try {
            byte[] bytes = file.getBytes();
            var processed = processing.processToJpeg(bytes);

            String folder = ownerType.name().toLowerCase();
            String token = UUID.randomUUID().toString();

            String mainKey = folder + "/" + ownerId + "/" + token + ".jpg";
            String thumbKey = folder + "/" + ownerId + "/" + token + "_thumb.jpg";

            storage.save(processed.mainBytes(), mainKey);
            storage.save(processed.thumbBytes(), thumbKey);

            ImageAsset asset = ImageAsset.builder()
                    .originalFilename(file.getOriginalFilename())
                    .contentType(processed.contentType())
                    .sizeBytes(processed.mainBytes().length)
                    .width(processed.width())
                    .height(processed.height())
                    .storageKey(mainKey)
                    .thumbStorageKey(thumbKey)
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .createdAt(Instant.now())
                    .build();

            return repo.save(asset);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public ImageAsset getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > props.getMaxUploadBytes()) {
            throw new IllegalArgumentException("File too large");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED.contains(ct)) {
            throw new IllegalArgumentException("Unsupported image type");
        }
    }
}