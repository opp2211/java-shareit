package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDtoWithBooking addNew(CreateItemDto createItemDto, Long userId);

    ItemDtoWithBooking patchUpdate(ItemDtoWithBooking itemDtoWithBooking, Long itemId, Long userId);

    ItemDtoWithBooking getById(Long itemId, Long userId);

    List<ItemDtoWithBooking> getAllOwnerItems(Long userId);

    List<ItemDtoWithBooking> findAvailableByText(String text);

    CommentDto addNewComment(Comment comment, Long userId, Long itemId);
}