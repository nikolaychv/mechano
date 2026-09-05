package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.ImageAssetRepository;
import bg.mechano.mechano.service.media.ImageAssetService;
import bg.mechano.mechano.service.media.ImageProcessingService;
import bg.mechano.mechano.service.media.StorageService;
import bg.mechano.mechano.web.exception.NotFoundException;
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
    public ImageAsset upload(
            MultipartFile file,
            ImageOwnerType ownerType,
            Long ownerId
    ) {
        validate(file);

        Path temp = null;
        Path thumbTemp = null;

        try {
            temp = Files.createTempFile(
                    "mechano-upload-",
                    ".tmp"
            );

            file.transferTo(temp);

            long mainSize = Files.size(temp);

            String contentType =
                    normalize(file.getContentType());

            String extension =
                    resolveExtension(
                            file,
                            contentType
                    );

            var info =
                    processing.readInfo(temp);

            String folder =
                    ownerType
                            .name()
                            .toLowerCase();

            String token =
                    UUID.randomUUID()
                            .toString();

            String mainKey =
                    folder
                            + "/"
                            + ownerId
                            + "/"
                            + token
                            + "."
                            + extension;

            String thumbKey =
                    folder
                            + "/"
                            + ownerId
                            + "/"
                            + token
                            + "_thumb."
                            + extension;

            int thumbSize =
                    props
                            .getImage()
                            .getThumbSize();

            var thumb =
                    processing.createThumbnail(
                            temp,
                            contentType,
                            extension,
                            thumbSize
                    );

            thumbTemp =
                    thumb.thumbnailFile();

            try (InputStream in =
                         Files.newInputStream(temp)) {

                storage.save(
                        in,
                        mainSize,
                        contentType,
                        mainKey
                );
            }

            try (InputStream in =
                         Files.newInputStream(
                                 thumbTemp
                         )) {

                storage.save(
                        in,
                        thumb.sizeBytes(),
                        thumb.contentType(),
                        thumbKey
                );
            }

            ImageAsset asset =
                    ImageAsset.builder()
                            .originalFilename(
                                    file.getOriginalFilename()
                            )
                            .contentType(contentType)
                            .sizeBytes(mainSize)
                            .width(info.width())
                            .height(info.height())
                            .storageKey(mainKey)
                            .thumbStorageKey(
                                    thumbKey
                            )
                            .thumbContentType(
                                    thumb.contentType()
                            )
                            .thumbSizeBytes(
                                    thumb.sizeBytes()
                            )
                            .ownerType(ownerType)
                            .ownerId(ownerId)
                            .createdAt(
                                    Instant.now()
                            )
                            .build();

            return repo.save(asset);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Upload failed",
                    e
            );
        } finally {
            tryDelete(temp);
            tryDelete(thumbTemp);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImageAsset getById(Long id) {
        return repo
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Image not found: " + id
                        )
                );
    }

    private void validate(
            MultipartFile file
    ) {
        if (file == null
                || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is required"
            );
        }

        if (file.getSize()
                > props.getMaxUploadBytes()) {
            throw new IllegalArgumentException(
                    "File too large"
            );
        }

        String name =
                file.getOriginalFilename();

        if (name != null
                && name
                .toLowerCase()
                .endsWith(".gif")) {
            throw new IllegalArgumentException(
                    "GIF is not supported"
            );
        }

        String contentType =
                normalize(
                        file.getContentType()
                );

        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Missing content type"
            );
        }

        Set<String> allowed =
                new HashSet<>(
                        props.getAllowedContentTypes()
                );

        if (!allowed.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported image type"
            );
        }
    }

    private String resolveExtension(
            MultipartFile file,
            String contentType
    ) {
        String name =
                file.getOriginalFilename();

        if (name != null) {
            int dot =
                    name.lastIndexOf('.');

            if (dot > -1
                    && dot < name.length() - 1) {

                String extension =
                        name.substring(dot + 1)
                                .toLowerCase()
                                .trim();

                if (extension.equals("jpeg")) {
                    extension = "jpg";
                }

                if ((extension.equals("jpg")
                        || extension.equals("png")
                        || extension.equals("webp"))
                        && isExtensionCompatible(
                        extension,
                        contentType
                )) {

                    return extension;
                }
            }
        }

        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported image type"
                    );
        };
    }

    private boolean isExtensionCompatible(
            String extension,
            String contentType
    ) {
        return switch (contentType) {
            case "image/jpeg" ->
                    extension.equals("jpg");

            case "image/png" ->
                    extension.equals("png");

            case "image/webp" ->
                    extension.equals("webp");

            default -> false;
        };
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value
                .toLowerCase()
                .trim();
    }

    private void tryDelete(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }
}