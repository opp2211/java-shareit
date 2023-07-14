package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingDto {
    @NotNull
    private Long itemId;

    @NotNull
    private LocalDateTime start;

    @JsonFormat()
    @NotNull
    private LocalDateTime end;
}
