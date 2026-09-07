package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairShopServiceImplTest {

    @Mock
    private RepairShopRepository repairShopRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RepairShopServiceImpl service;

    @Test
    void create_shouldCreateRepairShopForCurrentUserAndTrimFields() {
        RepairShopCreateRequest request = new RepairShopCreateRequest(
                "  My Garage  ",
                "  Sofia  ",
                "  Bul. Vitosha 1  ",
                "  +359 888 111 222  ",
                "  mail@test.com  ",
                "  https://site.bg  ",
                "  Great service  ",
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(200)
        );

        User owner = User.builder().id(10L).build();

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(repairShopRepository.save(any(RepairShop.class))).thenAnswer(inv -> {
            RepairShop shop = inv.getArgument(0);
            shop.setId(99L);
            return shop;
        });

        RepairShopResponse response = service.create(request);

        ArgumentCaptor<RepairShop> captor = ArgumentCaptor.forClass(RepairShop.class);
        verify(repairShopRepository).save(captor.capture());

        RepairShop saved = captor.getValue();

        assertEquals(10L, saved.getOwner().getId());
        assertEquals("My Garage", saved.getName());
        assertEquals("Sofia", saved.getCity());
        assertEquals("Bul. Vitosha 1", saved.getAddress());
        assertEquals("+359 888 111 222", saved.getPhone());
        assertEquals("mail@test.com", saved.getEmail());
        assertEquals("https://site.bg", saved.getWebsite());
        assertEquals("Great service", saved.getDescription());
        assertEquals(BigDecimal.valueOf(50), saved.getPriceRangeMin());
        assertEquals(BigDecimal.valueOf(200), saved.getPriceRangeMax());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getDeletedAt());

        assertEquals(99L, response.id());
        assertEquals(10L, response.ownerId());
        assertEquals("My Garage", response.name());
    }

    @Test
    void getById_shouldReturnRepairShopWhenItExists() {
        RepairShop shop = createShop(1L, 10L, "Shop", "Sofia", true, null);

        when(repairShopRepository.findById(1L)).thenReturn(Optional.of(shop));

        RepairShopResponse response = service.getById(1L);

        assertEquals(1L, response.id());
        assertEquals(10L, response.ownerId());
        assertEquals("Shop", response.name());
    }

    @Test
    void getById_shouldThrowNotFoundWhenRepairShopDoesNotExist() {
        when(repairShopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getById_shouldThrowNotFoundWhenRepairShopIsDeleted() {
        RepairShop shop = createShop(1L, 10L, "Shop", "Sofia", false, Instant.now());

        when(repairShopRepository.findById(1L)).thenReturn(Optional.of(shop));

        assertThrows(NotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void list_shouldUseOnlyActiveFilter() {
        RepairShop shop = createShop(1L, 10L, "Shop", "Sofia", true, null);

        when(repairShopRepository.findByIsActiveTrueAndDeletedAtIsNull())
                .thenReturn(List.of(shop));

        List<RepairShopResponse> result = service.list(null, true);

        verify(repairShopRepository).findByIsActiveTrueAndDeletedAtIsNull();
        verify(repairShopRepository, never())
                .findByCityIgnoreCaseAndDeletedAtIsNull(anyString());

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void list_shouldApplyCityAndOnlyActiveTogether() {
        RepairShop shop = createShop(1L, 10L, "Sofia Garage", "Sofia", true, null);

        when(repairShopRepository
                .findByCityIgnoreCaseAndIsActiveTrueAndDeletedAtIsNull("Sofia"))
                .thenReturn(List.of(shop));

        List<RepairShopResponse> result = service.list("Sofia", true);

        verify(repairShopRepository)
                .findByCityIgnoreCaseAndIsActiveTrueAndDeletedAtIsNull("Sofia");

        verify(repairShopRepository, never())
                .findByIsActiveTrueAndDeletedAtIsNull();

        verify(repairShopRepository, never())
                .findByCityIgnoreCaseAndDeletedAtIsNull(anyString());

        assertEquals(1, result.size());
        assertEquals("Sofia", result.get(0).city());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void list_shouldUseCityFilter() {
        RepairShop shop = createShop(1L, 10L, "Shop", "Sofia", true, null);

        when(repairShopRepository.findByCityIgnoreCaseAndDeletedAtIsNull("Sofia"))
                .thenReturn(List.of(shop));

        List<RepairShopResponse> result = service.list("Sofia", false);

        verify(repairShopRepository)
                .findByCityIgnoreCaseAndDeletedAtIsNull("Sofia");

        verify(repairShopRepository, never())
                .findByIsActiveTrueAndDeletedAtIsNull();

        assertEquals(1, result.size());
        assertEquals("Sofia", result.get(0).city());
    }

    @Test
    void list_shouldReturnNonDeletedRepairShopsWhenThereAreNoFilters() {
        RepairShop shop = createShop(1L, 10L, "Shop", "Sofia", true, null);

        when(repairShopRepository.findByDeletedAtIsNull())
                .thenReturn(List.of(shop));

        List<RepairShopResponse> result = service.list(null, null);

        verify(repairShopRepository).findByDeletedAtIsNull();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void listCurrentOwnerRepairShops_shouldUseCurrentUserId() {
        RepairShop shop = createShop(2L, 10L, "Owner Shop", "Sofia", true, null);

        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(repairShopRepository.findByOwnerIdAndDeletedAtIsNull(10L))
                .thenReturn(List.of(shop));

        List<RepairShopResponse> result = service.listCurrentOwnerRepairShops();

        verify(repairShopRepository).findByOwnerIdAndDeletedAtIsNull(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).ownerId());
    }

    @Test
    void update_shouldAllowAdmin() {
        RepairShop shop = createShop(5L, 10L, "Old", "Sofia", true, null);

        when(repairShopRepository.findById(5L)).thenReturn(Optional.of(shop));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(repairShopRepository.save(any(RepairShop.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RepairShopUpdateRequest request = new RepairShopUpdateRequest(
                "New Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        RepairShopResponse response = service.update(5L, request);

        assertEquals("New Name", response.name());
        verify(repairShopRepository).save(shop);
    }

    @Test
    void update_shouldAllowShopOwnerForOwnShop() {
        RepairShop shop = createShop(5L, 10L, "Old", "Sofia", true, null);
        User owner = User.builder().id(10L).build();

        when(repairShopRepository.findById(5L)).thenReturn(Optional.of(shop));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(currentUserService.isShopOwner()).thenReturn(true);
        when(repairShopRepository.save(any(RepairShop.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RepairShopUpdateRequest request = new RepairShopUpdateRequest(
                "New Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        RepairShopResponse response = service.update(5L, request);

        assertEquals("New Name", response.name());
        verify(repairShopRepository).save(shop);
    }

    @Test
    void update_shouldDenyDifferentShopOwner() {
        RepairShop shop = createShop(5L, 10L, "Old", "Sofia", true, null);
        User differentOwner = User.builder().id(20L).build();

        when(repairShopRepository.findById(5L)).thenReturn(Optional.of(shop));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(differentOwner);
        when(currentUserService.isShopOwner()).thenReturn(true);

        RepairShopUpdateRequest request = new RepairShopUpdateRequest(
                "New Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(AccessDeniedException.class, () -> service.update(5L, request));
        verify(repairShopRepository, never()).save(any());
    }

    @Test
    void softDelete_shouldDeactivateAndSetDeletedAtForAdmin() {
        RepairShop shop = createShop(7L, 10L, "Shop", "Sofia", true, null);

        when(repairShopRepository.findById(7L)).thenReturn(Optional.of(shop));
        when(currentUserService.isAdmin()).thenReturn(true);

        service.softDelete(7L);

        assertFalse(shop.isActive());
        assertNotNull(shop.getDeletedAt());

        verify(repairShopRepository).save(shop);
    }

    @Test
    void softDelete_shouldThrowNotFoundWhenAlreadyDeleted() {
        RepairShop shop = createShop(7L, 10L, "Shop", "Sofia", false, Instant.now());

        when(repairShopRepository.findById(7L)).thenReturn(Optional.of(shop));

        assertThrows(NotFoundException.class, () -> service.softDelete(7L));
        verify(repairShopRepository, never()).save(any());
    }

    private RepairShop createShop(Long id, Long ownerId, String name, String city,
                                  boolean active, Instant deletedAt) {
        return RepairShop.builder()
                .id(id)
                .owner(User.builder().id(ownerId).build())
                .name(name)
                .city(city)
                .isActive(active)
                .createdAt(Instant.now())
                .deletedAt(deletedAt)
                .build();
    }
}