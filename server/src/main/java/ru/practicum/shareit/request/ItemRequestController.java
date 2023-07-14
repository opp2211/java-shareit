package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequest addNew(@RequestBody @Valid ItemRequest itemRequest,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.addNew(itemRequest, userId);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getAllOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllByPages(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                       @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemRequestService.getAllByPages(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getById(@PathVariable Long requestId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getById(requestId, userId);
    }
}
