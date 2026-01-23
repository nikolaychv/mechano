package bg.mechano.mechano.service.media;

public interface ImageProcessingService {

    ProcessedImage processToJpeg(byte[] inputBytes);

    record ProcessedImage(
            byte[] mainBytes,
            byte[] thumbBytes,
            int width,
            int height,
            String contentType
    ) {}
}