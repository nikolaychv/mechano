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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageAssetServiceImpl implements ImageAssetService {

    private final MediaProperties props;
    private final ImageAssetRepository repo;
    private final StorageService storage;
    private final ImageProcessingService processing;

    @Override
    @Transactional
    public ImageAsset upload(MultipartFile file, ImageOwnerType ownerType, Long ownerId) {
        validate(file);

        Path temp = null;
        Path thumbTemp = null;

        try {
            // spool to temp file
            temp = Files.createTempFile("mechano-upload-", ".tmp");
            file.transferTo(temp);

            long mainSize = Files.size(temp);

            // determine format from upload meta
            String contentType = normalize(file.getContentType());        // e.g. image/png
            String extension  = resolveExtension(file, contentType);      // e.g. png

            // read metadata
            var info = processing.readInfo(temp);

            // keys
            String folder = ownerType.name().toLowerCase();
            String token = UUID.randomUUID().toString();

            String mainKey = folder + "/" + ownerId + "/" + token + "." + extension;
            String thumbKey = folder + "/" + ownerId + "/" + token + "_thumb." + extension;

            // create thumbnail (derived only)
            int thumbSize = props.getImage().getThumbSize();
            var thumb = processing.createThumbnail(temp, contentType, extension, thumbSize);
            thumbTemp = thumb.thumbnailFile();

            // save MAIN as-is
            try (InputStream in = Files.newInputStream(temp)) {
                storage.save(in, mainSize, contentType, mainKey);
            }

            // save THUMB
            try (InputStream in = Files.newInputStream(thumbTemp)) {
                storage.save(in, thumb.sizeBytes(), thumb.contentType(), thumbKey);
            }

            ImageAsset asset = ImageAsset.builder()
                    .originalFilename(file.getOriginalFilename())
                    .contentType(contentType)
                    .sizeBytes(mainSize)
                    .width(info.width())
                    .height(info.height())
                    .storageKey(mainKey)
                    .thumbStorageKey(thumbKey)
                    .thumbContentType(thumb.contentType())
                    .thumbSizeBytes(thumb.sizeBytes())
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .createdAt(Instant.now())
                    .build();

            return repo.save(asset);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        } finally {
            tryDelete(temp);
            tryDelete(thumbTemp);
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

        String name = file.getOriginalFilename();
        if (name != null && name.toLowerCase().endsWith(".gif")) {
            throw new IllegalArgumentException("GIF is not supported");
        }

        String ct = normalize(file.getContentType());
        if (ct == null) {
            throw new IllegalArgumentException("Missing content type");
        }

        Set<String> allowed = new HashSet<>(props.getAllowedContentTypes());
        if (!allowed.contains(ct)) {
            throw new IllegalArgumentException("Unsupported image type");
        }
    }

    private String resolveExtension(MultipartFile file, String contentType) {
        // Prefer extension from filename, fallback to contentType
        String name = file.getOriginalFilename();
        if (name != null) {
            int dot = name.lastIndexOf('.');
            if (dot > -1 && dot < name.length() - 1) {
                String ext = name.substring(dot + 1).toLowerCase().trim();
                // normalize common variants
                if (ext.equals("jpeg")) ext = "jpg";

                // allow only expected extensions
                if (ext.equals("jpg") || ext.equals("png") || ext.equals("webp")) {
                    if (isExtensionCompatible(ext, contentType)) {
                        return ext;
                    }
                }
            }
        }

        // fallback by content type
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported image type");
        };
    }

    private boolean isExtensionCompatible(String ext, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ext.equals("jpg");
            case "image/png" -> ext.equals("png");
            case "image/webp" -> ext.equals("webp");
            default -> false;
        };
    }

    private String normalize(String s) {
        return s == null ? null : s.toLowerCase().trim();
    }

    private void tryDelete(Path p) {
        if (p == null) return;
        try {
            Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
    }
}