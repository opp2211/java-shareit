package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private User user2;
    private ItemRequest itemRequest1;
    private Item item1;
    private Item item2;
    private Booking booking1;
    private Comment comment1;

    @BeforeEach
    void beforeEach() {
        itemService = new ItemServiceImpl(
                itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
        User user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("ItemRequest description")
                .requester(user1)
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item 1 name")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("Item 2 name")
                .description("Item 2 description")
                .available(true)
                .owner(user2)
                .request(itemRequest1)
                .build();
        booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusHours(24))
                .end(LocalDateTime.now().minusHours(20))
                .item(item1)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
        comment1 = Comment.builder()
                .id(1L)
                .text("text text text")
                .item(item1)
                .author(user2)
                .build();
    }

    @Test
    void testAddNew() {
        //given
        Long newItemId = 5L;
        ItemRequestDto createItemDto = ItemMapper.toItemRequestDto(item1);
        Long userId = item1.getOwner().getId();
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.of(item1.getOwner()));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0, Item.class);
                    item.setId(newItemId);
                    return item;
                });
        //when
        ItemResponseDto actualItemDto = itemService.addNew(createItemDto, userId);
        //then
        assertThat(actualItemDto.getId(), equalTo(newItemId));
        assertThat(actualItemDto.getName(), equalTo(item1.getName()));
        assertThat(actualItemDto.getRequestId(), equalTo(null));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(userId);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testAddNewToItemRequest() {
        //given
        Long newItemId = 5L;
        ItemRequestDto createItemDto = ItemMapper.toItemRequestDto(item2);
        Long userId = item2.getOwner().getId();
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.of(item2.getOwner()));
        Mockito
                .when(itemRequestRepository.findById(createItemDto.getRequestId()))
                .thenReturn(Optional.of(itemRequest1));
        Mockito
                .when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0, Item.class);
                    item.setId(newItemId);
                    return item;
                });
        //when
        ItemResponseDto actualItemDto = itemService.addNew(createItemDto, userId);
        //then
        assertThat(actualItemDto.getId(), equalTo(newItemId));
        assertThat(actualItemDto.getName(), equalTo(item2.getName()));
        assertThat(actualItemDto.getRequestId(), equalTo(item2.getRequest().getId()));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(userId);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(createItemDto.getRequestId());
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testPatchUpdate() {
        //given
        String newUserName = "New user name";
        String expectedDesc = item1.getDescription();
        Long itemId = item1.getId();
        Long userId = item1.getOwner().getId();
        ItemRequestDto itemDto = ItemRequestDto.builder()
                .name(newUserName)
                .description(null)
                .available(false)
                .build();
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(itemRepository.save(item1))
                .thenReturn(item1);
        //when
        ItemResponseDto actualItemDto = itemService.patchUpdate(itemDto, itemId, userId);
        //then
        assertThat(actualItemDto.getId(), equalTo(itemId));
        assertThat(actualItemDto.getName(), equalTo(newUserName));
        assertThat(actualItemDto.getDescription(), equalTo(expectedDesc));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(itemId);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(item1);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testPatchUpdateNoOwner() {
        //given
        String newUserName = "New user name";
        Long itemId = item1.getId();
        Long userId = user2.getId();
        ItemRequestDto itemDto = ItemRequestDto.builder()
                .name(newUserName)
                .description(null)
                .available(false)
                .build();
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        //when
        //then
        AccessDeniedException e = Assertions.assertThrows(AccessDeniedException.class,
                () -> itemService.patchUpdate(itemDto, itemId, userId));
        assertThat(e.getMessage(), equalTo("User ID and owner ID mismatch"));
    }

    @Test
    void testPatchUpdateWrongItemId() {
        //given
        String newUserName = "New user name";
        Long itemId = 99L;
        Long userId = item1.getOwner().getId();
        ItemRequestDto itemDto = ItemRequestDto.builder()
                .name(newUserName)
                .description(null)
                .available(false)
                .build();
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());
        //when
        //then
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.patchUpdate(itemDto, itemId, userId));
        assertThat(e.getMessage(), equalTo(String.format("Item ID = %d not found!", itemId)));
    }

    @Test
    void testGetByIdWithoutCommentsAndWithBookingGettingByNoOwner() {
        //given
        Long itemId = item1.getId();
        Long userId = user2.getId();
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        Mockito.when(commentRepository.findAllByItemId(itemId))
                .thenReturn(Collections.emptyList());
        //when
        ExtendedItemResponseDto actualItemDto = itemService.getById(itemId, userId);
        //then
        assertThat(actualItemDto.getId(), equalTo(itemId));
        assertThat(actualItemDto.getName(), equalTo(item1.getName()));
        assertThat(actualItemDto.getLastBooking(), equalTo(null));
        assertThat(actualItemDto.getNextBooking(), equalTo(null));
        assertThat(actualItemDto.getComments().size(), equalTo(0));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(itemId);
        Mockito.verify(commentRepository, Mockito.times(1))
                .findAllByItemId(itemId);
        Mockito.verifyNoMoreInteractions(itemRepository, commentRepository);
    }

    @Test
    void testGetByIdWithCommentsAndWithBookingGettingByOwner() {
        //given
        Long itemId = item1.getId();
        Long userId = item1.getOwner().getId();
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(booking1);
        Mockito.when(commentRepository.findAllByItemId(itemId))
                .thenReturn(Collections.singletonList(comment1));
        //when
        ExtendedItemResponseDto actualItemDto = itemService.getById(itemId, userId);
        //then
        assertThat(actualItemDto.getId(), equalTo(itemId));
        assertThat(actualItemDto.getName(), equalTo(item1.getName()));
        assertThat(actualItemDto.getLastBooking().getId(), equalTo(booking1.getId()));
        assertThat(actualItemDto.getNextBooking(), equalTo(null));
        assertThat(actualItemDto.getComments().size(), equalTo(1));
        assertThat(actualItemDto.getComments().get(0).getId(), equalTo(comment1.getId()));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(itemId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        Mockito.anyLong(), Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findAllByItemId(itemId);
        Mockito.verifyNoMoreInteractions(itemRepository, bookingRepository, commentRepository);
    }

    @Test
    void testFindAvailableByText() {
        //given
        String text = "Item";
        Integer fromElement = 0;
        Integer size = 20;
        Page<Item> page = new PageImpl<>(List.of(item1, item2));
        Mockito.when(bookingRepository.findAllByStatus(BookingStatus.APPROVED))
                .thenReturn(Collections.singletonList(booking1));
        Mockito.when(commentRepository.findAll())
                .thenReturn(Collections.singletonList(comment1));
        Mockito.when(itemRepository.searchAvailByText(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(page);
        //when
        List<ExtendedItemResponseDto> actualItems = itemService.findAvailableByText(text, fromElement, size);
        //then
        assertThat(actualItems.size(), equalTo(2));
        assertThat(actualItems.get(0).getId(), equalTo(item1.getId()));
        assertThat(actualItems.get(0).getComments().size(), equalTo(1));
        assertThat(actualItems.get(0).getLastBooking().getId(), equalTo(booking1.getId()));
        assertThat(actualItems.get(0).getNextBooking(), equalTo(null));
        assertThat(actualItems.get(1).getId(), equalTo(item2.getId()));
        assertThat(actualItems.get(1).getComments().size(), equalTo(0));
        assertThat(actualItems.get(1).getLastBooking(), equalTo(null));
        assertThat(actualItems.get(1).getNextBooking(), equalTo(null));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByStatus(BookingStatus.APPROVED);
        Mockito.verify(commentRepository, Mockito.times(1))
                .findAll();
        Mockito.verify(itemRepository, Mockito.times(1))
                .searchAvailByText(Mockito.anyString(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(bookingRepository, commentRepository, itemRepository);
    }

    @Test
    void testAddNewComment_whenValid() {
        Long itemId = item1.getId();
        Long userId = user2.getId();
        CommentRequestDto requestComment = CommentRequestDto.builder()
                .text("text")
                .build();
        Comment expectedComment = Comment.builder()
                .id(1L)
                .text(requestComment.getText())
                .item(item1)
                .author(user2)
                .created(LocalDateTime.now().minusHours(1))
                .build();
        Mockito
                .when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Mockito
                .when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(commentRepository.save(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    Comment comment = invocationOnMock.getArgument(0, Comment.class);
                    comment.setId(expectedComment.getId());
                    comment.setCreated(expectedComment.getCreated());
                    return comment;
                });

        CommentResponseDto actualComment = itemService.addNewComment(requestComment, userId, itemId);

        assertThat(actualComment.getId(), equalTo(expectedComment.getId()));
        assertThat(actualComment.getText(), equalTo(expectedComment.getText()));
        assertThat(actualComment.getAuthorName(), equalTo(expectedComment.getAuthor().getName()));
        assertThat(actualComment.getCreated(), equalTo(expectedComment.getCreated()));
    }


}
