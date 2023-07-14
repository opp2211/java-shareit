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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        userDto1 = UserDto.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .name("User 2 name")
                .email("user2@email.com")
                .build();
    }

    @SneakyThrows
    @Test
    void addNew_whenIsValid() {
        userDto1.setId(null);
        Long newId = 1L;
        Mockito.when(userService.addNew(Mockito.any(UserDto.class)))
                .thenAnswer(invocationOnMock -> {
                    UserDto userDto = invocationOnMock.getArgument(0, UserDto.class);
                    userDto.setId(newId);
                    return userDto;
                });
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto1))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newId), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .addNew(Mockito.any(UserDto.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void addNew_whenIsNotValid_thenStatusBadRequest() {
        userDto1.setId(null);
        userDto1.setName("");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto1))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
        Mockito.verify(userService, Mockito.never())
                .addNew(Mockito.any(UserDto.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getAll() {
        Mockito.when(userService.getAll())
                .thenReturn(List.of(userDto1, userDto2));
        mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto1.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto1.getEmail())))
                .andExpect(jsonPath("$[1].id", is(userDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(userDto2.getName())))
                .andExpect(jsonPath("$[1].email", is(userDto2.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .getAll();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getById() {
        Long userId = userDto1.getId();
        Mockito.when(userService.getById(userId))
                .thenReturn(userDto1);
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .getById(userId);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenValid_thenOk() {
        Mockito.when(userService.patchUpdate(Mockito.any(), Mockito.any()))
                .thenReturn(userDto1);
        mockMvc.perform(patch("/users/{id}", userDto1.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));
        Mockito.verify(userService, Mockito.times(1))
                .patchUpdate(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenIsNotValid_thenBadRequest() {
        userDto1.setEmail("email.ru@mail");
        mockMvc.perform(patch("/users/{id}", userDto1.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isBadRequest());
        Mockito.verify(userService, Mockito.never())
                .patchUpdate(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void remove() {
        mockMvc.perform(delete("/users/{id}", userDto1.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());
        Mockito.verify(userService, Mockito.times(1))
                .remove(Mockito.any());
        Mockito.verifyNoMoreInteractions(userService);
    }
}