package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    User user1;
    User user2;
    Item item1;
    Item item2;
    Booking booking1;
    Booking booking2;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item1")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("Item2")
                .description("Item 2 description")
                .available(true)
                .owner(user1)
                .build();
        booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(item1)
                .booker(user2)
                .status(BookingStatus.WAITING)
                .build();
        booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(2).plusHours(1))
                .end(LocalDateTime.now().plusDays(2).plusHours(7))
                .item(item2)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @SneakyThrows
    @Test
    void addNew_whenValid_thenOk() {
        Long userId = user2.getId();
        BookingDto bookingDto = BookingMapper.toBookingDto(booking1);
        Mockito
                .when(bookingService.addNew(Mockito.any(), Mockito.anyLong()))
                .thenReturn(booking1);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())));
        Mockito.verify(bookingService, Mockito.only())
                .addNew(Mockito.any(), Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void addNew_whenInvalid_thenBadRequest() {
        Long userId = user2.getId();
        BookingDto bookingDto = BookingMapper.toBookingDto(booking1);
        bookingDto.setEnd(null);
        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
        Mockito.verify(bookingService, Mockito.never())
                .addNew(Mockito.any(), Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void confirmBooking() {
        booking1.setStatus(BookingStatus.APPROVED);
        Long bookingId = booking1.getId();
        boolean approved = true;
        Long userId = user1.getId();
        Mockito
                .when(bookingService.confirmBooking(bookingId, approved, userId))
                .thenReturn(booking1);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .param("approved", Boolean.toString(approved))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())));
        Mockito.verify(bookingService, Mockito.only())
                .confirmBooking(bookingId, approved, userId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBooking() {
        Long bookingId = booking1.getId();
        Long userId = user1.getId();
        Mockito
                .when(bookingService.getBooking(bookingId, userId))
                .thenReturn(booking1);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().toString())));
        Mockito.verify(bookingService, Mockito.only())
                .getBooking(bookingId, userId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getAllByBookerIdAndState() {
        Long userId = user2.getId();
        String defaultState = "ALL";
        Integer defaultFrom = 0;
        Integer defaultSize = 20;
        Mockito
                .when(bookingService.getAllByBookerIdAndState(userId, defaultState, defaultFrom, defaultSize))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", notNullValue()))
                .andExpect(jsonPath("$[0].end", notNullValue()))
                .andExpect(jsonPath("$[0].item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].start", notNullValue()))
                .andExpect(jsonPath("$[1].end", notNullValue()))
                .andExpect(jsonPath("$[1].item.id", is(booking2.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().toString())));
        Mockito.verify(bookingService, Mockito.only())
                .getAllByBookerIdAndState(userId, defaultState, defaultFrom, defaultSize);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getAllByOwnerIdAndState() {
        Long userId = user1.getId();
        String defaultState = "ALL";
        Integer defaultFrom = 0;
        Integer defaultSize = 20;
        Mockito
                .when(bookingService.getAllByOwnerIdAndState(userId, defaultState, defaultFrom, defaultSize))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", notNullValue()))
                .andExpect(jsonPath("$[0].end", notNullValue()))
                .andExpect(jsonPath("$[0].item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(booking1.getStatus().toString())))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].start", notNullValue()))
                .andExpect(jsonPath("$[1].end", notNullValue()))
                .andExpect(jsonPath("$[1].item.id", is(booking2.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().toString())));
        Mockito.verify(bookingService, Mockito.only())
                .getAllByOwnerIdAndState(userId, defaultState, defaultFrom, defaultSize);
        Mockito.verifyNoMoreInteractions(bookingService);
    }
}