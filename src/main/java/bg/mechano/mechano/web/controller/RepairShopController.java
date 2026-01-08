package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.RepairShopService;
import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repair-shops")
public class RepairShopController {

    private final RepairShopService repairShopService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepairShopResponse create(@Valid @RequestBody RepairShopCreateRequest request) {
        return repairShopService.create(request);
    }

    @GetMapping("/{id}")
    public RepairShopResponse getById(@PathVariable Long id) {
        return repairShopService.getById(id);
    }

    @GetMapping
    public List<RepairShopResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean onlyActive
    ) {
        return repairShopService.list(city, onlyActive);
    }

    @PutMapping("/{id}")
    public RepairShopResponse update(
            @PathVariable Long id,
            @RequestBody RepairShopUpdateRequest request
    ) {
        return repairShopService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repairShopService.softDelete(id);
    }
}