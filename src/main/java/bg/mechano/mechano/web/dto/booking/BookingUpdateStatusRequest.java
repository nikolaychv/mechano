package bg.mechano.mechano.web.dto.booking;

import bg.mechano.mechano.domain.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record BookingUpdateStatusRequest(
        @NotNull BookingStatus status
) {}

