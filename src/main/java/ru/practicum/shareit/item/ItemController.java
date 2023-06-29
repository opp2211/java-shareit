package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto addNew(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addNew(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchUpdate(@RequestBody @Valid ItemDto itemDto,
                               @PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.patchUpdate(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findAvailableByText(@RequestParam String text) {
        return itemService.findAvailableByText(text);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addNewComment(@RequestBody @Valid Comment comment,
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId) {
        return itemService.addNewComment(comment, userId, itemId);
    }
}