package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.RepairShop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepairShopRepository extends JpaRepository<RepairShop, Long> {

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<RepairShop> findById(Long id);

    @EntityGraph(attributePaths = "owner")
    List<RepairShop> findByCityIgnoreCaseAndDeletedAtIsNull(String city);

    @EntityGraph(attributePaths = "owner")
    List<RepairShop> findByIsActiveTrueAndDeletedAtIsNull();

    @EntityGraph(attributePaths = "owner")
    List<RepairShop> findByCityIgnoreCaseAndIsActiveTrueAndDeletedAtIsNull(String city);

    @EntityGraph(attributePaths = "owner")
    List<RepairShop> findByDeletedAtIsNull();

    @EntityGraph(attributePaths = "owner")
    List<RepairShop> findByOwnerIdAndDeletedAtIsNull(Long ownerId);
}