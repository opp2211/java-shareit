package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepo;
    @Mock
    private ItemRepository itemRepo;
    @Mock
    private UserRepository userRepo;

    private User user1;
    private User user2;
    private User user3;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void beforeEach() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepo, userRepo, itemRepo);
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
        user3 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("ItemRequest 1 description")
                .requester(user1)
                .build();
        itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("ItemRequest 2 description")
                .requester(user1)
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item 1 name")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .request(itemRequest1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("Item 2 name")
                .description("Item 2 description")
                .available(true)
                .owner(user2)
                .request(itemRequest1)
                .build();

    }

    @Test
    void testGetAllOwn() {
        //given
        Long requesterId = user1.getId();
        Mockito
                .when(userRepo.existsById(requesterId))
                .thenReturn(true);
        Mockito
                .when((itemRepo.findAllByRequestIdNotNull()))
                .thenReturn(List.of(item1, item2));
        Mockito
                .when(itemRequestRepo.findAllByRequesterId(requesterId))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        //when
        List<ItemRequestWithItemsDto> actualItemRequestWithItems =
                itemRequestService.getAllOwn(requesterId);
        //then
        assertThat(actualItemRequestWithItems.size(), equalTo(2));
        assertThat(actualItemRequestWithItems.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(actualItemRequestWithItems.get(0).getItems().size(), equalTo(2));
        assertThat(actualItemRequestWithItems.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(actualItemRequestWithItems.get(0).getItems().get(1).getId(), equalTo(item2.getId()));
        assertThat(actualItemRequestWithItems.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(actualItemRequestWithItems.get(1).getItems().size(), equalTo(0));
        Mockito.verify(userRepo, Mockito.times(1))
                .existsById(requesterId);
        Mockito.verify(itemRepo, Mockito.times(1))
                .findAllByRequestIdNotNull();
        Mockito.verify(itemRequestRepo, Mockito.times(1))
                .findAllByRequesterId(requesterId);
        Mockito.verifyNoMoreInteractions(userRepo, itemRepo, itemRequestRepo);
    }

    @Test
    void testGetAllOwnWithNoRequests() {
        //given
        Long requesterId = user3.getId();
        Mockito
                .when(userRepo.existsById(requesterId))
                .thenReturn(true);
        Mockito
                .when((itemRepo.findAllByRequestIdNotNull()))
                .thenReturn(List.of(item1, item2));
        Mockito
                .when(itemRequestRepo.findAllByRequesterId(requesterId))
                .thenReturn(Collections.emptyList());
        //when
        List<ItemRequestWithItemsDto> actualItemRequestWithItems =
                itemRequestService.getAllOwn(requesterId);
        //then
        assertThat(actualItemRequestWithItems.size(), equalTo(0));
        Mockito.verify(userRepo, Mockito.times(1))
                .existsById(requesterId);
        Mockito.verify(itemRepo, Mockito.times(1))
                .findAllByRequestIdNotNull();
        Mockito.verify(itemRequestRepo, Mockito.times(1))
                .findAllByRequesterId(requesterId);
        Mockito.verifyNoMoreInteractions(userRepo, itemRepo, itemRequestRepo);
    }

    @Test
    void testGetAllOwnWrongUserId() {
        //given
        Long requesterId = 99L;
        Mockito
                .when(userRepo.existsById(requesterId))
                .thenReturn(false);
        //when
        //then
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllOwn(requesterId));
        assertThat(e.getMessage(), equalTo(String.format("User ID = %d not found!", requesterId)));
    }

    @Test
    void testGetAllByPages() {
        //given
        Long userId = user2.getId();
        Integer fromElement = 0;
        Integer size = 20;
        Mockito.when(itemRepo.findAllByRequestIdNotNull())
                .thenReturn(List.of(item1, item2));
        Mockito.when(itemRequestRepo.findAllByRequesterIdNot(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(itemRequest1, itemRequest2)));
        //when
        List<ItemRequestWithItemsDto> actualItemRequests = itemRequestService.getAllByPages(userId, fromElement, size);
        //then
        assertThat(actualItemRequests.size(), equalTo(2));
        assertThat(actualItemRequests.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(actualItemRequests.get(0).getItems().size(), equalTo(2));
        assertThat(actualItemRequests.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(actualItemRequests.get(0).getItems().get(1).getId(), equalTo(item2.getId()));
        assertThat(actualItemRequests.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(actualItemRequests.get(1).getItems().size(), equalTo(0));
        Mockito.verify(itemRepo, Mockito.times(1))
                .findAllByRequestIdNotNull();
        Mockito.verify(itemRequestRepo, Mockito.times(1))
                .findAllByRequesterIdNot(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepo, itemRepo, itemRequestRepo);
    }
}
