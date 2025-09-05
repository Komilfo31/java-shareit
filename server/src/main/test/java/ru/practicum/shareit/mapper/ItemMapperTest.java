package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void ShouldMapItemToItemDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        ItemDto itemDto = itemMapper.toDto(item);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isEqualTo("Test Description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isNull();
    }

    @Test
    void ShouldMapItemWithRequestToItemDto() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(10L);
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .request(itemRequest)
                .build();

        ItemDto itemDto = itemMapper.toDto(item);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void ShouldMapItemDtoToItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(10L)
                .build();

        Item item = itemMapper.toItem(itemDto);

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Test Item");
        assertThat(item.getDescription()).isEqualTo("Test Description");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getId()).isNull();
    }

    @Test
    void ShouldMapCommentToCommentDto() {
        User author = User.builder()
                .id(1L)
                .name("Author")
                .email("author@email.com")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .author(author)
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

        CommentDto commentDto = itemMapper.toCommentDto(comment);

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Great item!");
        assertThat(commentDto.getAuthorName()).isEqualTo("Author");
        assertThat(commentDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void ShouldMapCommentDtoToComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        Comment comment = itemMapper.toComment(commentDto);

        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getId()).isNull();
        assertThat(comment.getAuthor()).isNull();
    }

    @Test
    void ShouldMapBookingToBookingDto() {
        User booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 1, 10, 0))
                .end(LocalDateTime.of(2023, 1, 2, 10, 0))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        BookingDto bookingDto = itemMapper.toBookingDto(booking);

        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2023, 1, 2, 10, 0));
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingDto.getBooker().getId()).isEqualTo(2L);
        assertThat(bookingDto.getBooker().getName()).isEqualTo("Booker");
        assertThat(bookingDto.getItem().getId()).isEqualTo(1L);
        assertThat(bookingDto.getItem().getName()).isEqualTo("Test Item");
    }

    @Test
    void ShouldReturnNullWhenBookingIsNull() {
        BookingDto bookingDto = itemMapper.toBookingDto(null);

        assertThat(bookingDto).isNull();
    }

    @Test
    void ShouldReturnNullWhenBookerIsNull() {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 1, 10, 0))
                .end(LocalDateTime.of(2023, 1, 2, 10, 0))
                .status(BookingStatus.APPROVED)
                .booker(null)
                .item(item)
                .build();

        BookingDto bookingDto = itemMapper.toBookingDto(booking);

        assertThat(bookingDto).isNull();
    }

    @Test
    void ShouldReturnNullWhenItemIsNull() {
        User booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 1, 10, 0))
                .end(LocalDateTime.of(2023, 1, 2, 10, 0))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(null)
                .build();

        BookingDto bookingDto = itemMapper.toBookingDto(booking);

        assertThat(bookingDto).isNull();
    }

    @Test
    void ShouldMapUserToBookerDto() {
        User booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        BookingDto.BookerDto bookerDto = invokePrivateCreateBookerDto(booker);

        assertThat(bookerDto).isNotNull();
        assertThat(bookerDto.getId()).isEqualTo(2L);
        assertThat(bookerDto.getName()).isEqualTo("Booker");
    }

    @Test
    void createItemDtoShouldMapItemToItemDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .build();

        BookingDto.ItemDto itemDto = invokePrivateCreateItemDto(item);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
    }

    private BookingDto.BookerDto invokePrivateCreateBookerDto(User booker) {
        try {
            var method = ItemMapper.class.getDeclaredMethod("createBookerDto", User.class);
            method.setAccessible(true);
            return (BookingDto.BookerDto) method.invoke(itemMapper, booker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }

    private BookingDto.ItemDto invokePrivateCreateItemDto(Item item) {
        try {
            var method = ItemMapper.class.getDeclaredMethod("createItemDto", Item.class);
            method.setAccessible(true);
            return (BookingDto.ItemDto) method.invoke(itemMapper, item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}