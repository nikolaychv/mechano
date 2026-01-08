package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link CarBrand} entities.
 *
 * Provides data access operations related to supported vehicle brands.
 */
public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {

    Optional<CarBrand> findByName(String name);
}
