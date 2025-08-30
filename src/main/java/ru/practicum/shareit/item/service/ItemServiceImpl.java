package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemNotOwnedException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new ItemNotOwnedException("Данный товар не принадлежит владельцу");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));

        ItemDto itemDto = itemMapper.toDto(item);

        if (item.getOwner().getId().equals(userId)) {
            BookingDto bookingInfo = bookingRepository.findLastAndNextBookingsForItem(itemId);
            if (bookingInfo != null) {
                itemDto.setLastBooking(itemMapper.toBookingDto(bookingInfo.getLastBooking()));
                itemDto.setNextBooking(itemMapper.toBookingDto(bookingInfo.getNextBooking()));
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(comments.stream()
                .map(itemMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId, Pageable pageable) {
        List<Item> items = itemRepository.findByOwnerId(ownerId, pageable);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<Booking> lastBookings = new ArrayList<>();
        List<Booking> nextBookings = new ArrayList<>();

        for (Long itemId : itemIds) {
            List<Booking> itemLastBookings = bookingRepository.findLastBooking(itemId, LocalDateTime.now());
            List<Booking> itemNextBookings = bookingRepository.findNextBooking(itemId, LocalDateTime.now());

            if (!itemLastBookings.isEmpty()) {
                lastBookings.add(itemLastBookings.get(0));
            }
            if (!itemNextBookings.isEmpty()) {
                nextBookings.add(itemNextBookings.get(0));
            }
        }

        Map<Long, Booking> lastBookingMap = lastBookings.stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), booking -> booking));

        Map<Long, Booking> nextBookingMap = nextBookings.stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), booking -> booking));

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = itemMapper.toDto(item);

                    if (item.getOwner().getId().equals(ownerId)) {
                        itemDto.setLastBooking(itemMapper.toBookingDto(lastBookingMap.get(item.getId())));
                        itemDto.setNextBooking(itemMapper.toBookingDto(nextBookingMap.get(item.getId())));
                    }

                    List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());
                    itemDto.setComments(itemComments.stream()
                            .map(itemMapper::toCommentDto)
                            .collect(Collectors.toList()));

                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text, Pageable pageable) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.search(text, pageable);
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Comment addComment(Long itemId, Comment comment, Long authorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Товар не найден"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndEndBefore(
                itemId, authorId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new CommentNotAllowedException("Вы не можете комментировать этот товар");
        }

        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    private void addBookingInfo(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
        List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);

        if (lastBookings != null && !lastBookings.isEmpty()) {
            itemDto.setLastBooking(itemMapper.toBookingDto(lastBookings.get(0)));
        }
        if (nextBookings != null && !nextBookings.isEmpty()) {
            itemDto.setNextBooking(itemMapper.toBookingDto(nextBookings.get(0)));
        }
    }
}