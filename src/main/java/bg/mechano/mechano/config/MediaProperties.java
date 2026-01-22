package bg.mechano.mechano.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mechano.media")
public class MediaProperties {
    private String baseDir;
    private long maxUploadBytes;
    private Image image = new Image();

    @Getter @Setter
    public static class Image {
        private int maxWidth;
        private int maxHeight;
        private int thumbSize;
        private double quality;
    }
}
