package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.Booking;
import bg.mechano.mechano.domain.entity.CarBrand;
import bg.mechano.mechano.domain.entity.RepairShop;
import bg.mechano.mechano.domain.entity.RepairShopType;
import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.domain.repository.BookingRepository;
import bg.mechano.mechano.domain.repository.CarBrandRepository;
import bg.mechano.mechano.domain.repository.RepairShopRepository;
import bg.mechano.mechano.domain.repository.RepairShopTypeRepository;
import bg.mechano.mechano.service.BookingService;
import bg.mechano.mechano.service.security.CurrentUserService;
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
    private final RepairShopTypeRepository repairShopTypeRepository;
    private final CarBrandRepository carBrandRepository;
    private final CurrentUserService currentUserService;

    @Override
    public BookingResponse create(BookingCreateRequest request) {
        validateTime(
                request.startTime(),
                request.endTime()
        );

        User client = currentUserService.getCurrentUser();

        RepairShop repairShop = repairShopRepository
                .findById(request.repairShopId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "RepairShop not found: "
                                        + request.repairShopId()
                        )
                );

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

        RepairShopType type = repairShopTypeRepository
                .findById(request.serviceTypeId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "RepairShopType not found: "
                                        + request.serviceTypeId()
                        )
                );

        CarBrand brand = request.carBrandId() != null
                ? carBrandRepository
                .findById(request.carBrandId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "CarBrand not found: "
                                        + request.carBrandId()
                        )
                )
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
                .customerNameAtBooking(
                        trim(request.customerNameAtBooking())
                )
                .customerPhoneAtBooking(
                        trim(request.customerPhoneAtBooking())
                )
                .notesForService(
                        trim(request.notesForService())
                )
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(
                getExisting(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> list(
            Long repairShopId,
            Long clientId,
            BookingStatus status
    ) {
        return bookingRepository
                .findActiveByFilters(
                        repairShopId,
                        clientId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listCurrentUserBookings(
            BookingStatus status
    ) {
        Long currentUserId =
                currentUserService.getCurrentUserId();

        return bookingRepository
                .findActiveByFilters(
                        null,
                        currentUserId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookingResponse updateStatus(
            Long bookingId,
            BookingStatus status
    ) {
        Booking booking = getExisting(bookingId);

        booking.setStatus(status);

        return toResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    public void softDelete(Long bookingId) {
        Booking booking = getExisting(bookingId);

        booking.setDeletedAt(Instant.now());
        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }

    private Booking getExisting(Long id) {
        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking not found: " + id
                        )
                );

        if (booking.getDeletedAt() != null) {
            throw new NotFoundException(
                    "Booking not found: " + id
            );
        }

        return booking;
    }

    private void validateTime(
            Instant start,
            Instant end
    ) {
        if (start == null || end == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start and end time are required"
            );
        }

        if (!start.isBefore(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startTime must be before endTime"
            );
        }

        if (start.isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startTime must be in the future"
            );
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRepairShop().getId(),
                booking.getClient().getId(),
                booking.getRepairShopType().getId(),
                booking.getCarBrand() != null
                        ? booking.getCarBrand().getId()
                        : null,
                booking.getCarModel(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCustomerNameAtBooking(),
                booking.getCustomerPhoneAtBooking(),
                booking.getNotesForService(),
                booking.getCreatedAt()
        );
    }

    private String trim(String value) {
        return value == null
                ? null
                : value.trim();
    }
}