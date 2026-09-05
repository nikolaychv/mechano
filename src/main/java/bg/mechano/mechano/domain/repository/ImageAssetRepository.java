package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageAssetRepository
        extends JpaRepository<ImageAsset, Long> {

    Optional<ImageAsset> findByIdAndDeletedAtIsNull(Long id);
}