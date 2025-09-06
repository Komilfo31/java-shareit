package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = ?1 " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBooking(Long itemId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = ?1 " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > ?2 " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBooking(Long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime end);

    @Query("SELECT (b.item.id, " +
            "(SELECT b1 FROM Booking b1 WHERE b1.item.id = :itemId AND b1.status = 'APPROVED' " +
            "AND b1.start <= CURRENT_TIMESTAMP ORDER BY b1.start DESC LIMIT 1), " +
            "(SELECT b2 FROM Booking b2 WHERE b2.item.id = :itemId AND b2.status = 'APPROVED' " +
            "AND b2.start > CURRENT_TIMESTAMP ORDER BY b2.start ASC LIMIT 1)) " +
            "FROM Booking b WHERE b.item.id = :itemId")
    BookingDto findLastAndNextBookingsForItem(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.status = :status")
    List<Booking> findByItemIdAndBookerIdAndStatus(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("status") BookingStatus status);
}
