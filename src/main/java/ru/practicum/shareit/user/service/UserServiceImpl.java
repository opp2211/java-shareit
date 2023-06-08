package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto addNew(UserDto userDto) {
        if (!userStorage.isFreeEmail(userDto.getEmail())) {
            throw new AlreadyExistException("Email is already taken!");
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("Email field cannot be null!");
        }
        User user = userStorage.addNew(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userStorage.getById(id));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto patchUpdate(Long id, UserDto userDto) {
        User user = userStorage.getById(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (!user.getEmail().equals(userDto.getEmail()) && !userStorage.isFreeEmail(userDto.getEmail())) {
                throw new AlreadyExistException("Email is already taken");
            }
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.update(user));
    }

    @Override
    public void remove(Long id) {
        userStorage.remove(id);
    }
}