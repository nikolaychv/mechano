package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.RepairShopTypeService;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeCreateRequest;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeResponse;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repair-shop-types")
public class RepairShopTypeController {

    private final RepairShopTypeService repairShopTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepairShopTypeResponse create(@Valid @RequestBody RepairShopTypeCreateRequest request) {
        return repairShopTypeService.create(request);
    }

    @GetMapping("/{id}")
    public RepairShopTypeResponse getById(@PathVariable Long id) {
        return repairShopTypeService.getById(id);
    }

    @GetMapping
    public List<RepairShopTypeResponse> list() {
        return repairShopTypeService.list();
    }

    @PutMapping("/{id}")
    public RepairShopTypeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RepairShopTypeUpdateRequest request
    ) {
        return repairShopTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repairShopTypeService.delete(id);
    }
}