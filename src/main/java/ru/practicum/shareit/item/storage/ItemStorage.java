package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addNew(Item item);

    Item update(Item item);

    Item getById(Long itemId);

    List<Item> getAllByOwnerId(Long userId);

    List<Item> findAvailableByText(String text);
}