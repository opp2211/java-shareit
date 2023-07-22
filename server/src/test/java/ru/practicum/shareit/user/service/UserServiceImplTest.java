package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private final UserMapper userMapper = new UserMapperImpl();
    @Mock
    private UserRepository userRepository;
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void beforeEach() {
        userService = new UserServiceImpl(userRepository, userMapper);
        user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
    }

    @Test
    void testAddNew() {
        //given
        Mockito
                .when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user1);
        //when
        UserResponseDto actualUserDto = userService.addNew(userMapper.toUserRequestDto(user1));
        //then
        assertThat(actualUserDto.getId(), equalTo(user1.getId()));
        assertThat(actualUserDto.getName(), equalTo(user1.getName()));
        assertThat(actualUserDto.getEmail(), equalTo(user1.getEmail()));
        Mockito.verify(userRepository, Mockito.times(1))
                .save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testGetById() {
        //given
        Long userId = user1.getId();
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.of(user1));
        //when
        UserResponseDto actualUserDto = userService.getById(userId);
        //then
        assertThat(actualUserDto.getId(), equalTo(userId));
        assertThat(actualUserDto.getName(), equalTo(user1.getName()));
        assertThat(actualUserDto.getEmail(), equalTo(user1.getEmail()));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(userId);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testGetByIdWrongUserId() {
        //given
        Long userId = 99L;
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        //when
        //then
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> userService.getById(userId));
        assertThat(e.getMessage(), equalTo(String.format("User ID = %d not found!", userId)));
    }

    @Test
    void testGetAll() {
        //given
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        Mockito
                .when(userRepository.findAll())
                .thenReturn(users);
        //when
        List<UserResponseDto> actualUsers = userService.getAll();
        //then
        assertThat(actualUsers.size(), equalTo(users.size()));
        assertThat(actualUsers.get(0).getId(), equalTo(user1.getId()));
        assertThat(actualUsers.get(1).getId(), equalTo(user2.getId()));
        Mockito.verify(userRepository, Mockito.times(1))
                .findAll();
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testPatchUpdate() {
        //given
        Long user1Id = user1.getId();
        String newUser1Name = "New user1 name";
        UserRequestDto userDto = UserRequestDto.builder()
                .name(newUser1Name)
                .email(null)
                .build();
        Mockito
                .when(userRepository.findById(user1Id))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(userRepository.save(user1))
                .thenReturn(user1);
        //when
        UserResponseDto actualUserDto = userService.patchUpdate(user1Id, userDto);
        //then
        assertThat(actualUserDto.getId(), equalTo(user1Id));
        assertThat(actualUserDto.getName(), equalTo(newUser1Name));
        assertThat(actualUserDto.getEmail(), equalTo(user1.getEmail()));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(user1Id);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(user1);
        Mockito.verifyNoMoreInteractions(userRepository);
    }
    @Test
    void testRemove() {
        userService.remove(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(Mockito.any());
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}
