package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingNearest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
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
        return ItemMapper.toItemDto(itemRepository.save(item), null, null, Collections.EMPTY_LIST);
    }

    @Override
    @Transactional
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
        return ItemMapper.toItemDto(itemRepository.save(item), null, null, Collections.EMPTY_LIST);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        BookingNearest lastBooking = null;
        BookingNearest nextBooking = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            lastBooking = BookingMapper.toBookingNearest(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                    itemId, BookingStatus.APPROVED, LocalDateTime.now()));
            nextBooking = BookingMapper.toBookingNearest(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    itemId, BookingStatus.APPROVED, LocalDateTime.now()));
        }
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllOwnerItems(Long userId) {
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> ItemMapper.toItemDto(item,
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isBefore(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .max(Comparator.comparing(BookingNearest::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isAfter(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .min(Comparator.comparing(BookingNearest::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredComments.stream()
                                .filter(comment -> Objects.equals(comment.getItem().getId(), item.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAvailableByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByStatus(BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();
        return itemRepository.searchAvailByText(text).stream()
                .map(item -> ItemMapper.toItemDto(item,
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isBefore(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .max(Comparator.comparing(BookingNearest::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isAfter(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .min(Comparator.comparing(BookingNearest::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredComments.stream()
                                .filter(comment -> Objects.equals(comment.getItem().getId(), item.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addNewComment(Comment comment, Long userId, Long itemId) {
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ValidationException();
        }
        comment.setItem(itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId))));
        comment.setAuthor(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}
