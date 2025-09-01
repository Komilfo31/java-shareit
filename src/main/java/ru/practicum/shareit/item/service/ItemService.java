package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDto update(Long itemId, Long ownerId, ItemDto itemDto);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getAllByOwnerId(Long ownerId, Pageable pageable);

    List<ItemDto> search(String text, Pageable pageable);

    Comment addComment(Long itemId, Comment comment, Long authorId);
}
