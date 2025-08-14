package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден по id: " + userId));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item наименование не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Доступность не может быть пустой");
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        if (!itemStorage.isOwner(itemId, userId)) {
            throw new NotFoundException("Товар не найден или пользователь не является владельцем");
        }

        Item existingItem = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Товар не найден по id: " + itemId));

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemStorage.update(existingItem));
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        return ItemMapper.toItemDto(itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Товар не найден по id: " + itemId)));
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId) {
        if (!userStorage.existsById(ownerId)) {
            throw new NotFoundException("Пользователь не найден по id: " + ownerId);
        }
        return itemStorage.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
