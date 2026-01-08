package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.RepairShopType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link RepairShopType} entities.
 *
 * Provides data access operations for different categories
 * of services available in the platform.
 */
public interface RepairShopTypeRepository extends JpaRepository<RepairShopType, Long> {

    Optional<RepairShopType> findByName(String name);
}
