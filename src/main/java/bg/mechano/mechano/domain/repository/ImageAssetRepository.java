package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.ImageAsset;
import bg.mechano.mechano.domain.enums.ImageOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {

    List<ImageAsset> findAllByOwnerTypeAndOwnerId(ImageOwnerType ownerType, Long ownerId);
}