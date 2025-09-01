package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingDto.BookerDto(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()))
                .item(new BookingDto.ItemDto(
                        booking.getItem().getId(),
                        booking.getItem().getName()))
                .build();
    }
}
