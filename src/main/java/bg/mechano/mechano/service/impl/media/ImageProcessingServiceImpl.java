package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.service.media.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final MediaProperties props;

    @Override
    public ProcessedImage processToJpeg(byte[] inputBytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(inputBytes)) {

            BufferedImage original = ImageIO.read(in);
            if (original == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }

            int maxW = props.getImage().getMaxWidth();
            int maxH = props.getImage().getMaxHeight();
            double quality = props.getImage().getQuality();

            byte[] main = toJpegBytes(original, maxW, maxH, quality);

            int thumbSize = props.getImage().getThumbSize();
            byte[] thumb = toJpegBytes(original, thumbSize, thumbSize, Math.min(quality, 0.78));

            BufferedImage mainImg = ImageIO.read(new ByteArrayInputStream(main));

            return new ProcessedImage(
                    main,
                    thumb,
                    mainImg.getWidth(),
                    mainImg.getHeight(),
                    "image/jpeg"
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    private byte[] toJpegBytes(BufferedImage img, int maxW, int maxH, double quality) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(img)
                .size(maxW, maxH)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(out);
        return out.toByteArray();
    }
}