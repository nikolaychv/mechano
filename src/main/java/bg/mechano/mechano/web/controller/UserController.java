package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.service.BookingService;
import bg.mechano.mechano.service.UserService;
import bg.mechano.mechano.web.dto.booking.BookingResponse;
import bg.mechano.mechano.web.dto.user.UserCreateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.dto.user.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getUserBookings(
            @PathVariable Long id,
            @RequestParam(required = false) BookingStatus status
    ) {
        return bookingService.list(
                null,
                id,
                status
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return userService.update(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.softDelete(id);
    }
}