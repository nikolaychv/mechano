package bg.mechano.mechano.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "mechano.media")
public class MediaProperties {

    private String baseDir;
    private long maxUploadBytes;

    /**
     * Allowed content types for uploads (excluding GIF).
     * You can keep it in config to avoid recompiling to change policy.
     */
    private List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp");

    private Image image = new Image();

    @Getter
    @Setter
    public static class Image {
        private int thumbSize = 300;

        /**
         * Applies ONLY when thumbnail format is JPEG.
         */
        private double thumbJpegQuality = 0.85;
    }
}
