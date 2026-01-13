package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.RepairShopTypeService;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeCreateRequest;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeResponse;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repair-shop-types")
public class RepairShopTypeController {

    private final RepairShopTypeService repairShopTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<RepairShopTypeResponse> create(@Valid @RequestBody RepairShopTypeCreateRequest request) {
        RepairShopTypeResponse created = repairShopTypeService.create(request);
        return toModel(created);
    }

    @GetMapping("/{id}")
    public EntityModel<RepairShopTypeResponse> getById(@PathVariable Long id) {
        RepairShopTypeResponse dto = repairShopTypeService.getById(id);
        return toModel(dto);
    }

    @GetMapping
    public CollectionModel<EntityModel<RepairShopTypeResponse>> list() {
        List<EntityModel<RepairShopTypeResponse>> items = repairShopTypeService.list()
                .stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                items,
                linkTo(methodOn(RepairShopTypeController.class).list()).withSelfRel()
        );
    }

    @PutMapping("/{id}")
    public EntityModel<RepairShopTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RepairShopTypeUpdateRequest request
    ) {
        RepairShopTypeResponse updated = repairShopTypeService.update(id, request);
        return toModel(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repairShopTypeService.delete(id);
    }

    private EntityModel<RepairShopTypeResponse> toModel(RepairShopTypeResponse dto) {
        Long id = dto.id();

        return EntityModel.of(
                dto,
                linkTo(methodOn(RepairShopTypeController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(RepairShopTypeController.class).list()).withRel("all"),
                linkTo(methodOn(RepairShopTypeController.class).update(id, null)).withRel("update")
        );
    }
}