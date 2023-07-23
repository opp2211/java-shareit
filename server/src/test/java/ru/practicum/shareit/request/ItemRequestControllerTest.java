package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private final ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService requestService;
    private User user1;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;


    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("Item request 1 description")
                .requester(user1)
                .created(LocalDateTime.now())
                .build();
        itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("Item request 2 description")
                .requester(user1)
                .created(LocalDateTime.now())
                .build();
    }

    @SneakyThrows
    @Test
    void addNew_whenValid_thenOk() {
        Long userId = user1.getId();
        String desc = itemRequest1.getDescription();
        Mockito
                .when(requestService.addNew(Mockito.any(), Mockito.anyLong()))
                .thenReturn(itemRequest1);
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(
                                ItemRequest.builder()
                                        .description(desc)
                                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$.requester.id", is(itemRequest1.getRequester().getId()), Long.class))
                .andExpect(jsonPath("$.requester.name", is(itemRequest1.getRequester().getName())))
                .andExpect(jsonPath("$.requester.email", is(itemRequest1.getRequester().getEmail())))
                .andExpect(jsonPath("$.created", notNullValue()));
        Mockito.verify(requestService, Mockito.only())
                .addNew(Mockito.any(), Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @SneakyThrows
    @Test
    void getAllOwn() {
        Long userId = user1.getId();
        Mockito
                .when(requestService.getAllOwn(userId))
                .thenReturn(Stream.of(itemRequest1, itemRequest2)
                        .map(itemRequestMapper::toItemRequestWithItemsDto)
                        .collect(Collectors.toList()));
        mockMvc.perform(get("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$[0].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath())
                .andExpect(jsonPath("$[1].id", is(itemRequest2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequest2.getDescription())))
                .andExpect(jsonPath("$[1].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath());
        Mockito.verify(requestService, Mockito.only())
                .getAllOwn(userId);
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @SneakyThrows
    @Test
    void getAllByPages() {
        Long userId = user1.getId();
        Integer defaultFrom = 0;
        Integer defaultSize = 20;
        Mockito
                .when(requestService.getAllByPages(userId, defaultFrom, defaultSize))
                .thenReturn(Stream.of(itemRequest1, itemRequest2)
                        .map(itemRequestMapper::toItemRequestWithItemsDto)
                        .collect(Collectors.toList()));
        mockMvc.perform(get("/requests/all")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", defaultFrom.toString())
                        .param("size", defaultSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$[0].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath())
                .andExpect(jsonPath("$[1].id", is(itemRequest2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequest2.getDescription())))
                .andExpect(jsonPath("$[1].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath());
        Mockito.verify(requestService, Mockito.only())
                .getAllByPages(userId, defaultFrom, defaultSize);
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @SneakyThrows
    @Test
    void getById() {
        Long requestId = itemRequest1.getId();
        Long userId = user1.getId();
        Mockito
                .when(requestService.getById(requestId, userId))
                .thenReturn(itemRequestMapper.toItemRequestWithItemsDto(itemRequest1));
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items").hasJsonPath());
        Mockito.verify(requestService, Mockito.only())
                .getById(requestId, userId);
        Mockito.verifyNoMoreInteractions(requestService);
    }
}