package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest addNew(ItemRequest itemRequest, Long userId);

    List<ItemRequestWithItemsDto> getAllOwn(Long ownerId);

    List<ItemRequestWithItemsDto> getAllByPages(Long userId, Integer from, Integer size);

    ItemRequestWithItemsDto getById(Long requestId, Long userId);
}
