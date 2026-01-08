package bg.mechano.mechano.web.dto.booking;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record BookingCreateRequest(
        @NotNull Long repairShopId,
        @NotNull Long clientId,
        @NotNull Long serviceTypeId,
        Long carBrandId,
        String carModel,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        String customerNameAtBooking,
        String customerPhoneAtBooking,
        String notesForService
) {}

