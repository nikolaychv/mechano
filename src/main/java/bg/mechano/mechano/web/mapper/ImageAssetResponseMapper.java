package bg.mechano.mechano.web.mapper;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.web.dto.media.ImageAssetResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class ImageAssetResponseMapper {

    public ImageAssetResponse toResponse(
            ImageAsset asset
    ) {
        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/images/{id}")
                .buildAndExpand(asset.getId())
                .toUriString();

        return new ImageAssetResponse(
                asset.getId(),
                asset.getOwnerType(),
                asset.getOwnerId(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getWidth() == null
                        ? 0
                        : asset.getWidth(),
                asset.getHeight() == null
                        ? 0
                        : asset.getHeight(),
                baseUrl + "/content",
                baseUrl + "/thumb",
                asset.getCreatedAt()
        );
    }
}