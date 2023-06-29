package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking addNew(@RequestBody @Valid BookingDto bookingDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.addNew(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public Booking confirmBooking(@PathVariable Long bookingId,
                                  @RequestParam boolean approved,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.confirmBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public Booking getBooking(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<Booking> getAllByBookerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return bookingService.getAllByBookerIdAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<Booking> getAllByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state,
                                                 @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                 @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return bookingService.getAllByOwnerIdAndState(userId, state, from, size);
    }
}
