package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto addNew(@RequestBody ItemRequestDto itemRequestDto,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addNew(itemRequestDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto patchUpdate(@RequestBody ItemRequestDto itemRequestDto,
                                          @PathVariable Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.patchUpdate(itemRequestDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ExtendedItemResponseDto getById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ExtendedItemResponseDto> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "0") Integer from,
                                                          @RequestParam(defaultValue = "20") Integer size) {
        return itemService.getAllOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ExtendedItemResponseDto> findAvailableByText(@RequestParam String text,
                                                             @RequestParam(defaultValue = "0") Integer from,
                                                             @RequestParam(defaultValue = "20") Integer size) {
        return itemService.findAvailableByText(text, from, size);
    }

    @PostMapping("{itemId}/comment")
    public CommentResponseDto addNewComment(@RequestBody @Valid CommentRequestDto commentRequestDto,
                                            @RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long itemId) {
        return itemService.addNewComment(commentRequestDto, userId, itemId);
    }
}