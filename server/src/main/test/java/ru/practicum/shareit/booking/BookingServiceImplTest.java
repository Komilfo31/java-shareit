package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@email.ru")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .build();
    }

    @Test
    void shouldCreateBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking result = bookingService.create(booking, 1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowExceptionWhenCreateBookingWithNullDates() {
        Booking invalidBooking = Booking.builder()
                .start(null)
                .end(null)
                .build();

        InvalidDateTimeException exception = assertThrows(InvalidDateTimeException.class,
                () -> bookingService.create(invalidBooking, 1L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Дата начала и окончания обязательны");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.create(booking, 1L, 999L));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.create(booking, 999L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Товар не найден");
    }

    @Test
    void shouldThrowExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemNotAvailableException exception = assertThrows(ItemNotAvailableException.class,
                () -> bookingService.create(booking, 1L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Товар недоступен для бронирования");
    }

    @Test
    void shouldThrowExceptionWhenBookingOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingOwnItemException exception = assertThrows(BookingOwnItemException.class,
                () -> bookingService.create(booking, 1L, 1L));

        assertThat(exception.getMessage()).isEqualTo("Вы не можете забронировать свой товар");
    }

    @Test
    void shouldThrowExceptionWhenEndBeforeStart() {
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStart(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        InvalidDateTimeException exception = assertThrows(InvalidDateTimeException.class,
                () -> bookingService.create(booking, 1L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Дата окончания должна быть позже даты начала");
    }

    @Test
    void shouldThrowExceptionWhenEndEqualsStart() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        booking.setStart(sameTime);
        booking.setEnd(sameTime);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        InvalidDateTimeException exception = assertThrows(InvalidDateTimeException.class,
                () -> bookingService.create(booking, 1L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Дата начала и окончания не могут быть одинаковы");
    }

    @Test
    void shouldThrowExceptionWhenStartInPast() {
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        InvalidDateTimeException exception = assertThrows(InvalidDateTimeException.class,
                () -> bookingService.create(booking, 1L, 2L));

        assertThat(exception.getMessage()).isEqualTo("Дата начала должна быть в будущем");
    }

    @Test
    void shouldThrowExceptionWhenApproveBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.approve(999L, 1L, true));

        assertThat(exception.getMessage()).isEqualTo("Бронь не найдена");
    }

    @Test
    void shouldThrowExceptionWhenApproveByNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingOwnItemException exception = assertThrows(BookingOwnItemException.class,
                () -> bookingService.approve(1L, 999L, true));

        assertThat(exception.getMessage()).isEqualTo("Подтвердить бронирование может только владелец товара");
    }

    @Test
    void shouldThrowExceptionWhenApproveAlreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingAlreadyApprovedException exception = assertThrows(BookingAlreadyApprovedException.class,
                () -> bookingService.approve(1L, 1L, true));

        assertThat(exception.getMessage()).isEqualTo("Бронирование уже подтверждено/отклонено");
    }

    @Test
    void shouldGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getById(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void shouldGetBookingByIdByOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGetBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.getById(999L, 1L));

        assertThat(exception.getMessage()).isEqualTo("Бронь не найдена");
    }

    @Test
    void shouldThrowExceptionWhenGetBookingUnauthorized() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.getById(1L, 999L));

        assertThat(exception.getMessage()).isEqualTo("Доступ к бронированию запрещен");
    }

    @Test
    void shouldGetAllByBookerAllState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "ALL", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(2L, pageable);
    }

    @Test
    void shouldThrowExceptionWhenInvalidState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(2L, "INVALID", pageable));

        assertThat(exception.getMessage()).isEqualTo("Неизвестный статус: INVALID");
    }

    @Test
    void shouldGetAllByOwnerAllState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(1L, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "ALL", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(1L, pageable);
    }

    @Test
    void shouldGetAllByOwnerWaitingState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "WAITING", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldApproveBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approve(1L, 1L, true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldRejectBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approve(1L, 1L, false);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldGetAllByBookerCurrentState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "CURRENT", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
    }

    @Test
    void shouldGetAllByBookerPastState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "PAST", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
    }

    @Test
    void shouldGetAllByBookerFutureState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "FUTURE", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
    }

    @Test
    void shouldGetAllByBookerWaitingState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.WAITING, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "WAITING", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
    }

    @Test
    void shouldGetAllByBookerRejectedState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.REJECTED, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByBooker(2L, "REJECTED", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
    }

    @Test
    void shouldGetAllByOwnerCurrentState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "CURRENT", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetAllByOwnerPastState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "PAST", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetAllByOwnerFutureState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "FUTURE", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetAllByOwnerRejectedState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.REJECTED, pageable))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllByOwner(1L, "REJECTED", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForGetAllByBooker() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllByBooker(999L, "ALL", pageable));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForGetAllByOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllByOwner(999L, "ALL", pageable));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void lambdaGetByIdShouldWorkCorrectly() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getById(1L, 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void lambdaCreateShouldWorkCorrectly() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.create(booking, 1L, 2L);
        assertThat(result).isNotNull();
    }

    @Test
    void lambdaApproveShouldWorkCorrectly() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approve(1L, 1L, true);
        assertThat(result).isNotNull();
    }
}