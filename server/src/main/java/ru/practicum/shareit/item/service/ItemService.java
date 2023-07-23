package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemResponseDto addNew(ItemRequestDto itemRequestDto, Long userId);

    ItemResponseDto patchUpdate(ItemRequestDto itemRequestDto, Long itemId, Long userId);

    ExtendedItemResponseDto getById(Long itemId, Long userId);

    List<ExtendedItemResponseDto> getAllOwnerItems(Long userId, Integer fromElement, Integer size);

    List<ExtendedItemResponseDto> findAvailableByText(String text, Integer fromElement, Integer size);

    CommentResponseDto addNewComment(CommentRequestDto commentRequestDto, Long userId, Long itemId);
}