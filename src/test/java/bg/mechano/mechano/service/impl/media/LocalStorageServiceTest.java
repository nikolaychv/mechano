package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalStorageServiceTest {

    private LocalStorageService localStorageService;

    @Mock
    private MediaProperties mediaProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        localStorageService = new LocalStorageService(mediaProperties);
    }

    @Test
    void testSaveValidFile() throws Exception {
        String baseDir = "test-storage";
        String relativeKey = "test/file.txt";
        InputStream inputStream = new ByteArrayInputStream("Test content".getBytes());

        when(mediaProperties.getBaseDir()).thenReturn(baseDir);

        String savedKey = localStorageService.save(inputStream, 12, "text/plain", relativeKey);

        assertEquals(relativeKey, savedKey);
        Path savedFilePath = Path.of(baseDir, relativeKey);
        assertTrue(Files.exists(savedFilePath));

        // Cleanup
        Files.deleteIfExists(savedFilePath);
        Files.deleteIfExists(savedFilePath.getParent());
        Files.deleteIfExists(Path.of(baseDir));
    }

    @Test
    void testSaveInvalidPathTraversal() {
        String baseDir = "test-storage";
        String relativeKey = "../invalid/file.txt";
        InputStream inputStream = new ByteArrayInputStream("Test content".getBytes());

        when(mediaProperties.getBaseDir()).thenReturn(baseDir);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                localStorageService.save(inputStream, 12, "text/plain", relativeKey));

        assertEquals("Invalid storage key", exception.getMessage());
    }

    @Test
    void testLoadAsResource() throws Exception {
        String baseDir = "test-storage";
        String relativeKey = "test/file.txt";
        Path filePath = Path.of(baseDir, relativeKey);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "Test content");

        when(mediaProperties.getBaseDir()).thenReturn(baseDir);

        Resource resource = localStorageService.loadAsResource(relativeKey);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals("Test content", new String(resource.getInputStream().readAllBytes()));

        // Cleanup
        Files.deleteIfExists(filePath);
        Files.deleteIfExists(filePath.getParent());
        Files.deleteIfExists(Path.of(baseDir));
    }
}
