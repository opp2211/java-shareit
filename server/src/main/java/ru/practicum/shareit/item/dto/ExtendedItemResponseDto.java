package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingNearestDto;

import java.util.List;

@Data
@Builder
public class ExtendedItemResponseDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

    private BookingNearestDto lastBooking;

    private BookingNearestDto nextBooking;

    private List<CommentResponseDto> comments;
}
