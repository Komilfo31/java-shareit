package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    private BookingMapper bookingMapper = new BookingMapper();

    @Test
    void shouldMapBookingToDto() {
        User booker = User.builder()
                .id(1L)
                .name("John Booker")
                .email("booker@example.com")
                .build();

        User owner = User.builder()
                .id(2L)
                .name("Item Owner")
                .email("owner@example.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Power Drill")
                .owner(owner)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 1, 10, 0))
                .end(LocalDateTime.of(2023, 1, 2, 10, 0))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        BookingDto dto = bookingMapper.toDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2023, 1, 2, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);

        assertThat(dto.getBooker().getId()).isEqualTo(1L);
        assertThat(dto.getBooker().getName()).isEqualTo("John Booker");

        assertThat(dto.getItem().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getName()).isEqualTo("Power Drill");
    }

    @Test
    void shouldHandleNullFieldsInBooking() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 1, 10, 0))
                .end(LocalDateTime.of(2023, 1, 2, 10, 0))
                .status(BookingStatus.WAITING)
                .build();

        assertThrows(NullPointerException.class, () -> bookingMapper.toDto(booking));
    }

    @Test
    void shouldMapWithDifferentStatuses() {
        User booker = User.builder().id(1L).name("Booker").build();
        User owner = User.builder().id(2L).name("Owner").build();
        Item item = Item.builder().id(1L).name("Item").owner(owner).build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.REJECTED)
                .booker(booker)
                .item(item)
                .build();

        BookingDto dto = bookingMapper.toDto(booking);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }
}