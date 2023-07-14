package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addNew(UserDto userDto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    UserDto patchUpdate(Long id, UserDto userDto);

    void remove(Long id);
}