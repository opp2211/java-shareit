package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    private final UserMapper userMapper = new UserMapperImpl();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User 2 name")
                .email("user2@email.com")
                .build();
    }

    @SneakyThrows
    @Test
    void addNew_whenIsValid() {
        Mockito.when(userService.addNew(Mockito.any()))
                .thenReturn(userMapper.toUserResponseDto(user1));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userMapper.toUserRequestDto(user1)))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .addNew(Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getAll() {
        Mockito.when(userService.getAll())
                .thenReturn(Stream.of(user1, user2)
                        .map(userMapper::toUserResponseDto)
                        .collect(Collectors.toList()));
        mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user1.getName())))
                .andExpect(jsonPath("$[0].email", is(user1.getEmail())))
                .andExpect(jsonPath("$[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user2.getName())))
                .andExpect(jsonPath("$[1].email", is(user2.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .getAll();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getById() {
        Long userId = user1.getId();
        Mockito.when(userService.getById(userId))
                .thenReturn(userMapper.toUserResponseDto(user1));
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .getById(userId);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenValid_thenOk() {
        Mockito.when(userService.patchUpdate(Mockito.any(), Mockito.any()))
                .thenReturn(userMapper.toUserResponseDto(user1));
        mockMvc.perform(patch("/users/{id}", user1.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userMapper.toUserRequestDto(user1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .patchUpdate(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void remove() {
        mockMvc.perform(delete("/users/{id}", user1.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());
        Mockito.verify(userService, Mockito.times(1))
                .remove(Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }
}