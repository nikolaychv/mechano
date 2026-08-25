package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.CarBrandService;
import bg.mechano.mechano.web.dto.carbrand.CarBrandCreateRequest;
import bg.mechano.mechano.web.dto.carbrand.CarBrandResponse;
import bg.mechano.mechano.web.dto.carbrand.CarBrandUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/car-brands")
public class CarBrandController {

    private final CarBrandService carBrandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CarBrandResponse create(
            @Valid @RequestBody CarBrandCreateRequest request
    ) {
        return carBrandService.create(request);
    }

    @GetMapping("/{id}")
    public CarBrandResponse getById(@PathVariable Long id) {
        return carBrandService.getById(id);
    }

    @GetMapping
    public List<CarBrandResponse> list() {
        return carBrandService.list();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CarBrandResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CarBrandUpdateRequest request
    ) {
        return carBrandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carBrandService.delete(id);
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public CarBrandResponse restore(@PathVariable Long id) {
        return carBrandService.restore(id);
    }
}