package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.RepairShopCarBrand;
import bg.mechano.mechano.domain.entity.RepairShopCarBrandId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link RepairShopCarBrand} associations.
 *
 * Provides data access operations for the relationship between
 * services and supported car brands.
 */
public interface RepairShopCarBrandRepository
        extends JpaRepository<RepairShopCarBrand, RepairShopCarBrandId> {

    List<RepairShopCarBrand> findByIdRepairShopId(Long repairShopId);

    List<RepairShopCarBrand> findByIdCarBrandId(Long carBrandId);
}
