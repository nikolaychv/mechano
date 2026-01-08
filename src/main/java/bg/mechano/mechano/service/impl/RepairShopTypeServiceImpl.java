package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShopType;
import bg.mechano.mechano.domain.repository.RepairShopTypeRepository;
import bg.mechano.mechano.service.RepairShopTypeService;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeCreateRequest;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeResponse;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RepairShopTypeServiceImpl implements RepairShopTypeService {

    private final RepairShopTypeRepository repairShopTypeRepository;

    @Override
    public RepairShopTypeResponse create(RepairShopTypeCreateRequest request) {
        String name = normalizeName(request.name());

        repairShopTypeRepository.findByName(name).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RepairShopType already exists: " + name);
        });

        RepairShopType type = RepairShopType.builder()
                .name(name)
                .description(normalizeNullable(request.description()))
                .build();

        return toResponse(repairShopTypeRepository.save(type));
    }

    @Override
    @Transactional(readOnly = true)
    public RepairShopTypeResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairShopTypeResponse> list() {
        return repairShopTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RepairShopTypeResponse update(Long id, RepairShopTypeUpdateRequest request) {
        RepairShopType type = getExisting(id);

        String name = normalizeName(request.name());

        repairShopTypeRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "RepairShopType already exists: " + name);
            }
        });

        type.setName(name);
        type.setDescription(normalizeNullable(request.description()));

        return toResponse(repairShopTypeRepository.save(type));
    }

    @Override
    public void delete(Long id) {
        RepairShopType type = getExisting(id);
        repairShopTypeRepository.delete(type);
    }

    private RepairShopType getExisting(Long id) {
        return repairShopTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RepairShopType not found: " + id));
    }

    private RepairShopTypeResponse toResponse(RepairShopType type) {
        return new RepairShopTypeResponse(
                type.getId(),
                type.getName(),
                type.getDescription()
        );
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        String n = name.trim();
        if (n.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }
        return n;
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}