package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingNearest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static Item toItem(CreateItemDto createItemDto) {
        return Item.builder()
                .name(createItemDto.getName())
                .description(createItemDto.getDescription())
                .available(createItemDto.getAvailable())
                .build();
    }

    public static ItemDtoWithBooking toItemDtoWithBooking(Item item, BookingNearest lastBooking, BookingNearest nextBooking,
                                                          List<Comment> comments) {
        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments.stream()
                        .map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public static ItemDtoForItemRequest toItemDtoForItemRequest(Item item) {
        return ItemDtoForItemRequest.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }
}
