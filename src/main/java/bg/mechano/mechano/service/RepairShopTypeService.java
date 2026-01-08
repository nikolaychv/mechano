package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeCreateRequest;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeResponse;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeUpdateRequest;

import java.util.List;

public interface RepairShopTypeService {

    RepairShopTypeResponse create(RepairShopTypeCreateRequest request);

    RepairShopTypeResponse getById(Long id);

    List<RepairShopTypeResponse> list();

    RepairShopTypeResponse update(Long id, RepairShopTypeUpdateRequest request);

    void delete(Long id);
}