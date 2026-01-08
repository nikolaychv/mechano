package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.*;
import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.domain.repository.*;
import bg.mechano.mechano.web.dto.booking.BookingCreateRequest;
import bg.mechano.mechano.web.dto.booking.BookingResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RepairShopRepository repairShopRepository;
    @Mock private UserRepository userRepository;
    @Mock private RepairShopTypeRepository repairShopTypeRepository;
    @Mock private CarBrandRepository carBrandRepository;

    @InjectMocks
    private BookingServiceImpl service;

    // ---------- create() ----------

    @Test
    void create_shouldCreateBooking_whenValidRequest_andNoOverlap_andNoCarBrand() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L,   // repairShopId
                20L,   // clientId
                30L,   // serviceTypeId
                null,  // carBrandId
                "  Civic  ", // carModel
                start,
                end,
                "  John Doe  ",
                "  +359 888 123 456  ",
                "  Please check brakes  "
        );

        RepairShop shop = RepairShop.builder().id(10L).build();
        User client = User.builder().id(20L).build();
        RepairShopType type = RepairShopType.builder().id(30L).build();

        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(client));
        when(repairShopTypeRepository.findById(30L)).thenReturn(Optional.of(type));

        // capture saved booking
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(999L);
            return b;
        });

        // when
        BookingResponse response = service.create(request);

        // then
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();

        assertEquals(10L, saved.getRepairShop().getId());
        assertEquals(20L, saved.getClient().getId());
        assertEquals(30L, saved.getRepairShopType().getId());
        assertNull(saved.getCarBrand());
        assertEquals("Civic", saved.getCarModel()); // trimmed
        assertEquals(start, saved.getStartTime());
        assertEquals(end, saved.getEndTime());
        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals("John Doe", saved.getCustomerNameAtBooking());
        assertEquals("+359 888 123 456", saved.getCustomerPhoneAtBooking());
        assertEquals("Please check brakes", saved.getNotesForService());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getDeletedAt());

        assertEquals(999L, response.id());
        assertEquals(10L, response.repairShopId());
        assertEquals(20L, response.clientId());
        assertEquals(30L, response.serviceTypeId());
        assertNull(response.carBrandId());
        assertEquals("Civic", response.carModel());
        assertEquals(BookingStatus.PENDING, response.status());
    }

    @Test
    void create_shouldCreateBooking_whenCarBrandProvided() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, 40L,
                "  Model S  ",
                start, end,
                null, null, null
        );

        RepairShop shop = RepairShop.builder().id(10L).build();
        User client = User.builder().id(20L).build();
        RepairShopType type = RepairShopType.builder().id(30L).build();
        CarBrand brand = CarBrand.builder().id(40L).build();

        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(client));
        when(repairShopTypeRepository.findById(30L)).thenReturn(Optional.of(type));
        when(carBrandRepository.findById(40L)).thenReturn(Optional.of(brand));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        // when
        BookingResponse response = service.create(request);

        // then
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();

        assertNotNull(saved.getCarBrand());
        assertEquals(40L, saved.getCarBrand().getId());
        assertEquals("Model S", saved.getCarModel());

        assertEquals(40L, response.carBrandId());
    }

    @Test
    void create_shouldThrowConflict_whenOverlappingBookingExists() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                start, end,
                null, null, null
        );

        RepairShop shop = RepairShop.builder().id(10L).build();
        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(true);

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));

        // then
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Time slot is already booked"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenRepairShopMissing() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                start, end,
                null, null, null
        );

        when(repairShopRepository.findById(10L)).thenReturn(Optional.empty());

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("RepairShop not found: 10"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenClientMissing() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                start, end,
                null, null, null
        );

        RepairShop shop = RepairShop.builder().id(10L).build();

        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("Client not found: 20"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenServiceTypeMissing() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                start, end,
                null, null, null
        );

        RepairShop shop = RepairShop.builder().id(10L).build();
        User client = User.builder().id(20L).build();

        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(client));
        when(repairShopTypeRepository.findById(30L)).thenReturn(Optional.empty());

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("RepairShopType not found: 30"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenCarBrandProvidedButMissing() {
        // given
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(1800);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, 40L,
                "X",
                start, end,
                null, null, null
        );

        RepairShop shop = RepairShop.builder().id(10L).build();
        User client = User.builder().id(20L).build();
        RepairShopType type = RepairShopType.builder().id(30L).build();

        when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(start), eq(end), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(client));
        when(repairShopTypeRepository.findById(30L)).thenReturn(Optional.of(type));
        when(carBrandRepository.findById(40L)).thenReturn(Optional.empty());

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("CarBrand not found: 40"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenStartOrEndNull() {
        // given
        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                null, null,
                null, null, null
        );

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Start and end time are required"));
    }

    @Test
    void create_shouldThrowBadRequest_whenStartNotBeforeEnd() {
        // given
        Instant t = Instant.now().plusSeconds(3600);
        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                t, t,
                null, null, null
        );

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("startTime must be before endTime"));
    }

    @Test
    void create_shouldThrowBadRequest_whenStartInPast() {
        // given
        Instant start = Instant.now().minusSeconds(60);
        Instant end = Instant.now().plusSeconds(60);

        var request = new BookingCreateRequest(
                10L, 20L, 30L, null,
                "X",
                start, end,
                null, null, null
        );

        // when
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("startTime must be in the future"));
    }

    // ---------- getById() ----------

    @Test
    void getById_shouldReturnBooking_whenExistsAndNotDeleted() {
        // given
        Booking b = Booking.builder()
                .id(7L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .carBrand(null)
                .carModel("X")
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .status(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(b));

        // when
        BookingResponse res = service.getById(7L);

        // then
        assertEquals(7L, res.id());
        assertEquals(10L, res.repairShopId());
    }

    @Test
    void getById_shouldThrowNotFound_whenSoftDeleted() {
        // given
        Booking b = Booking.builder()
                .id(7L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .status(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(b));

        // when + then
        assertThrows(NotFoundException.class, () -> service.getById(7L));
    }

    // ---------- list() ----------

    @Test
    void list_shouldFilterByDeletedAndOptionalParams() {
        // given
        Booking notDeleted1 = Booking.builder()
                .id(1L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .status(BookingStatus.PENDING)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        Booking notDeleted2 = Booking.builder()
                .id(2L)
                .repairShop(RepairShop.builder().id(11L).build())
                .client(User.builder().id(21L).build())
                .repairShopType(RepairShopType.builder().id(31L).build())
                .status(BookingStatus.CONFIRMED)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        Booking deleted = Booking.builder()
                .id(3L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .status(BookingStatus.PENDING)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(bookingRepository.findAll()).thenReturn(List.of(notDeleted1, notDeleted2, deleted));

        // when: filter by repairShopId=10, clientId=20, status=PENDING
        List<BookingResponse> res = service.list(10L, 20L, BookingStatus.PENDING);

        // then
        assertEquals(1, res.size());
        assertEquals(1L, res.get(0).id());
    }

    // ---------- updateStatus() ----------

    @Test
    void updateStatus_shouldUpdateAndSave() {
        // given
        Booking b = Booking.builder()
                .id(5L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .status(BookingStatus.PENDING)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        BookingResponse res = service.updateStatus(5L, BookingStatus.CONFIRMED);

        // then
        assertEquals(BookingStatus.CONFIRMED, res.status());
        verify(bookingRepository).save(b);
    }

    // ---------- softDelete() ----------

    @Test
    void softDelete_shouldSetDeletedAtAndCancelAndSave() {
        // given
        Booking b = Booking.builder()
                .id(6L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .status(BookingStatus.PENDING)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(bookingRepository.findById(6L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        service.softDelete(6L);

        // then
        assertNotNull(b.getDeletedAt());
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        verify(bookingRepository).save(b);
    }

    @Test
    void softDelete_shouldThrowNotFound_whenAlreadyDeleted() {
        // given
        Booking b = Booking.builder()
                .id(6L)
                .repairShop(RepairShop.builder().id(10L).build())
                .client(User.builder().id(20L).build())
                .repairShopType(RepairShopType.builder().id(30L).build())
                .status(BookingStatus.PENDING)
                .startTime(Instant.now().plusSeconds(1000))
                .endTime(Instant.now().plusSeconds(2000))
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(bookingRepository.findById(6L)).thenReturn(Optional.of(b));

        // when + then
        assertThrows(NotFoundException.class, () -> service.softDelete(6L));
        verify(bookingRepository, never()).save(any());
    }
}

