package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShopType;
import bg.mechano.mechano.domain.repository.RepairShopTypeRepository;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeCreateRequest;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeResponse;
import bg.mechano.mechano.web.dto.repairshoptype.RepairShopTypeUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairShopTypeServiceImplTest {

    @Mock
    private RepairShopTypeRepository repairShopTypeRepository;

    @InjectMocks
    private RepairShopTypeServiceImpl service;

    @Test
    void create_shouldCreateRepairShopType_whenValidRequest() {
        RepairShopTypeCreateRequest request =
                new RepairShopTypeCreateRequest("  Engine repair  ", "  All engine services  ");

        when(repairShopTypeRepository.findByName("Engine repair")).thenReturn(Optional.empty());
        when(repairShopTypeRepository.save(any())).thenAnswer(inv -> {
            RepairShopType t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        RepairShopTypeResponse response = service.create(request);

        ArgumentCaptor<RepairShopType> captor = ArgumentCaptor.forClass(RepairShopType.class);
        verify(repairShopTypeRepository).save(captor.capture());

        RepairShopType saved = captor.getValue();
        assertEquals("Engine repair", saved.getName());
        assertEquals("All engine services", saved.getDescription());

        assertEquals(1L, response.id());
        assertEquals("Engine repair", response.name());
        assertEquals("All engine services", response.description());
    }

    @Test
    void create_shouldSetDescriptionNull_whenBlank() {
        RepairShopTypeCreateRequest request =
                new RepairShopTypeCreateRequest("  Diagnostics  ", "   ");

        when(repairShopTypeRepository.findByName("Diagnostics")).thenReturn(Optional.empty());
        when(repairShopTypeRepository.save(any())).thenAnswer(inv -> {
            RepairShopType t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        RepairShopTypeResponse response = service.create(request);

        assertNull(response.description());
    }

    @Test
    void create_shouldThrowBadRequest_whenNameNull() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> service.create(new RepairShopTypeCreateRequest(null, "x")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void create_shouldThrowBadRequest_whenNameBlank() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> service.create(new RepairShopTypeCreateRequest("   ", "x")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void create_shouldThrowConflict_whenNameExists() {
        when(repairShopTypeRepository.findByName("Oil change"))
                .thenReturn(Optional.of(RepairShopType.builder().id(5L).name("Oil change").build()));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> service.create(new RepairShopTypeCreateRequest("Oil change", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getById_shouldReturnType_whenExists() {
        RepairShopType type = RepairShopType.builder()
                .id(3L)
                .name("Diagnostics")
                .description("Desc")
                .build();

        when(repairShopTypeRepository.findById(3L)).thenReturn(Optional.of(type));

        RepairShopTypeResponse response = service.getById(3L);

        assertEquals(3L, response.id());
        assertEquals("Diagnostics", response.name());
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(repairShopTypeRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(4L));
    }

    @Test
    void list_shouldReturnAllTypes() {
        when(repairShopTypeRepository.findAll()).thenReturn(List.of(
                RepairShopType.builder().id(1L).name("A").build(),
                RepairShopType.builder().id(2L).name("B").build()
        ));

        List<RepairShopTypeResponse> result = service.list();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).name());
        assertEquals("B", result.get(1).name());
    }

    @Test
    void update_shouldUpdateType_whenValidAndNoConflict() {
        RepairShopType existing = RepairShopType.builder()
                .id(10L)
                .name("Old")
                .description("Old desc")
                .build();

        when(repairShopTypeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(repairShopTypeRepository.findByName("New"))
                .thenReturn(Optional.empty());
        when(repairShopTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RepairShopTypeUpdateRequest request =
                new RepairShopTypeUpdateRequest("  New  ", "  New desc  ");

        RepairShopTypeResponse response = service.update(10L, request);

        assertEquals("New", response.name());
        assertEquals("New desc", response.description());
    }

    @Test
    void update_shouldThrowConflict_whenNameExistsForAnotherId() {
        RepairShopType existing = RepairShopType.builder()
                .id(10L)
                .name("Old")
                .build();

        RepairShopType other = RepairShopType.builder()
                .id(11L)
                .name("New")
                .build();

        when(repairShopTypeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(repairShopTypeRepository.findByName("New"))
                .thenReturn(Optional.of(other));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> service.update(10L, new RepairShopTypeUpdateRequest("New", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void update_shouldThrowNotFound_whenMissing() {
        when(repairShopTypeRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(20L, new RepairShopTypeUpdateRequest("X", null)));
    }

    @Test
    void delete_shouldDeleteType_whenExists() {
        RepairShopType type = RepairShopType.builder()
                .id(30L)
                .name("To delete")
                .build();

        when(repairShopTypeRepository.findById(30L)).thenReturn(Optional.of(type));

        service.delete(30L);

        verify(repairShopTypeRepository).delete(type);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(repairShopTypeRepository.findById(31L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(31L));
        verify(repairShopTypeRepository, never()).delete(any());
    }
}
