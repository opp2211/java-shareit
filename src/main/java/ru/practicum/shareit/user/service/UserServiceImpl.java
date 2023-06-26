package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addNew(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new ValidationException("Email field cannot be null!");
        }
        User user = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", id))));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto patchUpdate(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", id)));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void remove(Long id) {
        userRepository.deleteById(id);
    }
}