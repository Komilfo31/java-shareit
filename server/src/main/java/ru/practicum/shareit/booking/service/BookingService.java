package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    Booking create(Booking booking, Long itemId, Long userId);

    Booking approve(Long bookingId, Long userId, Boolean approved);

    Booking getById(Long bookingId, Long userId);

    List<Booking> getAllByBooker(Long bookerId, String state, Pageable pageable);

    List<Booking> getAllByOwner(Long ownerId, String state, Pageable pageable);
}
