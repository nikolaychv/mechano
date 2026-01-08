package bg.mechano.mechano.domain.enums;

/**
 * Represents the lifecycle status of a booking.
 *
 * The status reflects the current state of a repairShop booking
 * from creation to completion or cancellation.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
