package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public Item toItem(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .build();
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null || booking.getBooker() == null || booking.getItem() == null) {
            return null;
        }

        return BookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(createBookerDto(booking.getBooker()))
                .item(createItemDto(booking.getItem()))
                .build();
    }

    private BookingDto.BookerDto createBookerDto(User booker) {
        return BookingDto.BookerDto.builder()
                .id(booker.getId())
                .name(booker.getName())
                .build();
    }

    private BookingDto.ItemDto createItemDto(Item item) {
        return BookingDto.ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }
}