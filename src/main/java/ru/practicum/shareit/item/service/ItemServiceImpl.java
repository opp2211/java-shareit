package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addNew(ItemDto itemDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }
        if (itemDto.getName() == null) {
            throw new ValidationException("Name field cannot be null");
        }
        if (itemDto.getDescription() == null) {
            throw new ValidationException("Description field cannot be null");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Available field cannot be null");
        }
        userStorage.getById(userId); //check user existence
        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemStorage.addNew(item));
    }

    @Override
    public ItemDto patchUpdate(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemStorage.getById(itemId);
        if (!item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("User ID and owner ID mismatch");
        }
        if (itemDto.getId() != null && !itemDto.getId().equals(itemId)) {
            throw new ValidationException("Item ID mismatch");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemStorage.update(item));
    }

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toItemDto(itemStorage.getById(itemId));
    }

    @Override
    public List<ItemDto> getAllOwnerItems(Long userId) {
        return itemStorage.getAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findAvailableByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemStorage.findAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
