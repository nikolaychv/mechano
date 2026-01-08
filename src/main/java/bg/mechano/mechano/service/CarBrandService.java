package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.carbrand.CarBrandCreateRequest;
import bg.mechano.mechano.web.dto.carbrand.CarBrandResponse;
import bg.mechano.mechano.web.dto.carbrand.CarBrandUpdateRequest;

import java.util.List;

public interface CarBrandService {

    CarBrandResponse create(CarBrandCreateRequest request);

    CarBrandResponse getById(Long id);

    List<CarBrandResponse> list();

    CarBrandResponse update(Long id, CarBrandUpdateRequest request);

    void delete(Long id);
}