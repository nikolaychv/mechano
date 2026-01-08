package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.service.BookingService;
import bg.mechano.mechano.web.dto.booking.BookingCreateRequest;
import bg.mechano.mechano.web.dto.booking.BookingResponse;
import bg.mechano.mechano.web.dto.booking.BookingUpdateStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request) {
        return bookingService.create(request);
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @GetMapping
    public List<BookingResponse> list(
            @RequestParam(required = false) Long repairShopId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) BookingStatus status
    ) {
        return bookingService.list(repairShopId, clientId, status);
    }

    @PatchMapping("/{id}/status")
    public BookingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateStatusRequest request
    ) {
        return bookingService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookingService.softDelete(id);
    }
}
