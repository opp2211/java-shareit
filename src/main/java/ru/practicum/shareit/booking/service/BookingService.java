package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking addNew(BookingDto bookingDto, Long userId);

    Booking confirmBooking(Long bookingId, boolean approved, Long userId);

    Booking getBooking(Long bookingId, Long userId);

    List<Booking> getAllByBookerIdAndState(Long bookerId, String state, Integer fromElement, Integer size);

    List<Booking> getAllByOwnerIdAndState(Long ownerId, String state, Integer fromElement, Integer size);
}
