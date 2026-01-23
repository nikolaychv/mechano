package bg.mechano.mechano.web.dto.media;

import bg.mechano.mechano.domain.enums.ImageOwnerType;

import java.time.Instant;

public record ImageAssetResponse(
        Long id,
        ImageOwnerType ownerType,
        Long ownerId,
        String contentType,
        long sizeBytes,
        int width,
        int height,
        String url,
        String thumbUrl,
        Instant createdAt
) {}