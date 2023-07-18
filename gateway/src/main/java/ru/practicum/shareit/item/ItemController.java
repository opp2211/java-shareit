package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validator.validationGroups.OnCreate;
import ru.practicum.shareit.validator.validationGroups.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @Validated(OnCreate.class)
    @PostMapping
    public ResponseEntity<Object> addNew(@RequestBody @Valid ItemRequestDto itemRequestDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.addNew(itemRequestDto, userId);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patchUpdate(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                          @PathVariable Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.patchUpdate(itemRequestDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                     @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.getAllOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findAvailableByText(@RequestParam String text,
                                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                        @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.findAvailableByText(text, from, size);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> addNewComment(@RequestBody @Valid CommentRequestDto commentRequestDto,
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId) {
        return itemClient.addNewComment(commentRequestDto, itemId, userId);
    }
}
