package ru.practicum.shareit.user.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    public abstract User toUser(UserRequestDto userRequestDto);

    public abstract UserResponseDto toUserResponseDto(User user);

    public abstract UserRequestDto toUserRequestDto(User user);
}
