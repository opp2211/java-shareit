package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingNearest;
import ru.practicum.shareit.user.validator.NullableNotBlank;

import java.util.List;

@Data
@Builder
public class ItemDtoWithBooking {
    private Long id;

    @NullableNotBlank(message = "Name field cannot be blank")
    private String name;

    @NullableNotBlank(message = "Description field cannot be blank")
    private String description;

    private Boolean available;

    private Long requestId;

    private BookingNearest lastBooking;

    private BookingNearest nextBooking;

    private List<CommentDto> comments;
}
