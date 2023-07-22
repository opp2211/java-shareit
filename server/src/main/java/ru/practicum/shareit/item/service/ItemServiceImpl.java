package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingNearestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepo;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;


    @Override
    public ItemResponseDto addNew(ItemRequestDto itemRequestDto, Long userId) {
        Item item = itemMapper.toItem(itemRequestDto);
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        Long itemRequestId = itemRequestDto.getRequestId();
        if (itemRequestId != null) {
            item.setRequest(itemRequestRepo.findById(itemRequestId)
                    .orElseThrow(() -> new NotFoundException(String.format("ItemRequest ID = %d not found!", itemRequestId))));
        }
        return itemMapper.toItemResponseDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto patchUpdate(ItemRequestDto itemRequestDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User ID and owner ID mismatch");
        }
        if (itemRequestDto.getName() != null) {
            item.setName(itemRequestDto.getName());
        }
        if (itemRequestDto.getDescription() != null) {
            item.setDescription(itemRequestDto.getDescription());
        }
        if (itemRequestDto.getAvailable() != null) {
            item.setAvailable(itemRequestDto.getAvailable());
        }
        return itemMapper.toItemResponseDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ExtendedItemResponseDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId)));
        BookingNearestDto lastBooking = null;
        BookingNearestDto nextBooking = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            lastBooking = BookingMapper.toBookingNearest(
                    bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                            itemId, BookingStatus.APPROVED, LocalDateTime.now()));
            nextBooking = BookingMapper.toBookingNearest(
                    bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            itemId, BookingStatus.APPROVED, LocalDateTime.now()));
        }
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return itemMapper.toExtendedItemResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedItemResponseDto> getAllOwnerItems(Long userId, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByItemOwnerIdAndStatus(userId, BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();

        return itemRepository.findAllByOwnerId(userId, PageRequest.of(fromPage, size)).stream()
                .map(item -> itemMapper.toExtendedItemResponseDto(item,
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isBefore(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .max(Comparator.comparing(BookingNearestDto::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isAfter(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .min(Comparator.comparing(BookingNearestDto::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredComments.stream()
                                .filter(comment -> Objects.equals(comment.getItem().getId(), item.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtendedItemResponseDto> findAvailableByText(String text, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        List<Booking> unfilteredBookings = bookingRepository
                .findAllByStatus(BookingStatus.APPROVED);
        List<Comment> unfilteredComments = commentRepository.findAll();
        return itemRepository.searchAvailByText(text, PageRequest.of(fromPage, size)).stream()
                .map(item -> itemMapper.toExtendedItemResponseDto(item,
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isBefore(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .max(Comparator.comparing(BookingNearestDto::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredBookings.stream()
                                .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                        booking.getStart().isAfter(LocalDateTime.now()))
                                .map(BookingMapper::toBookingNearest)
                                .min(Comparator.comparing(BookingNearestDto::getStart, LocalDateTime::compareTo))
                                .orElse(null),
                        unfilteredComments.stream()
                                .filter(comment -> Objects.equals(comment.getItem().getId(), item.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto addNewComment(CommentRequestDto commentRequestDto, Long userId, Long itemId) {
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ValidationException("");
        }
        Comment comment = commentMapper.toComment(commentRequestDto);
        comment.setItem(itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID = %d not found!", itemId))));
        comment.setAuthor(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        return commentMapper.toCommentResponseDto(commentRepository.save(comment));
    }
}
