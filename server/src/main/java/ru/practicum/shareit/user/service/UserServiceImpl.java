package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponseDto addNew(UserRequestDto userRequestDto) {
        User user = userRepository.save(UserMapper.toUser(userRequestDto));
        return UserMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return UserMapper.toUserResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto patchUpdate(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User ID = %d not found!", id)));
        if (userRequestDto.getName() != null) {
            user.setName(userRequestDto.getName());
        }
        if (userRequestDto.getEmail() != null) {
            user.setEmail(userRequestDto.getEmail());
        }
        return UserMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public void remove(Long id) {
        userRepository.deleteById(id);
    }
}