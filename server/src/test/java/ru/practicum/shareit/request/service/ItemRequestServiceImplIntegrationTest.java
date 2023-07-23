package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRequestService itemRequestService;

    @Test
    void testGetById() {
        //given
        UserResponseDto userDto1 = userService.addNew(UserRequestDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        UserResponseDto userDto2 = userService.addNew(UserRequestDto.builder()
                .name("User 2 name")
                .email("user2@email.com")
                .build());
        ItemRequest itemRequest = itemRequestService.addNew(
                ItemRequest.builder()
                        .description("ItemRequest 1 description")
                        .created(LocalDateTime.now())
                        .build(),
                userDto1.getId());
        itemService.addNew(
                ItemRequestDto.builder()
                        .name("Item 1 name")
                        .description("Item 1 description")
                        .available(true)
                        .requestId(itemRequest.getId())
                        .build(),
                userDto2.getId());
        itemService.addNew(
                ItemRequestDto.builder()
                        .name("Item 2 name")
                        .description("Item 2 description")
                        .available(true)
                        .requestId(itemRequest.getId())
                        .build(),
                userDto2.getId());
        //when
        ItemRequestWithItemsDto actualRequest = itemRequestService.getById(itemRequest.getId(),
                itemRequest.getRequester().getId());
        //then
        assertThat(actualRequest.getId(), equalTo(itemRequest.getId()));
        assertThat(actualRequest.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(actualRequest.getCreated(), equalTo(itemRequest.getCreated()));
        assertThat(actualRequest.getItems().size(), equalTo(2));
    }
}
