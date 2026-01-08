package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.CarBrand;
import bg.mechano.mechano.domain.repository.CarBrandRepository;
import bg.mechano.mechano.web.dto.carbrand.CarBrandCreateRequest;
import bg.mechano.mechano.web.dto.carbrand.CarBrandResponse;
import bg.mechano.mechano.web.dto.carbrand.CarBrandUpdateRequest;
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
class CarBrandServiceImplTest {

    @Mock
    private CarBrandRepository carBrandRepository;

    @InjectMocks
    private CarBrandServiceImpl service;

    // ---------- create() ----------

    @Test
    void create_shouldCreateCarBrand_whenValidRequest() {
        // given
        CarBrandCreateRequest request = new CarBrandCreateRequest("  Toyota  ");

        when(carBrandRepository.findByName("Toyota")).thenReturn(Optional.empty());
        when(carBrandRepository.save(any(CarBrand.class))).thenAnswer(inv -> {
            CarBrand b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        // when
        CarBrandResponse response = service.create(request);

        // then
        ArgumentCaptor<CarBrand> captor = ArgumentCaptor.forClass(CarBrand.class);
        verify(carBrandRepository).save(captor.capture());

        CarBrand saved = captor.getValue();
        assertEquals("Toyota", saved.getName());

        assertEquals(1L, response.id());
        assertEquals("Toyota", response.name());
    }

    @Test
    void create_shouldThrowBadRequest_whenRequestIsNull() {
        // when
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(null));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Request body is required"));
        verify(carBrandRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenNameIsNullOrBlank() {
        // null name
        ResponseStatusException ex1 = assertThrows(
                ResponseStatusException.class,
                () -> service.create(new CarBrandCreateRequest(null))
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        // blank name
        ResponseStatusException ex2 = assertThrows(
                ResponseStatusException.class,
                () -> service.create(new CarBrandCreateRequest("   "))
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());

        verify(carBrandRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenCarBrandAlreadyExists() {
        // given
        CarBrandCreateRequest request = new CarBrandCreateRequest("BMW");

        when(carBrandRepository.findByName("BMW"))
                .thenReturn(Optional.of(CarBrand.builder().id(99L).name("BMW").build()));

        // when
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(request));

        // then
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Car brand already exists"));
        verify(carBrandRepository, never()).save(any());
    }

    // ---------- getById() ----------

    @Test
    void getById_shouldReturnCarBrand_whenExists() {
        // given
        CarBrand brand = CarBrand.builder()
                .id(5L)
                .name("Audi")
                .build();

        when(carBrandRepository.findById(5L)).thenReturn(Optional.of(brand));

        // when
        CarBrandResponse response = service.getById(5L);

        // then
        assertEquals(5L, response.id());
        assertEquals("Audi", response.name());
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        // given
        when(carBrandRepository.findById(10L)).thenReturn(Optional.empty());

        // when + then
        NotFoundException ex =
                assertThrows(NotFoundException.class, () -> service.getById(10L));

        assertTrue(ex.getMessage().contains("CarBrand not found: 10"));
    }

    // ---------- list() ----------

    @Test
    void list_shouldReturnAllCarBrands() {
        // given
        when(carBrandRepository.findAll()).thenReturn(List.of(
                CarBrand.builder().id(1L).name("BMW").build(),
                CarBrand.builder().id(2L).name("Mercedes").build()
        ));

        // when
        List<CarBrandResponse> result = service.list();

        // then
        assertEquals(2, result.size());
        assertEquals("BMW", result.get(0).name());
        assertEquals("Mercedes", result.get(1).name());
    }

    // ---------- update() ----------

    @Test
    void update_shouldUpdateName_whenValidAndNoConflict() {
        // given
        CarBrand existing = CarBrand.builder()
                .id(3L)
                .name("Volvo")
                .build();

        when(carBrandRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(carBrandRepository.findByName("Volvo Cars")).thenReturn(Optional.empty());
        when(carBrandRepository.save(any(CarBrand.class))).thenAnswer(inv -> inv.getArgument(0));

        CarBrandUpdateRequest request = new CarBrandUpdateRequest("  Volvo Cars  ");

        // when
        CarBrandResponse response = service.update(3L, request);

        // then
        assertEquals("Volvo Cars", response.name());
        verify(carBrandRepository).save(existing);
    }

    @Test
    void update_shouldThrowConflict_whenNameExistsForAnotherId() {
        // given
        CarBrand existing = CarBrand.builder()
                .id(3L)
                .name("Volvo")
                .build();

        CarBrand other = CarBrand.builder()
                .id(4L)
                .name("Volvo Cars")
                .build();

        when(carBrandRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(carBrandRepository.findByName("Volvo Cars")).thenReturn(Optional.of(other));

        CarBrandUpdateRequest request = new CarBrandUpdateRequest("Volvo Cars");

        // when
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.update(3L, request));

        // then
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Car brand already exists"));
        verify(carBrandRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameNameForSameId() {
        // given
        CarBrand existing = CarBrand.builder()
                .id(3L)
                .name("Volvo")
                .build();

        when(carBrandRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(carBrandRepository.findByName("Volvo")).thenReturn(Optional.of(existing));
        when(carBrandRepository.save(any(CarBrand.class))).thenAnswer(inv -> inv.getArgument(0));

        CarBrandUpdateRequest request = new CarBrandUpdateRequest(" Volvo ");

        // when
        CarBrandResponse response = service.update(3L, request);

        // then
        assertEquals("Volvo", response.name());
        verify(carBrandRepository).save(existing);
    }

    @Test
    void update_shouldThrowNotFound_whenBrandMissing() {
        // given
        when(carBrandRepository.findById(8L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class,
                () -> service.update(8L, new CarBrandUpdateRequest("X")));
    }

    // ---------- delete() ----------

    @Test
    void delete_shouldDeleteBrand_whenExists() {
        // given
        CarBrand brand = CarBrand.builder()
                .id(9L)
                .name("Skoda")
                .build();

        when(carBrandRepository.findById(9L)).thenReturn(Optional.of(brand));

        // when
        service.delete(9L);

        // then
        verify(carBrandRepository).delete(brand);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        // given
        when(carBrandRepository.findById(10L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class, () -> service.delete(10L));
        verify(carBrandRepository, never()).delete(any());
    }
}

