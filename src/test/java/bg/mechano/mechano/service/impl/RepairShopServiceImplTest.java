package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.web.dto.repairshop.RepairShopCreateRequest;
import bg.mechano.mechano.web.dto.repairshop.RepairShopResponse;
import bg.mechano.mechano.web.dto.repairshop.RepairShopUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairShopServiceImplTest {

    @Mock
    private RepairShopRepository repairShopRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RepairShopServiceImpl service;

    @Test
    void create_shouldCreateRepairShop_whenOwnerExists_andTrimFields() {
        RepairShopCreateRequest request = new RepairShopCreateRequest(
                10L,
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
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));

        ArgumentCaptor<RepairShop> captor = ArgumentCaptor.forClass(RepairShop.class);
        when(repairShopRepository.save(any())).thenAnswer(inv -> {
            RepairShop s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        RepairShopResponse response = service.create(request);

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
        assertTrue(response.isActive());
    }

    @Test
    void create_shouldThrowNotFound_whenOwnerMissing() {
        RepairShopCreateRequest request = new RepairShopCreateRequest(
                10L, "X", "Sofia", null, null, null, null, null, null, null
        );

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request));
        verify(repairShopRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnRepairShop_whenExistsAndNotDeleted() {
        RepairShop shop = RepairShop.builder()
                .id(1L)
                .owner(User.builder().id(10L).build())
                .name("Shop")
                .city("Sofia")
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(repairShopRepository.findById(1L)).thenReturn(Optional.of(shop));

        RepairShopResponse response = service.getById(1L);

        assertEquals(1L, response.id());
        assertEquals(10L, response.ownerId());
        assertEquals("Shop", response.name());
    }

    @Test
    void getById_shouldThrowNotFound_whenDeleted() {
        RepairShop shop = RepairShop.builder()
                .id(1L)
                .owner(User.builder().id(10L).build())
                .name("Shop")
                .city("Sofia")
                .isActive(false)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(repairShopRepository.findById(1L)).thenReturn(Optional.of(shop));

        assertThrows(NotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void list_shouldUseFindByIsActiveTrueAndDeletedAtIsNull_whenOnlyActiveTrue() {
        when(repairShopRepository.findByIsActiveTrueAndDeletedAtIsNull()).thenReturn(List.of(
                RepairShop.builder()
                        .id(1L)
                        .owner(User.builder().id(10L).build())
                        .name("A")
                        .city("Sofia")
                        .isActive(true)
                        .createdAt(Instant.now())
                        .deletedAt(null)
                        .build()
        ));

        List<RepairShopResponse> result = service.list(null, true);

        verify(repairShopRepository).findByIsActiveTrueAndDeletedAtIsNull();
        verify(repairShopRepository, never()).findByCityIgnoreCaseAndDeletedAtIsNull(any());
        verify(repairShopRepository, never()).findAll();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void list_shouldPreferOnlyActive_overCity() {
        when(repairShopRepository.findByIsActiveTrueAndDeletedAtIsNull()).thenReturn(List.of());

        service.list("Sofia", true);

        verify(repairShopRepository).findByIsActiveTrueAndDeletedAtIsNull();
        verify(repairShopRepository, never()).findByCityIgnoreCaseAndDeletedAtIsNull(any());
    }

    @Test
    void list_shouldUseFindByCityIgnoreCaseAndDeletedAtIsNull_whenCityProvided() {
        when(repairShopRepository.findByCityIgnoreCaseAndDeletedAtIsNull("Sofia")).thenReturn(List.of(
                RepairShop.builder()
                        .id(1L)
                        .owner(User.builder().id(10L).build())
                        .name("A")
                        .city("Sofia")
                        .isActive(true)
                        .createdAt(Instant.now())
                        .deletedAt(null)
                        .build()
        ));

        List<RepairShopResponse> result = service.list("Sofia", false);

        verify(repairShopRepository).findByCityIgnoreCaseAndDeletedAtIsNull("Sofia");
        verify(repairShopRepository, never()).findByIsActiveTrueAndDeletedAtIsNull();
        verify(repairShopRepository, never()).findAll();

        assertEquals(1, result.size());
        assertEquals("Sofia", result.get(0).city());
    }

    @Test
    void list_shouldUseFindAllAndFilterDeleted_whenNoFilters() {
        RepairShop notDeleted = RepairShop.builder()
                .id(1L)
                .owner(User.builder().id(10L).build())
                .name("A")
                .city("Sofia")
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        RepairShop deleted = RepairShop.builder()
                .id(2L)
                .owner(User.builder().id(11L).build())
                .name("B")
                .city("Plovdiv")
                .isActive(false)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(repairShopRepository.findAll()).thenReturn(List.of(notDeleted, deleted));

        List<RepairShopResponse> result = service.list(null, null);

        verify(repairShopRepository).findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields_andSave() {
        RepairShop shop = RepairShop.builder()
                .id(5L)
                .owner(User.builder().id(10L).build())
                .name("Old")
                .city("Sofia")
                .address("Addr")
                .phone("Phone")
                .email("Email")
                .website("Web")
                .description("Desc")
                .priceRangeMin(BigDecimal.valueOf(10))
                .priceRangeMax(BigDecimal.valueOf(100))
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(repairShopRepository.findById(5L)).thenReturn(Optional.of(shop));
        when(repairShopRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RepairShopUpdateRequest request = new RepairShopUpdateRequest(
                "  New Name  ",
                null,
                "  New Address  ",
                null,
                "  new@mail.com  ",
                null,
                null,
                BigDecimal.valueOf(20),
                null,
                false
        );

        RepairShopResponse response = service.update(5L, request);

        assertEquals("New Name", response.name());
        assertEquals("Sofia", response.city());
        assertEquals("New Address", response.address());
        assertEquals("new@mail.com", response.email());
        assertEquals(BigDecimal.valueOf(20), response.priceRangeMin());
        assertEquals(BigDecimal.valueOf(100), response.priceRangeMax());
        assertFalse(response.isActive());

        verify(repairShopRepository).save(shop);
    }

    @Test
    void update_shouldThrowNotFound_whenShopDeleted() {
        RepairShop shop = RepairShop.builder()
                .id(5L)
                .owner(User.builder().id(10L).build())
                .name("Old")
                .city("Sofia")
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(repairShopRepository.findById(5L)).thenReturn(Optional.of(shop));

        assertThrows(NotFoundException.class,
                () -> service.update(5L,
                        new RepairShopUpdateRequest(null, null, null, null, null, null, null, null, null, null)));

        verify(repairShopRepository, never()).save(any());
    }

    @Test
    void softDelete_shouldSetDeletedAtAndDeactivate_andSave() {
        RepairShop shop = RepairShop.builder()
                .id(7L)
                .owner(User.builder().id(10L).build())
                .name("Shop")
                .city("Sofia")
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(repairShopRepository.findById(7L)).thenReturn(Optional.of(shop));
        when(repairShopRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(7L);

        assertNotNull(shop.getDeletedAt());
        assertFalse(shop.isActive());
        verify(repairShopRepository).save(shop);
    }

    @Test
    void softDelete_shouldThrowNotFound_whenShopAlreadyDeleted() {
        RepairShop shop = RepairShop.builder()
                .id(7L)
                .owner(User.builder().id(10L).build())
                .name("Shop")
                .city("Sofia")
                .isActive(false)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(repairShopRepository.findById(7L)).thenReturn(Optional.of(shop));

        assertThrows(NotFoundException.class, () -> service.softDelete(7L));
        verify(repairShopRepository, never()).save(any());
    }
}
