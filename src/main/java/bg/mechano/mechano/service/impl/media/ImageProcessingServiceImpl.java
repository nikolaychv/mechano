package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.service.media.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final MediaProperties props;

    @Override
    public ImageInfo readInfo(Path imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile.toFile());
            if (img == null) {
                throw new IllegalArgumentException("File is not a valid or supported image");
            }
            return new ImageInfo(img.getWidth(), img.getHeight());
        } catch (Exception e) {
            throw new IllegalArgumentException("File is not a valid or supported image", e);
        }
    }

    @Override
    public ThumbnailResult createThumbnail(Path originalFile, String originalContentType, String extension, int thumbSize) {
        try {
            String format = normalizeFormat(extension);

            Path thumb = Files.createTempFile("mechano-thumb-", ".tmp");

            try (OutputStream out = Files.newOutputStream(thumb)) {
                var builder = Thumbnails.of(originalFile.toFile())
                        .size(thumbSize, thumbSize)
                        .keepAspectRatio(true)
                        .outputFormat(format);

                // Only for JPEG thumbs
                if ("jpg".equals(format) || "jpeg".equals(format)) {
                    builder.outputQuality(props.getImage().getThumbJpegQuality());
                }

                builder.toOutputStream(out);
            }

            long sizeBytes = Files.size(thumb);

            BufferedImage thumbImg = ImageIO.read(thumb.toFile());
            if (thumbImg == null) {
                throw new IllegalStateException("Failed to read generated thumbnail");
            }

            // thumb content type same as original
            String ct = normalizeContentType(originalContentType);

            return new ThumbnailResult(
                    thumb,
                    sizeBytes,
                    thumbImg.getWidth(),
                    thumbImg.getHeight(),
                    ct
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate thumbnail", e);
        }
    }

    private String normalizeFormat(String ext) {
        if (ext == null) throw new IllegalArgumentException("Missing extension");
        ext = ext.toLowerCase().trim();
        if (ext.equals("jpeg")) ext = "jpg";

        return switch (ext) {
            case "jpg" -> "jpg";
            case "png" -> "png";
            case "webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported image extension: " + ext);
        };
    }

    private String normalizeContentType(String ct) {
        return ct == null ? "" : ct.toLowerCase().trim();
    }
}