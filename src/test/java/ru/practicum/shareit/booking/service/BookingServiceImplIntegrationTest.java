package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplIntegrationTest {
    private final EntityManager entityManager;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void testAddNew() {
        //given
        UserDto userDto1 = userService.addNew(UserDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        UserDto userDto2 = userService.addNew(UserDto.builder()
                .name("User 2 name")
                .email("user2@email.com")
                .build());
        ItemDtoWithBooking itemDto = itemService.addNew(
                CreateItemDto.builder()
                        .name("Item 1 name")
                        .description("Item 1 description")
                        .available(true)
                        .build(),
                userDto1.getId());
        //when
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemDto.getId())
                .start(start)
                .end(end)
                .build();
        Long bookerId = userDto2.getId();
        bookingService.addNew(bookingDto, bookerId);
        //then
        TypedQuery<Booking> query = entityManager.createQuery("SELECT b from Booking b ", Booking.class);
        List<Booking> actualBookings = query.getResultList();

        assertThat(actualBookings.size(), equalTo(1));
        assertThat(actualBookings.get(0).getId(), notNullValue());
        assertThat(actualBookings.get(0).getStart(), equalTo(start));
        assertThat(actualBookings.get(0).getEnd(), equalTo(end));
        assertThat(actualBookings.get(0).getItem().getId(), equalTo(itemDto.getId()));
        assertThat(actualBookings.get(0).getItem().getName(), equalTo("Item 1 name"));
        assertThat(actualBookings.get(0).getBooker().getId(), equalTo(bookerId));
        assertThat(actualBookings.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }
}
