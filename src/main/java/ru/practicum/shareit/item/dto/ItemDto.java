package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.validator.NullableNotBlank;

@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NullableNotBlank(message = "Name field cannot be blank")
    private String name;
    @NullableNotBlank(message = "Description field cannot be blank")
    private String description;
    private Boolean available;
}
