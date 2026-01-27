package bg.mechano.mechano.service.media;

import java.nio.file.Path;

public interface ImageProcessingService {

    ImageInfo readInfo(Path imageFile);

    ThumbnailResult createThumbnail(Path originalFile, String originalContentType, String extension, int thumbSize);

    record ImageInfo(int width, int height) {}

    record ThumbnailResult(
            Path thumbnailFile,
            long sizeBytes,
            int width,
            int height,
            String contentType
    ) {}
}
