package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNew(ItemDto itemDto, Long userId);

    ItemDto patchUpdate(ItemDto itemDto, Long itemId, Long userId);

    ItemDto getById(Long itemId);

    List<ItemDto> getAllOwnerItems(Long userId);

    List<ItemDto> findAvailableByText(String text);
}