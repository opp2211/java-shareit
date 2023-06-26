package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepo;
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
public class BookingServiceImpl implements BookingService {
    private final BookingRepo bookingRepo;
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
        return bookingRepo.save(booking);
    }

    @Override
    public Booking confirmBooking(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingRepo.findById(bookingId)
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
        return bookingRepo.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking ID = %d not found!", bookingId)));
        if (!Objects.equals(booking.getBooker().getId(), userId) &&
                !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Requester ID and creator(item owner) ID mismatch!");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllByBookerIdAndState(Long bookerId, String state) {
        if (Arrays.stream(BookingState.values())
                .map(BookingState::name)
                .map(String::toUpperCase)
                .noneMatch(state.toUpperCase()::equals)) {
            throw new ValidationException(String.format("Unknown state: %s", state.toUpperCase()));
        }
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", bookerId));
        }
        switch (BookingState.valueOf(state)) {
            case ALL:
                return bookingRepo.findAllByBookerIdOrderByStartDesc(bookerId);
            case PAST:
                return bookingRepo.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
            case FUTURE:
                return bookingRepo.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
            case CURRENT:
                return bookingRepo.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId,
                        LocalDateTime.now(), LocalDateTime.now());
            case WAITING:
                return bookingRepo.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepo.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public List<Booking> getAllByOwnerIdAndState(Long ownerId, String state) {
        if (Arrays.stream(BookingState.values())
                .map(BookingState::name)
                .map(String::toUpperCase)
                .noneMatch(state.toUpperCase()::equals)) {
            throw new ValidationException(String.format("Unknown state: %s", state.toUpperCase()));
        }
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID = %d not found!", ownerId));
        }
        switch (BookingState.valueOf(state)) {
            case ALL:
                return bookingRepo.findAllByItemOwnerIdOrderByStartDesc(ownerId);
            case PAST:
                return bookingRepo.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
            case FUTURE:
                return bookingRepo.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now());
            case CURRENT:
                return bookingRepo.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId,
                        LocalDateTime.now(), LocalDateTime.now());
            case WAITING:
                return bookingRepo.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepo.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
            default:
                throw new RuntimeException();
        }
    }
}
