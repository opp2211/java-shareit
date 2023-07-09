package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    User user1;
    Item item1;
    Item item2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item1")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("Item2")
                .description("Item 2 description")
                .available(false)
                .owner(user1)
                .build();
    }

    @SneakyThrows
    @Test
    void addNew_whenValid_thenOk() {
        Mockito.when(itemService.addNew(Mockito.any(), Mockito.any()))
                .thenReturn(ItemMapper.toItemDtoWithBooking(
                        item1, null, null, Collections.EMPTY_LIST));

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(ItemMapper.toCreateItemDto(item1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.isAvailable())));
        Mockito.verify(itemService, Mockito.only())
                .addNew(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void addNew_whenIsNotValid_thenBadRequest() {
        item1.setName("a".repeat(256));

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(ItemMapper.toCreateItemDto(item1))))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService, Mockito.never())
                .addNew(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenValid_thenOk() {
        ItemDtoWithBooking itemDto = ItemMapper.toItemDtoWithBooking(
                item1, null, null, Collections.EMPTY_LIST);
        Mockito.when(itemService.patchUpdate(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(itemDto);
        mockMvc.perform(patch("/items/{itemId}", item1.getId())
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.isAvailable())));
        Mockito.verify(itemService, Mockito.only())
                .patchUpdate(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenIsNotValid_thenBadRequest() {
        item1.setName("");
        ItemDtoWithBooking itemDto = ItemMapper.toItemDtoWithBooking(
                item1, null, null, Collections.EMPTY_LIST);
        mockMvc.perform(patch("/items/{itemId}", item1.getId())
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService, Mockito.never())
                .patchUpdate(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getById() {
        Long itemId = item1.getId();
        Long userId = item1.getOwner().getId();
        ItemDtoWithBooking itemDto = ItemMapper.toItemDtoWithBooking(
                item1, null, null, Collections.EMPTY_LIST);
        Mockito
                .when(itemService.getById(itemId, userId))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item1.getName())))
                .andExpect(jsonPath("$.description", is(item1.getDescription())))
                .andExpect(jsonPath("$.available", is(item1.isAvailable())));
        Mockito.verify(itemService, Mockito.only())
                .getById(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getAllOwnerItems() {
        Long userId = 0L;
        Mockito
                .when(itemService.getAllOwnerItems(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Stream.of(item1, item2)
                        .map(ItemMapper::toItemDtoWithBooking)
                        .collect(Collectors.toList()));
        mockMvc.perform(get("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.isAvailable())))
                .andExpect(jsonPath("$[1].id", is(item2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(item2.getName())))
                .andExpect(jsonPath("$[1].description", is(item2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item2.isAvailable())));
        Mockito.verify(itemService, Mockito.only())
                .getAllOwnerItems(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void findAvailableByText() {
        Mockito
                .when(itemService.findAvailableByText(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Stream.of(item1, item2)
                        .map(ItemMapper::toItemDtoWithBooking)
                        .collect(Collectors.toList()));
        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(item1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item1.getName())))
                .andExpect(jsonPath("$[0].description", is(item1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item1.isAvailable())))
                .andExpect(jsonPath("$[1].id", is(item2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(item2.getName())))
                .andExpect(jsonPath("$[1].description", is(item2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(item2.isAvailable())));
        Mockito.verify(itemService, Mockito.only())
                .findAvailableByText(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void addNewComment_whenValid_thenOk() {
        Long itemId = 0L;
        Long userId = 0L;
        Comment comment = Comment.builder()
                .text("Comment text")
                .build();
        String authorName = "Name";
        LocalDateTime created = LocalDateTime.now();
        Mockito
                .when(itemService.addNewComment(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CommentDto.builder()
                        .id(0L)
                        .text(comment.getText())
                        .authorName(authorName)
                        .created(created)
                        .build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(authorName)))
                .andExpect(jsonPath("$.created", notNullValue()));
        Mockito.verify(itemService, Mockito.only())
                .addNewComment(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void addNewComment_whenIsNotValid_thenBadRequest() {
        Long itemId = 0L;
        Long userId = 0L;
        Comment comment = Comment.builder()
                .text(" ")
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService, Mockito.never())
                .addNewComment(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }
}