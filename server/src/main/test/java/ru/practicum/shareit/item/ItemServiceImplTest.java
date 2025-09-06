package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemNotOwnedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User author;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@email.com");

        author = new User();
        author.setId(2L);
        author.setName("Author");
        author.setEmail("author@email.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need item");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequest);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    @Transactional
    void shouldCreateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(1L, itemDto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(itemRequestRepository).findById(1L);
        verify(itemRepository).save(item);
    }

    @Test
    void shouldCreateItemWithoutRequest() {
        itemDto.setRequestId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(1L, itemDto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowExceptionWhenCreateItemWithInvalidUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemService.create(999L, itemDto));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void shouldThrowExceptionWhenCreateItemWithInvalidRequest() {
        itemDto.setRequestId(999L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(1L, itemDto));

        assertThat(exception.getMessage()).contains("Request not found");
    }

    @Test
    @Transactional
    void shouldUpdateItem() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getAvailable()).isFalse();
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowExceptionWhenUpdateItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.update(999L, 1L, itemDto));

        assertThat(exception.getMessage()).isEqualTo("Товар не найден");
    }

    @Test
    void shouldThrowExceptionWhenUpdateItemNotOwned() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemNotOwnedException exception = assertThrows(ItemNotOwnedException.class,
                () -> itemService.update(1L, 999L, itemDto));

        assertThat(exception.getMessage()).isEqualTo("Данный товар не принадлежит владельцу");
    }

    @Test
    void shouldGetItemById() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemDto result = itemService.getById(1L, 1L);

        assertThat(result).isNotNull();
        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItemId(1L);
    }

    @Test
    void shouldGetItemByIdWithBookingInfoForOwner() {
        BookingDto bookingInfo = new BookingDto();
        bookingInfo.setLastBooking(new Booking());
        bookingInfo.setNextBooking(new Booking());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(bookingRepository.findLastAndNextBookingsForItem(1L)).thenReturn(bookingInfo);
        when(itemMapper.toBookingDto(any(Booking.class))).thenReturn(new BookingDto());
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemDto result = itemService.getById(1L, 1L);

        assertThat(result).isNotNull();
        verify(bookingRepository).findLastAndNextBookingsForItem(1L);
    }

    @Test
    void shouldGetAllByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(itemRepository).findByOwnerId(1L, pageable);
        verify(commentRepository).findByItemIdIn(List.of(1L));
        verify(bookingRepository).findLastBooking(eq(1L), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoItemsForOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).isEmpty();
        verify(commentRepository, never()).findByItemIdIn(any());
    }

    @Test
    void shouldSearchItems() {
        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.search("test", pageable)).thenReturn(List.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("test", pageable);

        assertThat(result).hasSize(1);
        verify(itemRepository).search("test", pageable);
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank() {
        List<ItemDto> result = itemService.search("   ", PageRequest.of(0, 10));

        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(anyString(), any());
    }

    @Test
    @Transactional
    void shouldAddComment() {
        Booking booking = new Booking();
        booking.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(eq(1L), eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = itemService.addComment(1L, comment, 2L);

        assertThat(result).isNotNull();
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByItemIdAndBookerIdAndEndBefore(eq(1L), eq(2L), any(LocalDateTime.class));
        verify(commentRepository).save(comment);
    }

    @Test
    void shouldThrowExceptionWhenAddCommentToInvalidItem() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.addComment(999L, comment, 2L));

        assertThat(exception.getMessage()).isEqualTo("Товар не найден");
    }

    @Test
    void shouldThrowExceptionWhenAddCommentWithInvalidUser() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemService.addComment(1L, comment, 999L));

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void shouldThrowExceptionWhenAddCommentWithoutBooking() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(eq(1L), eq(2L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        CommentNotAllowedException exception = assertThrows(CommentNotAllowedException.class,
                () -> itemService.addComment(1L, comment, 2L));

        assertThat(exception.getMessage()).isEqualTo("Вы не можете комментировать этот товар");

        verify(bookingRepository).findByItemIdAndBookerIdAndEndBefore(eq(1L), eq(2L), any(LocalDateTime.class));
    }

    @Test
    void shouldHandleGetAllByOwnerIdWithBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(nextBooking));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(itemMapper.toBookingDto(lastBooking)).thenReturn(new BookingDto());
        when(itemMapper.toBookingDto(nextBooking)).thenReturn(new BookingDto());

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findLastBooking(eq(1L), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(1L), any(LocalDateTime.class));
        verify(itemMapper, times(2)).toBookingDto(any(Booking.class));
    }

    @Test
    void shouldGetItemByIdForNonOwner() {
        Long nonOwnerId = 999L;

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemDto result = itemService.getById(1L, nonOwnerId);

        assertThat(result).isNotNull();
        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItemId(1L);
        verify(bookingRepository, never()).findLastAndNextBookingsForItem(anyLong());
    }

    @Test
    void shouldGetItemByIdWithNoBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(bookingRepository.findLastAndNextBookingsForItem(1L)).thenReturn(null);
        when(commentRepository.findByItemId(1L)).thenReturn(List.of(comment));

        ItemDto result = itemService.getById(1L, 1L);

        assertThat(result).isNotNull();
        verify(bookingRepository).findLastAndNextBookingsForItem(1L);
    }

    @Test
    void shouldHandleGetAllByOwnerIdWithMultipleItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(owner);

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item, item2));
        when(commentRepository.findByItemIdIn(List.of(1L, 2L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(anyLong(), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBooking(anyLong(), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(2);
        verify(bookingRepository, times(2)).findLastBooking(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(2)).findNextBooking(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void shouldHandleEmptyBookingsInGetAllByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(itemMapper, never()).toBookingDto(any(Booking.class));
    }

    @Test
    void shouldUpdateItemWithPartialData() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name Only");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name Only");
        verify(itemRepository).save(item);
    }

    @Test
    void shouldUpdateItemWithNullFields() {
        ItemDto updateDto = new ItemDto();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        verify(itemRepository).save(item);
    }

    @Test
    void shouldHandleLambdaExpressionsInGetAllByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(now.minusDays(3))
                .end(now.minusDays(2))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking currentBooking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1))
                .end(now.plusDays(1))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking futureBooking = Booking.builder()
                .id(3L)
                .start(now.plusDays(2))
                .end(now.plusDays(3))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(pastBooking, currentBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(futureBooking, currentBooking));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(itemMapper.toBookingDto(any(Booking.class))).thenReturn(new BookingDto());

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(itemMapper, atLeastOnce()).toBookingDto(any(Booking.class));
    }

    @Test
    void shouldHandleDifferentBookingStatuses() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        // Бронирования с разными статусами
        Booking waitingBooking = Booking.builder()
                .id(1L)
                .start(now.minusDays(1))
                .end(now.plusDays(1))
                .booker(owner)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();

        Booking rejectedBooking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1))
                .end(now.plusDays(1))
                .booker(owner)
                .item(item)
                .status(BookingStatus.REJECTED)
                .build();

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(waitingBooking, rejectedBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(waitingBooking, rejectedBooking));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(itemMapper.toBookingDto(any(Booking.class))).thenReturn(new BookingDto());

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(itemMapper, atLeast(2)).toBookingDto(any(Booking.class));
    }

    @Test
    void shouldHandleMultipleBookingsForSameItem() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Booking oldBooking = Booking.builder()
                .id(1L)
                .start(now.minusDays(5))
                .end(now.minusDays(4))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking recentBooking = Booking.builder()
                .id(2L)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking futureBooking1 = Booking.builder()
                .id(3L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        Booking futureBooking2 = Booking.builder()
                .id(4L)
                .start(now.plusDays(3))
                .end(now.plusDays(4))
                .booker(owner)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(comment));
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(oldBooking, recentBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(futureBooking1, futureBooking2));
        when(itemMapper.toDto(item)).thenReturn(itemDto);
        when(itemMapper.toBookingDto(any(Booking.class))).thenReturn(new BookingDto());

        List<ItemDto> result = itemService.getAllByOwnerId(1L, pageable);

        assertThat(result).hasSize(1);
        verify(itemMapper, atLeast(2)).toBookingDto(any(Booking.class));
    }

}