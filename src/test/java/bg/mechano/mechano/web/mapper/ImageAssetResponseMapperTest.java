package bg.mechano.mechano.web.mapper;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageAssetResponseMapperTest {

    private final ImageAssetResponseMapper mapper =
            new ImageAssetResponseMapper();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toResponse_shouldUseCurrentRequestHost() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setScheme("https");
        request.setServerName("api.mechano.test");
        request.setServerPort(8443);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        Instant createdAt = Instant.now();

        ImageAsset asset = ImageAsset.builder()
                .id(16L)
                .ownerType(ImageOwnerType.BOOKING)
                .ownerId(4L)
                .contentType("image/jpeg")
                .sizeBytes(11205L)
                .width(400)
                .height(400)
                .createdAt(createdAt)
                .build();

        ImageAssetResponse response =
                mapper.toResponse(asset);

        assertEquals(
                16L,
                response.id()
        );

        assertEquals(
                ImageOwnerType.BOOKING,
                response.ownerType()
        );

        assertEquals(
                4L,
                response.ownerId()
        );

        assertEquals(
                "https://api.mechano.test:8443"
                        + "/api/images/16/content",
                response.url()
        );

        assertEquals(
                "https://api.mechano.test:8443"
                        + "/api/images/16/thumb",
                response.thumbUrl()
        );

        assertEquals(
                createdAt,
                response.createdAt()
        );
    }

    @Test
    void toResponse_shouldConvertNullDimensionsToZero() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        ImageAsset asset = ImageAsset.builder()
                .id(20L)
                .ownerType(
                        ImageOwnerType.USER_AVATAR
                )
                .ownerId(3L)
                .contentType("image/png")
                .sizeBytes(1000L)
                .width(null)
                .height(null)
                .createdAt(Instant.now())
                .build();

        ImageAssetResponse response =
                mapper.toResponse(asset);

        assertEquals(
                0,
                response.width()
        );

        assertEquals(
                0,
                response.height()
        );
    }
}