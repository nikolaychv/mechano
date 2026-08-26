package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.service.BookingService;
import bg.mechano.mechano.web.dto.booking.BookingCreateRequest;
import bg.mechano.mechano.web.dto.booking.BookingResponse;
import bg.mechano.mechano.web.dto.booking.BookingUpdateStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @Valid @RequestBody BookingCreateRequest request
    ) {
        return bookingService.create(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<BookingResponse> getCurrentUserBookings(
            @RequestParam(required = false) BookingStatus status
    ) {
        return bookingService
                .listCurrentUserBookings(status);
    }

    @GetMapping("/repair-shops/{repairShopId}")
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')")
    public List<BookingResponse> getRepairShopBookings(
            @PathVariable Long repairShopId,
            @RequestParam(required = false) BookingStatus status
    ) {
        return bookingService.listRepairShopBookings(
                repairShopId,
                status
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('USER', 'SHOP_OWNER', 'ADMIN')"
    )
    public BookingResponse getById(
            @PathVariable Long id
    ) {
        return bookingService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> list(
            @RequestParam(required = false) Long repairShopId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) BookingStatus status
    ) {
        return bookingService.list(
                repairShopId,
                clientId,
                status
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('SHOP_OWNER', 'ADMIN')"
    )
    public BookingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateStatusRequest request
    ) {
        return bookingService.updateStatus(
                id,
                request.status()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        bookingService.softDelete(id);
    }
}