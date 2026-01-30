package bg.mechano.mechano.service.impl.media;

import bg.mechano.mechano.config.MediaProperties;
import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.domain.repository.ImageAssetRepository;
import bg.mechano.mechano.service.media.ImageProcessingService;
import bg.mechano.mechano.service.media.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageAssetServiceImplTest {

    @Mock private MediaProperties props;
    @Mock private MediaProperties.Image imageProps;
    @Mock private ImageAssetRepository repo;
    @Mock private StorageService storage;
    @Mock private ImageProcessingService processing;

    private ImageAssetServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ImageAssetServiceImpl(props, repo, storage, processing);
    }

    @Test
    void upload_nullFile_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.upload(null, ImageOwnerType.USER_AVATAR, 1L));
    }

    @Test
    void upload_emptyFile_throws() {
        MultipartFile file = new MockMultipartFile("f", "a.png", "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> service.upload(file, ImageOwnerType.USER_AVATAR, 1L));
    }

    @Test
    void upload_fileTooLarge_throws() {
        when(props.getMaxUploadBytes()).thenReturn(1L);

        MultipartFile file = new MockMultipartFile("f", "a.png", "image/png", new byte[]{1, 2});

        assertThrows(IllegalArgumentException.class,
                () -> service.upload(file, ImageOwnerType.USER_AVATAR, 1L));
    }

    @Test
    void upload_gifRejected_throws() {
        MultipartFile file = new MockMultipartFile("f", "a.gif", "image/gif", new byte[]{1});
        assertThrows(IllegalArgumentException.class,
                () -> service.upload(file, ImageOwnerType.USER_AVATAR, 1L));
    }

    @Test
    void upload_missingContentType_throws() {
        MultipartFile file = new MockMultipartFile("f", "a.png", null, new byte[]{1});
        assertThrows(IllegalArgumentException.class,
                () -> service.upload(file, ImageOwnerType.USER_AVATAR, 1L));
    }

    @Test
    void upload_happyPath_png() throws Exception {
        when(props.getMaxUploadBytes()).thenReturn(10_000_000L);
        when(props.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png", "image/webp"));
        when(props.getImage()).thenReturn(imageProps);
        when(imageProps.getThumbSize()).thenReturn(256);

        byte[] content = new byte[]{1, 2, 3, 4};
        MultipartFile file = new MockMultipartFile("f", "pic.png", "IMAGE/PNG", content);

        var info = mock(ImageProcessingService.ImageInfo.class);
        when(info.width()).thenReturn(800);
        when(info.height()).thenReturn(600);
        when(processing.readInfo(any(Path.class))).thenReturn(info);

        Path thumbFile = Files.createTempFile(tempDir, "thumb-", ".tmp");
        Files.write(thumbFile, new byte[]{9, 9});

        var thumb = mock(ImageProcessingService.ThumbnailResult.class);
        when(thumb.thumbnailFile()).thenReturn(thumbFile);
        when(thumb.sizeBytes()).thenReturn(2L);
        when(thumb.contentType()).thenReturn("image/png");

        when(processing.createThumbnail(any(Path.class), eq("image/png"), eq("png"), eq(256)))
                .thenReturn(thumb);

        when(repo.save(any(ImageAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        ImageAsset out = service.upload(file, ImageOwnerType.REPAIR_SHOP_COVER, 42L);

        assertEquals("pic.png", out.getOriginalFilename());
        assertEquals("image/png", out.getContentType());
        assertEquals(800, out.getWidth());
        assertEquals(600, out.getHeight());
        assertEquals(42L, out.getOwnerId());
        assertEquals(ImageOwnerType.REPAIR_SHOP_COVER, out.getOwnerType());
        assertTrue(out.getStorageKey().startsWith("repair_shop_cover/42/"));
        assertTrue(out.getStorageKey().endsWith(".png"));
        assertTrue(out.getThumbStorageKey().startsWith("repair_shop_cover/42/"));
        assertTrue(out.getThumbStorageKey().endsWith("_thumb.png"));

        verify(storage, times(2)).save(any(InputStream.class), anyLong(), anyString(), anyString());
        verify(repo).save(any(ImageAsset.class));
    }

    @Test
    void upload_jpegFilenameNormalizedToJpg() throws Exception {
        when(props.getMaxUploadBytes()).thenReturn(10_000_000L);
        when(props.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png", "image/webp"));
        when(props.getImage()).thenReturn(imageProps);
        when(imageProps.getThumbSize()).thenReturn(256);

        MultipartFile file = new MockMultipartFile("f", "photo.jpeg", "image/jpeg", new byte[]{1, 2, 3});

        var info = mock(ImageProcessingService.ImageInfo.class);
        when(info.width()).thenReturn(100);
        when(info.height()).thenReturn(100);
        when(processing.readInfo(any(Path.class))).thenReturn(info);

        Path thumbFile = Files.createTempFile(tempDir, "thumb-", ".tmp");
        Files.write(thumbFile, new byte[]{5});

        var thumb = mock(ImageProcessingService.ThumbnailResult.class);
        when(thumb.thumbnailFile()).thenReturn(thumbFile);
        when(thumb.sizeBytes()).thenReturn(1L);
        when(thumb.contentType()).thenReturn("image/jpeg");

        when(processing.createThumbnail(any(Path.class), eq("image/jpeg"), eq("jpg"), eq(256)))
                .thenReturn(thumb);

        when(repo.save(any(ImageAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        ImageAsset out = service.upload(file, ImageOwnerType.USER_AVATAR, 7L);

        assertTrue(out.getStorageKey().startsWith("user_avatar/7/"));
        assertTrue(out.getStorageKey().endsWith(".jpg"));
        assertTrue(out.getThumbStorageKey().startsWith("user_avatar/7/"));
        assertTrue(out.getThumbStorageKey().endsWith("_thumb.jpg"));
    }

    @Test
    void upload_filenameExtensionIncompatible_fallsBackToContentType() throws Exception {
        when(props.getMaxUploadBytes()).thenReturn(10_000_000L);
        when(props.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png", "image/webp"));
        when(props.getImage()).thenReturn(imageProps);
        when(imageProps.getThumbSize()).thenReturn(256);

        MultipartFile file = new MockMultipartFile("f", "x.png", "image/jpeg", new byte[]{1, 2, 3});

        var info = mock(ImageProcessingService.ImageInfo.class);
        when(info.width()).thenReturn(10);
        when(info.height()).thenReturn(20);
        when(processing.readInfo(any(Path.class))).thenReturn(info);

        Path thumbFile = Files.createTempFile(tempDir, "thumb-", ".tmp");
        Files.write(thumbFile, new byte[]{6});

        var thumb = mock(ImageProcessingService.ThumbnailResult.class);
        when(thumb.thumbnailFile()).thenReturn(thumbFile);
        when(thumb.sizeBytes()).thenReturn(1L);
        when(thumb.contentType()).thenReturn("image/jpeg");

        when(processing.createThumbnail(any(Path.class), eq("image/jpeg"), eq("jpg"), eq(256)))
                .thenReturn(thumb);

        when(repo.save(any(ImageAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        ImageAsset out = service.upload(file, ImageOwnerType.REVIEW, 5L);

        assertTrue(out.getStorageKey().startsWith("review/5/"));
        assertTrue(out.getStorageKey().endsWith(".jpg"));
        assertTrue(out.getThumbStorageKey().endsWith("_thumb.jpg"));
    }

    @Test
    void upload_missingFilenameExtension_usesContentType() throws Exception {
        when(props.getMaxUploadBytes()).thenReturn(10_000_000L);
        when(props.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png", "image/webp"));
        when(props.getImage()).thenReturn(imageProps);
        when(imageProps.getThumbSize()).thenReturn(256);

        MultipartFile file = new MockMultipartFile("f", "noext", "image/webp", new byte[]{1, 2, 3, 4});

        var info = mock(ImageProcessingService.ImageInfo.class);
        when(info.width()).thenReturn(1);
        when(info.height()).thenReturn(2);
        when(processing.readInfo(any(Path.class))).thenReturn(info);

        Path thumbFile = Files.createTempFile(tempDir, "thumb-", ".tmp");
        Files.write(thumbFile, new byte[]{9});

        var thumb = mock(ImageProcessingService.ThumbnailResult.class);
        when(thumb.thumbnailFile()).thenReturn(thumbFile);
        when(thumb.sizeBytes()).thenReturn(1L);
        when(thumb.contentType()).thenReturn("image/webp");

        when(processing.createThumbnail(any(Path.class), eq("image/webp"), eq("webp"), eq(256)))
                .thenReturn(thumb);

        when(repo.save(any(ImageAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        ImageAsset out = service.upload(file, ImageOwnerType.BOOKING, 9L);

        assertTrue(out.getStorageKey().startsWith("booking/9/"));
        assertTrue(out.getStorageKey().endsWith(".webp"));
        assertTrue(out.getThumbStorageKey().endsWith("_thumb.webp"));
    }

    @Test
    void upload_processingReadInfoThrows_wrapsRuntimeException() throws Exception {
        when(props.getMaxUploadBytes()).thenReturn(10_000_000L);
        when(props.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png", "image/webp"));

        MultipartFile file = new MockMultipartFile("f", "pic.png", "image/png", new byte[]{1, 2, 3});

        when(processing.readInfo(any(Path.class))).thenThrow(new RuntimeException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.upload(file, ImageOwnerType.REVIEW, 1L));

        assertEquals("Upload failed", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void getById_found() {
        ImageAsset asset = ImageAsset.builder().id(1L).build();
        when(repo.findById(1L)).thenReturn(Optional.of(asset));

        ImageAsset out = service.getById(1L);

        assertSame(asset, out);
    }

    @Test
    void getById_notFound_throws() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }
}
