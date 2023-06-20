package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.validator.NullableNotBlank;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    @Email(message = "Invalid email",
            regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                    "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    @NullableNotBlank(message = "Email cannot be blank")
    private String email;
}
