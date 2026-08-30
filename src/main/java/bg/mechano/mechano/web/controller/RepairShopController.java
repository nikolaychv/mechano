package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.RepairShopService;
import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repair-shops")
public class RepairShopController {

    private final RepairShopService repairShopService;

    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RepairShopResponse create(
            @Valid @RequestBody RepairShopCreateRequest request
    ) {
        return repairShopService.create(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public List<RepairShopResponse> getCurrentOwnerRepairShops() {
        return repairShopService
                .listCurrentOwnerRepairShops();
    }

    @GetMapping("/{id}")
    public RepairShopResponse getById(
            @PathVariable Long id
    ) {
        return repairShopService.getById(id);
    }

    @GetMapping
    public List<RepairShopResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean onlyActive
    ) {
        return repairShopService.list(
                city,
                onlyActive
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SHOP_OWNER', 'ADMIN')"
    )
    public RepairShopResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RepairShopUpdateRequest request
    ) {
        return repairShopService.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('SHOP_OWNER', 'ADMIN')"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        repairShopService.softDelete(id);
    }
}