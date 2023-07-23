package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ExtendedItemResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void testGetAllOwnerItems() {
        //given
        UserResponseDto userDto1 = userService.addNew(UserRequestDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        UserResponseDto userDto2 = userService.addNew(UserRequestDto.builder()
                .name("User 2 name")
                .email("user2@email.com")
                .build());
        ItemResponseDto itemDto1 = itemService.addNew(
                ItemRequestDto.builder()
                        .name("Item 1 name")
                        .description("Item 1 description")
                        .available(true)
                        .build(),
                userDto1.getId());
        ItemResponseDto itemDto2 = itemService.addNew(
                ItemRequestDto.builder()
                        .name("Item 2 name")
                        .description("Item 2 description")
                        .available(false)
                        .build(),
                userDto1.getId());
        ItemResponseDto itemDto3 = itemService.addNew(
                ItemRequestDto.builder()
                        .name("Item 3 name")
                        .description("Item 3 description")
                        .available(true)
                        .build(),
                userDto2.getId());
        //when
        Long userId = userDto1.getId();
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        List<ExtendedItemResponseDto> actualItems = itemService.getAllOwnerItems(userId, defaultFromElement, defaultSize);
        //then
        assertThat(actualItems.size(), equalTo(2));
        assertThat(actualItems.get(0).getId(), equalTo(itemDto1.getId()));
        assertThat(actualItems.get(0).getName(), equalTo("Item 1 name"));
        assertThat(actualItems.get(0).getAvailable(), equalTo(true));
        assertThat(actualItems.get(1).getId(), equalTo(itemDto2.getId()));
        assertThat(actualItems.get(1).getName(), equalTo("Item 2 name"));
        assertThat(actualItems.get(1).getAvailable(), equalTo(false));
    }
}
