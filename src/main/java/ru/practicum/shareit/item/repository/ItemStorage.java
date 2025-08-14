package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> search(String text);

    Item update(Item item);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean isOwner(Long itemId, Long userId);
}
