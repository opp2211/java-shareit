package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestWithItemsDto toItemRequestWithItemsDto(ItemRequest itemRequest) {
        return ItemRequestWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
