package bg.mechano.mechano.web.dto.booking;

import bg.mechano.mechano.domain.enums.BookingStatus;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long repairShopId,
        Long clientId,
        Long serviceTypeId,
        Long carBrandId,
        String carModel,
        Instant startTime,
        Instant endTime,
        BookingStatus status,
        String customerNameAtBooking,
        String customerPhoneAtBooking,
        String notesForService,
        Instant createdAt
) {}

