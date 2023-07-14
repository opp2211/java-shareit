package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtosTest {

    @Autowired
    private JacksonTester<BookingDto> jacksonTester;

    @SneakyThrows
    @Test
    void testBookingDtoToJson() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        JsonContent<BookingDto> content = jacksonTester.write(bookingDto);

        assertThat(content).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(bookingDto.getItemId().intValue());
        assertThat(content).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME));
        assertThat(content).extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @SneakyThrows
    @Test
    void testBookingDtoFromJson() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        String bookingDto1Json = jacksonTester.write(bookingDto).getJson();

        BookingDto actualBookingDto = jacksonTester.parseObject(bookingDto1Json);

        assertThat(actualBookingDto.getItemId()).isEqualTo(bookingDto.getItemId());
        assertThat(actualBookingDto.getStart()).isEqualTo(bookingDto.getStart());
        assertThat(actualBookingDto.getEnd()).isEqualTo(bookingDto.getEnd());
    }
}