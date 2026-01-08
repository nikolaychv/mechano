package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.RepairShopServiceType;
import bg.mechano.mechano.domain.entity.RepairShopServiceTypeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link RepairShopServiceType} associations.
 *
 * Provides data access operations for the relationship between
 * services and the types of services they offer.
 */
public interface RepairShopServiceTypeRepository
        extends JpaRepository<RepairShopServiceType, RepairShopServiceTypeId> {

    List<RepairShopServiceType> findByIdRepairShopId(Long repairShopId);

    List<RepairShopServiceType> findByIdRepairShopTypeId(Long repairShopTypeId);
}

