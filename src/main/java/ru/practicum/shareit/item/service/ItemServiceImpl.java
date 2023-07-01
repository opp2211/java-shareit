package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
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
    private final ItemRequestRepository itemRequestRepo;

    @Override
    @Transactional
    public ItemDtoWithBooking addNew(CreateItemDto createItemDto, Long userId) {
        Item item = ItemMapper.toItem(createItemDto);
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        Long itemRequestId = createItemDto.getRequestId();
        if (itemRequestId != null) {
            item.setRequest(itemRequestRepo.findById(itemRequestId)
                    .orElseThrow(() -> new NotFoundException(String.format("ItemRequest ID = %d not found!", itemRequestId))));
        }
        return ItemMapper.toItemDtoWithBooking(itemRepository.save(item), null, null, Collections.EMPTY_LIST);
    }

    @Override
    @Transactional
    public ItemDtoWithBooking patchUpdate(ItemDtoWithBooking itemDtoWithBooking, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User ID and owner ID mismatch");
        }
        if (itemDtoWithBooking.getId() != null && !itemDtoWithBooking.getId().equals(itemId)) {
            throw new ValidationException("Item ID mismatch");
        }
        if (itemDtoWithBooking.getName() != null) {
            item.setName(itemDtoWithBooking.getName());
        }
        if (itemDtoWithBooking.getDescription() != null) {
            item.setDescription(itemDtoWithBooking.getDescription());
        }
        if (itemDtoWithBooking.getAvailable() != null) {
            item.setAvailable(itemDtoWithBooking.getAvailable());
        }
        return ItemMapper.toItemDtoWithBooking(itemRepository.save(item), null, null, Collections.EMPTY_LIST);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoWithBooking getById(Long itemId, Long userId) {
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
        return ItemMapper.toItemDtoWithBooking(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoWithBooking> getAllOwnerItems(Long userId, Integer fromElement, Integer size) {
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        int fromPage = fromElement / size;
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();

        return itemRepository.findAllByOwnerId(userId, PageRequest.of(fromPage, size)).stream()
                .map(item -> ItemMapper.toItemDtoWithBooking(item,
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
    public List<ItemDtoWithBooking> findAvailableByText(String text, Integer fromElement, Integer size) {
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        int fromPage = fromElement / size;
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByStatus(BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();
        return itemRepository.searchAvailByText(text, PageRequest.of(fromPage, size)).stream()
                .map(item -> ItemMapper.toItemDtoWithBooking(item,
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
