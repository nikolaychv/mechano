package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.RepairShop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link RepairShop} entities.
 *
 * Provides data access operations for auto repair services,
 * including queries based on location and active status.
 */
public interface RepairShopRepository extends JpaRepository<RepairShop, Long> {

    List<RepairShop> findByCityIgnoreCaseAndDeletedAtIsNull(String city);

    List<RepairShop> findByIsActiveTrueAndDeletedAtIsNull();
}