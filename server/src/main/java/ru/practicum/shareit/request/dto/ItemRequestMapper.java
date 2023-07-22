package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public abstract class ItemRequestMapper {
    public abstract ItemRequestWithItemsDto toItemRequestWithItemsDto(ItemRequest itemRequest);
}
