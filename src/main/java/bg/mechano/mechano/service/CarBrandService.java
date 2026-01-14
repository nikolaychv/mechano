package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.carbrand.CarBrandCreateRequest;
import bg.mechano.mechano.web.dto.carbrand.CarBrandResponse;
import bg.mechano.mechano.web.dto.carbrand.CarBrandUpdateRequest;

import java.util.List;

/**
 * Service interface for managing car brands.
 *
 * Supports CRUD operations with soft delete semantics.
 */
public interface CarBrandService {

    CarBrandResponse create(CarBrandCreateRequest request);

    CarBrandResponse getById(Long id);

    List<CarBrandResponse> list();

    CarBrandResponse update(Long id, CarBrandUpdateRequest request);

    /**
     * Soft deletes a car brand.
     * Internally sets deleted_at timestamp.
     */
    void delete(Long id);

    /**
     * Restores a previously soft-deleted car brand.
     */
    CarBrandResponse restore(Long id);

    /**
     * Returns only soft-deleted car brands.
     */
    List<CarBrandResponse> listDeleted();
}