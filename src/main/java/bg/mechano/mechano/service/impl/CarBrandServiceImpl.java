package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.CarBrand;
import bg.mechano.mechano.domain.repository.CarBrandRepository;
import bg.mechano.mechano.service.CarBrandService;
import bg.mechano.mechano.web.dto.carbrand.CarBrandCreateRequest;
import bg.mechano.mechano.web.dto.carbrand.CarBrandResponse;
import bg.mechano.mechano.web.dto.carbrand.CarBrandUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
@Transactional
public class CarBrandServiceImpl implements CarBrandService {

    private final CarBrandRepository carBrandRepository;

    @Override
    public CarBrandResponse create(CarBrandCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Request body is required.");
        }

        String name = normalize(request.name());
        if (name == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Car brand name is required.");
        }

        // With soft delete + @SQLRestriction, this checks only active records.
        carBrandRepository.findByName(name).ifPresent(existing -> {
            throw new ResponseStatusException(CONFLICT, "Car brand already exists: " + name);
        });

        CarBrand brand = CarBrand.builder()
                .name(name)
                .build();

        return toResponse(carBrandRepository.save(brand));
    }

    @Override
    @Transactional(readOnly = true)
    public CarBrandResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarBrandResponse> list() {
        return carBrandRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CarBrandResponse update(Long id, CarBrandUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Request body is required.");
        }

        String name = normalize(request.name());
        if (name == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Car brand name is required.");
        }

        CarBrand brand = getExisting(id);

        carBrandRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ResponseStatusException(CONFLICT, "Car brand already exists: " + name);
            }
        });

        brand.setName(name);
        return toResponse(carBrandRepository.save(brand));
    }

    @Override
    public void delete(Long id) {
        CarBrand brand = getExisting(id);
        carBrandRepository.delete(brand); // triggers @SQLDelete => sets deleted_at
    }

    @Override
    public CarBrandResponse restore(Long id) {
        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Id is required.");
        }

        CarBrand brand = carBrandRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new NotFoundException("CarBrand not found: " + id));

        if (brand.getDeletedAt() == null) {
            return toResponse(brand); // already active
        }

        // Prevent unique index violation: (name) must be unique among ACTIVE rows
        carBrandRepository.findByName(brand.getName()).ifPresent(active -> {
            throw new ResponseStatusException(CONFLICT,
                    "Cannot restore car brand '" + brand.getName()
                            + "'. An active car brand with the same name already exists (id=" + active.getId() + ").");
        });

        brand.setDeletedAt(null);
        return toResponse(carBrandRepository.save(brand));
    }

    /**
     * Optional: List only deleted car brands (admin use).
     */
    @Override
    @Transactional(readOnly = true)
    public List<CarBrandResponse> listDeleted() {
        return carBrandRepository.findAllDeleted().stream()
                .map(this::toResponse)
                .toList();
    }

    private CarBrand getExisting(Long id) {
        if (id == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Id is required.");
        }
        return carBrandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CarBrand not found: " + id));
    }

    private CarBrandResponse toResponse(CarBrand brand) {
        return new CarBrandResponse(
                brand.getId(),
                brand.getName()
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isBlank() ? null : v;
    }
}
