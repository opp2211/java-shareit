package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.List;

public interface UserService {
    UserResponseDto addNew(UserRequestDto userRequestDto);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    UserResponseDto patchUpdate(Long id, UserRequestDto userRequestDto);

    void remove(Long id);
}