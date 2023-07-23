package ru.practicum.shareit.user.dto;

import lombok.Data;
import ru.practicum.shareit.validator.NullableNotBlank;
import ru.practicum.shareit.validator.validationGroups.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserRequestDto {
    @NotNull(groups = OnCreate.class,
            message = "Name cannot be null")
    @NullableNotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(groups = OnCreate.class,
            message = "Email cannot be null")
    @NullableNotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email",
            regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                    "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    private String email;
}
