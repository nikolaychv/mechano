package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Booking;
import bg.mechano.mechano.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for managing {@link Booking} entities.
 *
 * Provides data access operations for repairShop bookings,
 * including retrieval by repairShop, client, and booking status.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        select count(b) > 0
        from Booking b
        where b.repairShop.id = :repairShopId
          and b.deletedAt is null
          and b.status <> :cancelled
          and b.startTime < :endTime
          and b.endTime > :startTime
    """)
    boolean existsOverlappingBooking(
            Long repairShopId,
            Instant startTime,
            Instant endTime,
            BookingStatus cancelled
    );

    @Query("""
        select b
        from Booking b
        where b.deletedAt is null
          and (:repairShopId is null or b.repairShop.id = :repairShopId)
          and (:clientId is null or b.client.id = :clientId)
          and (:status is null or b.status = :status)
        order by b.createdAt desc
    """)
    List<Booking> findActiveByFilters(Long repairShopId, Long clientId, BookingStatus status);
}
