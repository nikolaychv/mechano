package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageProcessingServiceImplTest {

    @Mock
    private MediaProperties mediaProperties;

    private ImageProcessingServiceImpl imageProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        MediaProperties.Image imageProps = new MediaProperties.Image();
        imageProps.setThumbJpegQuality(0.85);

        when(mediaProperties.getImage()).thenReturn(imageProps);

        imageProcessingService = new ImageProcessingServiceImpl(mediaProperties);
    }

    @Test
    void testReadInfo_ValidImage() throws Exception {
        Path imageFile = Files.createTempFile("test-image", ".png");
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        javax.imageio.ImageIO.write(img, "png", imageFile.toFile());

        var result = imageProcessingService.readInfo(imageFile);

        assertNotNull(result);
        assertEquals(100, result.width());
        assertEquals(200, result.height());
    }

    @Test
    void testReadInfo_InvalidImage() {
        Path invalidFile = Path.of("invalid-file.txt");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imageProcessingService.readInfo(invalidFile);
        });

        assertTrue(exception.getMessage().contains("not a valid or supported image"));
    }

    @Test
    void testCreateThumbnail_InvalidImage() {
        Path invalidFile = Path.of("invalid-file.txt");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            imageProcessingService.createThumbnail(invalidFile, "image/png", "png", 100);
        });

        assertTrue(exception.getMessage().contains("Failed to generate thumbnail"));
    }
}
