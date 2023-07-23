package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequest addNew(ItemRequest itemRequest, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId)));
        itemRequest.setRequester(user);
        return itemRequestRepo.save(itemRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestWithItemsDto> getAllOwn(Long ownerId) {
        if (!userRepo.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", ownerId));
        }
        List<ItemResponseDto> unfilteredItems = itemRepo.findAllByRequestIdNotNull().stream()
                .map(itemMapper::toItemResponseDto)
                .collect(Collectors.toList());
        return itemRequestRepo.findAllByRequesterId(ownerId).stream()
                .map(itemRequestMapper::toItemRequestWithItemsDto)
                .peek(itemRequestWithItemsDto -> itemRequestWithItemsDto.setItems(
                        unfilteredItems.stream()
                                .filter(itemWithIdResponseDto ->
                                        itemWithIdResponseDto.getRequestId().equals(itemRequestWithItemsDto.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestWithItemsDto> getAllByPages(Long userId, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        List<ItemResponseDto> unfilteredItems = itemRepo.findAllByRequestIdNotNull().stream()
                .map(itemMapper::toItemResponseDto)
                .collect(Collectors.toList());
        return itemRequestRepo.findAllByRequesterIdNot(
                        userId, PageRequest.of(fromPage, size, Sort.by(Sort.Direction.DESC, "created")))
                .stream()
                .map(itemRequestMapper::toItemRequestWithItemsDto)
                .peek(itemRequestWithItemsDto -> itemRequestWithItemsDto.setItems(
                        unfilteredItems.stream()
                                .filter(itemWithIdResponseDto ->
                                        itemWithIdResponseDto.getRequestId().equals(itemRequestWithItemsDto.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestWithItemsDto getById(Long requestId, Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", userId));
        }
        ItemRequestWithItemsDto itemRequestWithItemsDto = itemRequestMapper.toItemRequestWithItemsDto(
                itemRequestRepo.findById(requestId)
                        .orElseThrow(
                                () -> new NotFoundException(String.format("Request ID = %d not found!", requestId))));
        itemRequestWithItemsDto.setItems(itemRepo.findAllByRequestId(requestId).stream()
                .map(itemMapper::toItemResponseDto)
                .collect(Collectors.toList()));
        return itemRequestWithItemsDto;
    }
}
