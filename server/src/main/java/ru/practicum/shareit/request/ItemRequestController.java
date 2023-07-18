package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequest addNew(@RequestBody ItemRequest itemRequest,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.addNew(itemRequest, userId);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getAllOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllByPages(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "20") Integer size) {
        return itemRequestService.getAllByPages(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getById(@PathVariable Long requestId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getById(requestId, userId);
    }
}
