package bg.mechano.mechano.domain.entity;

import bg.mechano.mechano.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a booking entity in the system.
 * This entity maps to the "bookings" table in the database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_id", nullable = false)
    private RepairShop repairShop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_shop_type_id", nullable = false)
    private RepairShopType repairShopType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_brand_id")
    private CarBrand carBrand;

    @Column(name = "car_model", length = 255)
    private String carModel;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "customer_name_at_booking", length = 255)
    private String customerNameAtBooking;

    @Column(name = "customer_phone_at_booking", length = 50)
    private String customerPhoneAtBooking;

    @Column(name = "notes_for_service", columnDefinition = "text")
    private String notesForService;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
