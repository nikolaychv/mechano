package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.*;
import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.domain.repository.*;
import bg.mechano.mechano.service.BookingService;
import bg.mechano.mechano.web.dto.booking.BookingCreateRequest;
import bg.mechano.mechano.web.dto.booking.BookingResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RepairShopRepository repairShopRepository;
    private final UserRepository userRepository;
    private final RepairShopTypeRepository repairShopTypeRepository;
    private final CarBrandRepository carBrandRepository;

    @Override
    public BookingResponse create(BookingCreateRequest request) {
        validateTime(request.startTime(), request.endTime());

        RepairShop repairShop = repairShopRepository.findById(request.repairShopId())
                .orElseThrow(() -> new NotFoundException("RepairShop not found: " + request.repairShopId()));

        if (bookingRepository.existsOverlappingBooking(
                repairShop.getId(),
                request.startTime(),
                request.endTime(),
                BookingStatus.CANCELLED
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Time slot is already booked"
            );
        }

        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found: " + request.clientId()));

        RepairShopType type = repairShopTypeRepository.findById(request.serviceTypeId())
                .orElseThrow(() -> new NotFoundException("RepairShopType not found: " + request.serviceTypeId()));

        CarBrand brand = request.carBrandId() != null
                ? carBrandRepository.findById(request.carBrandId())
                .orElseThrow(() -> new NotFoundException("CarBrand not found: " + request.carBrandId()))
                : null;

        Booking booking = Booking.builder()
                .repairShop(repairShop)
                .client(client)
                .repairShopType(type)
                .carBrand(brand)
                .carModel(trim(request.carModel()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(BookingStatus.PENDING)
                .customerNameAtBooking(trim(request.customerNameAtBooking()))
                .customerPhoneAtBooking(trim(request.customerPhoneAtBooking()))
                .notesForService(trim(request.notesForService()))
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> list(Long repairShopId, Long clientId, BookingStatus status) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getDeletedAt() == null)
                .filter(b -> repairShopId == null || b.getRepairShop().getId().equals(repairShopId))
                .filter(b -> clientId == null || b.getClient().getId().equals(clientId))
                .filter(b -> status == null || b.getStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookingResponse updateStatus(Long bookingId, BookingStatus status) {
        Booking booking = getExisting(bookingId);
        booking.setStatus(status);
        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public void softDelete(Long bookingId) {
        Booking booking = getExisting(bookingId);
        booking.setDeletedAt(Instant.now());
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private Booking getExisting(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + id));

        if (booking.getDeletedAt() != null) {
            throw new NotFoundException("Booking not found: " + id);
        }
        return booking;
    }

    private void validateTime(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end time are required");
        }
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
        if (start.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be in the future");
        }
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getRepairShop().getId(),
                b.getClient().getId(),
                b.getRepairShopType().getId(),
                b.getCarBrand() != null ? b.getCarBrand().getId() : null,
                b.getCarModel(),
                b.getStartTime(),
                b.getEndTime(),
                b.getStatus(),
                b.getCustomerNameAtBooking(),
                b.getCustomerPhoneAtBooking(),
                b.getNotesForService(),
                b.getCreatedAt()
        );
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}