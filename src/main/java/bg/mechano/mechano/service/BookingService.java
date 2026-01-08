package bg.mechano.mechano.service;

import bg.mechano.mechano.domain.enums.BookingStatus;
import bg.mechano.mechano.web.dto.booking.BookingCreateRequest;
import bg.mechano.mechano.web.dto.booking.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse create(BookingCreateRequest request);

    BookingResponse getById(Long id);

    List<BookingResponse> list(Long repairShopId, Long clientId, BookingStatus status);

    BookingResponse updateStatus(Long bookingId, BookingStatus status);

    void softDelete(Long bookingId);
}

