package ru.practicum.shareit.item.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingNearestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collections.class})
public abstract class ItemMapper {
    public abstract Item toItem(ItemRequestDto itemRequestDto);

    @Mapping(target = "requestId", source = "request.id")
    public abstract ItemRequestDto toItemRequestDto(Item item);

    @Mapping(target = "requestId", source = "request.id")
    public abstract ItemResponseDto toItemResponseDto(Item item);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "requestId", source = "item.request.id")
    public abstract ExtendedItemResponseDto toExtendedItemResponseDto(
            Item item, BookingNearestDto lastBooking, BookingNearestDto nextBooking, List<Comment> comments);

    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "comments", expression = "java(Collections.EMPTY_LIST)")
    public abstract ExtendedItemResponseDto toExtendedItemResponseDto(Item item);
}
