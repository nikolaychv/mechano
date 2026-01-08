package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.RepairShopService;
import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RepairShopServiceImpl implements RepairShopService {

    private final RepairShopRepository repairShopRepository;
    private final UserRepository userRepository;

    @Override
    public RepairShopResponse create(RepairShopCreateRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new NotFoundException("Owner not found: " + request.ownerId()));

        RepairShop shop = RepairShop.builder()
                .owner(owner)
                .name(request.name().trim())
                .city(request.city().trim())
                .address(trim(request.address()))
                .phone(trim(request.phone()))
                .email(trim(request.email()))
                .website(trim(request.website()))
                .description(trim(request.description()))
                .priceRangeMin(request.priceRangeMin())
                .priceRangeMax(request.priceRangeMax())
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(repairShopRepository.save(shop));
    }

    @Override
    @Transactional(readOnly = true)
    public RepairShopResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairShopResponse> list(String city, Boolean onlyActive) {
        List<RepairShop> shops;

        if (onlyActive != null && onlyActive) {
            shops = repairShopRepository.findByIsActiveTrueAndDeletedAtIsNull();
        } else if (city != null) {
            shops = repairShopRepository.findByCityIgnoreCaseAndDeletedAtIsNull(city);
        } else {
            shops = repairShopRepository.findAll().stream()
                    .filter(s -> s.getDeletedAt() == null)
                    .toList();
        }

        return shops.stream().map(this::toResponse).toList();
    }

    @Override
    public RepairShopResponse update(Long id, RepairShopUpdateRequest request) {
        RepairShop shop = getExisting(id);

        if (request.name() != null) shop.setName(request.name().trim());
        if (request.city() != null) shop.setCity(request.city().trim());
        if (request.address() != null) shop.setAddress(trim(request.address()));
        if (request.phone() != null) shop.setPhone(trim(request.phone()));
        if (request.email() != null) shop.setEmail(trim(request.email()));
        if (request.website() != null) shop.setWebsite(trim(request.website()));
        if (request.description() != null) shop.setDescription(trim(request.description()));
        if (request.priceRangeMin() != null) shop.setPriceRangeMin(request.priceRangeMin());
        if (request.priceRangeMax() != null) shop.setPriceRangeMax(request.priceRangeMax());
        if (request.isActive() != null) shop.setActive(request.isActive());

        return toResponse(repairShopRepository.save(shop));
    }

    @Override
    public void softDelete(Long id) {
        RepairShop shop = getExisting(id);
        shop.setDeletedAt(Instant.now());
        shop.setActive(false);
        repairShopRepository.save(shop);
    }

    private RepairShop getExisting(Long id) {
        RepairShop shop = repairShopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RepairShop not found: " + id));

        if (shop.getDeletedAt() != null) {
            throw new NotFoundException("RepairShop not found: " + id);
        }
        return shop;
    }

    private RepairShopResponse toResponse(RepairShop s) {
        return new RepairShopResponse(
                s.getId(),
                s.getOwner().getId(),
                s.getName(),
                s.getCity(),
                s.getAddress(),
                s.getPhone(),
                s.getEmail(),
                s.getWebsite(),
                s.getDescription(),
                s.getPriceRangeMin(),
                s.getPriceRangeMax(),
                s.isActive(),
                s.getCreatedAt()
        );
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}