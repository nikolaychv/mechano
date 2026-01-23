package bg.mechano.mechano.service.media;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import org.springframework.web.multipart.MultipartFile;

public interface ImageAssetService {
    ImageAsset upload(MultipartFile file, ImageOwnerType ownerType, Long ownerId);
    ImageAsset getById(Long id);
}