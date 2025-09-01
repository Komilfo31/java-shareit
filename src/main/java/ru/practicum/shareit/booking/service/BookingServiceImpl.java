package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingAlreadyApprovedException;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.BookingOwnItemException;
import ru.practicum.shareit.exception.InvalidDateTimeException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Booking create(Booking booking, Long itemId, Long bookerId) {
        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new InvalidDateTimeException("Дата начала и окончания обязательны");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Товар недоступен для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new BookingOwnItemException("Вы не можете забронировать свой товар");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new InvalidDateTimeException("Дата окончания должна быть позже даты начала");
        }

        if (booking.getEnd().isEqual(booking.getStart())) {
            throw new InvalidDateTimeException("Дата начала и окончания не могут быть одинаковы");
        }

        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new InvalidDateTimeException("Дата начала должна быть в будущем");
        }

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approve(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронь не найдена"));

        if (booking.getItem() == null || booking.getItem().getOwner() == null) {
            throw new BookingNotFoundException("Бронь содержит некорректные данные");
        }

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new BookingOwnItemException("Подтвердить бронирование может только владелец товара");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingAlreadyApprovedException("Бронирование уже подтверждено/отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронь не найдена"));

        if (booking.getBooker() == null || booking.getItem() == null || booking.getItem().getOwner() == null) {
            throw new BookingNotFoundException("Бронь содержит некорректные данные");
        }

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingNotFoundException("Доступ к бронированию запрещен");
        }

        return booking;
    }

    @Override
    public List<Booking> getAllByBooker(Long bookerId, String bookingState, Pageable pageable) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        BookingState state;

        try {
            state = BookingState.valueOf(bookingState.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный статус: " + bookingState);
        }

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(bookerId, pageable);
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, now, now, pageable);
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                        bookerId, now, pageable);
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId, now, pageable);
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.REJECTED, pageable);
            default:
                throw new IllegalArgumentException("Неизвестный статус: " + bookingState);
        }
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, String bookingState, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        BookingState state;

        try {
            state = BookingState.valueOf(bookingState.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный статус: " + bookingState);
        }

        switch (state) {
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, now, now, pageable);
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        ownerId, now, pageable);
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                        ownerId, now, pageable);
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.REJECTED, pageable);
            default:
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
        }
    }

}
