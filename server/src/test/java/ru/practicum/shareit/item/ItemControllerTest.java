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
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    User user1;
    Item item1;
    Item item2;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    private final ItemMapper itemMapper = new ItemMapperImpl(new CommentMapperImpl());

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
                .thenReturn(itemMapper.toItemResponseDto(item1));

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemMapper.toItemRequestDto(item1))))
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
    void patchUpdate_whenValid_thenOk() {
        ItemResponseDto itemDto = itemMapper.toItemResponseDto(item1);
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
    void patchUpdate_whenIdMismatch_thenForbidden() {
        ItemResponseDto itemDto = itemMapper.toItemResponseDto(item1);
        Mockito.when(itemService.patchUpdate(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new AccessDeniedException(""));
        mockMvc.perform(patch("/items/{itemId}", item1.getId())
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 0)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    void patchUpdate_whenWrongUserId_thenNotFound() {
        ItemResponseDto itemDto = itemMapper.toItemResponseDto(item1);
        Mockito.when(itemService.patchUpdate(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new NotFoundException(""));
        mockMvc.perform(patch("/items/{itemId}", item1.getId())
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", 0)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    void getById() {
        Long itemId = item1.getId();
        Long userId = item1.getOwner().getId();
        ExtendedItemResponseDto itemDto = itemMapper.toExtendedItemResponseDto(item1);
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
                        .map(itemMapper::toExtendedItemResponseDto)
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
                        .map(itemMapper::toExtendedItemResponseDto)
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
                .thenReturn(CommentResponseDto.builder()
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
    void addNewComment_whenNoBooking_thenBadRequest() {
        Long itemId = 0L;
        Long userId = 0L;
        Comment comment = Comment.builder()
                .text("Comment text")
                .build();
        Mockito
                .when(itemService.addNewComment(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new ValidationException(""));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService, Mockito.only())
                .addNewComment(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(itemService);
    }
}