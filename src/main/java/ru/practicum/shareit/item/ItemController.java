package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDtoWithBooking addNew(@RequestBody @Valid CreateItemDto createItemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addNew(createItemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoWithBooking patchUpdate(@RequestBody @Valid ItemDtoWithBooking itemDtoWithBooking,
                                          @PathVariable Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.patchUpdate(itemDtoWithBooking, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking getById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoWithBooking> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                     @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemService.getAllOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDtoWithBooking> findAvailableByText(@RequestParam String text,
                                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                        @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemService.findAvailableByText(text, from, size);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto addNewComment(@RequestBody @Valid Comment comment,
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId) {
        return itemService.addNewComment(comment, userId, itemId);
    }
}