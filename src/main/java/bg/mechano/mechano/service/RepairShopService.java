package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;

import java.util.List;

public interface RepairShopService {

    RepairShopResponse create(RepairShopCreateRequest request);

    RepairShopResponse getById(Long id);

    List<RepairShopResponse> list(
            String city,
            Boolean onlyActive
    );

    List<RepairShopResponse> listCurrentOwnerRepairShops();

    RepairShopResponse update(
            Long id,
            RepairShopUpdateRequest request
    );

    void softDelete(Long id);
}