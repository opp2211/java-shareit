package ru.practicum.shareit.item.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.shareit.validator.NullableNotBlank;
import ru.practicum.shareit.validator.validationGroups.OnCreate;
import ru.practicum.shareit.validator.validationGroups.OnUpdate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
public class ItemRequestDto {
    @NotNull(groups = OnCreate.class)
    @NullableNotBlank
    @Length(max = 255)
    private String name;

    @NotNull(groups = OnCreate.class)
    @NullableNotBlank
    @Length(max = 2000)
    private String description;

    @NotNull(groups = OnCreate.class)
    private Boolean available;

    @Null(groups = OnUpdate.class)
    private Long requestId;
}
