package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CarBrand} entities.
 *
 * Soft delete behavior:
 * - Standard JPA methods automatically exclude deleted records
 *   via @SQLRestriction("deleted_at IS NULL") in the entity.
 * - Special queries are provided to access deleted records when needed.
 */
public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {

    /**
     * Finds an active car brand by name.
     * Deleted records are automatically excluded.
     */
    Optional<CarBrand> findByName(String name);

    /**
     * Returns all car brands including soft-deleted ones.
     * Intended for administrative use.
     */
    @Query(value = "SELECT * FROM car_brands", nativeQuery = true)
    List<CarBrand> findAllIncludingDeleted();

    /**
     * Finds a car brand by ID including soft-deleted records.
     * Useful for restore operations.
     */
    @Query(value = "SELECT * FROM car_brands WHERE id = :id", nativeQuery = true)
    Optional<CarBrand> findByIdIncludingDeleted(Long id);

    /**
     * Returns only soft-deleted car brands.
     */
    @Query(value = "SELECT * FROM car_brands WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<CarBrand> findAllDeleted();
}