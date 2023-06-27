package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingNearest;
import ru.practicum.shareit.booking.storage.BookingRepo;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepo bookingRepo;

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

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        return ItemMapper.toItemDto(itemRepository.save(item), null, null);
    }

    @Override
    public ItemDto patchUpdate(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        if (!item.getOwner().getId().equals(userId)) {
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
        return ItemMapper.toItemDto(itemRepository.save(item), null, null);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        BookingNearest lastBooking = null;
        BookingNearest nextBooking = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            lastBooking = bookingRepo.findFirstByItemIdAndEndBeforeOrderByStartDesc(itemId, LocalDateTime.now());
            nextBooking = bookingRepo.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now());
        }
        return ItemMapper.toItemDto(item, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> getAllOwnerItems(Long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> ItemMapper.toItemDto(item,
                        bookingRepo.findFirstByItemIdAndEndBeforeOrderByStartDesc(item.getId(), LocalDateTime.now()),
                        bookingRepo.findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findAvailableByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchAvailByText(text).stream()
                .map(item -> ItemMapper.toItemDto(item,
                        bookingRepo.findFirstByItemIdAndEndBeforeOrderByStartDesc(item.getId(), LocalDateTime.now()),
                        bookingRepo.findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now())))
                .collect(Collectors.toList());
    }
}
