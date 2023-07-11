package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking addNew(BookingDto bookingDto, Long userId) {
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() ->
                        new NotFoundException(String.format("Item ID = %d not found!", bookingDto.getItemId())));
        if (!item.isAvailable()) {
            throw new ValidationException(String.format("Item ID = %d is not available for booking!", item.getId()));
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Invalid booking datetime!");
        }
        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Cant book own item!");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item);
        booking.setBooker(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", userId))));
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking confirmBooking(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking ID = %d not found!", bookingId)));
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Owner ID and confirmer ID mismatch!");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking state is already confirmed");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking ID = %d not found!", bookingId)));
        if (!Objects.equals(booking.getBooker().getId(), userId) &&
                !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Requester ID and creator(item owner) ID mismatch!");
        }
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllByBookerIdAndState(Long bookerId, String state, Integer fromElement, Integer size) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", bookerId));
        }
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable).toList();
            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        bookerId, LocalDateTime.now(), pageable).toList();
            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId, LocalDateTime.now(), pageable).toList();
            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, LocalDateTime.now(), LocalDateTime.now(), pageable).toList();
            case "WAITING":
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.WAITING, pageable).toList();
            case "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.REJECTED, pageable).toList();
            default:
                throw new ValidationException(String.format("Unknown state: %s", state.toUpperCase()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllByOwnerIdAndState(Long ownerId, String state, Integer fromElement, Integer size) {
        if (Arrays.stream(BookingState.values())
                .map(BookingState::name)
                .map(String::toUpperCase)
                .noneMatch(state.toUpperCase()::equals)) {
            throw new ValidationException(String.format("Unknown state: %s", state.toUpperCase()));
        }
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", ownerId));
        }
        if (fromElement % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        switch (BookingState.valueOf(state.toUpperCase())) {
            case ALL:
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId, pageable).toList();
            case PAST:
                return bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        ownerId, LocalDateTime.now(), pageable).toList();
            case FUTURE:
                return bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                        ownerId, LocalDateTime.now(), pageable).toList();
            case CURRENT:
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, LocalDateTime.now(), LocalDateTime.now(), pageable).toList();
            case WAITING:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.WAITING, pageable).toList();
            case REJECTED:
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.REJECTED, pageable).toList();
            default:
                throw new RuntimeException();
        }
    }
}
