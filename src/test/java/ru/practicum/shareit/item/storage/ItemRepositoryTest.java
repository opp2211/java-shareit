package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private ItemRepository itemRepository;
    private User user1;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        testEntityManager.persistAndFlush(user1);
        item1 = Item.builder()
                .name("Item1 name")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .build();
        item2 = Item.builder()
                .name("Item2 name")
                .description("Item 2 description")
                .available(true)
                .owner(user1)
                .build();
        testEntityManager.persist(item1);
        testEntityManager.persist(item2);
        testEntityManager.flush();
    }

    @Test
    void findAllByOwnerId() {
        Long ownerId = user1.getId();
        int defaultFromPage = 0;
        int defaultSize = 20;
        List<Item> items =
                itemRepository.findAllByOwnerId(ownerId, PageRequest.of(defaultFromPage, defaultSize)).toList();

        assertThat(items.size(), is(2));
        assertThat(items.get(0).getId(), notNullValue());
        assertThat(items.get(0).getName(), is(item1.getName()));
        assertThat(items.get(0).getDescription(), is(item1.getDescription()));
        assertThat(items.get(0).isAvailable(), is(item1.isAvailable()));
        assertThat(items.get(0).getOwner().getId(), is(item1.getOwner().getId()));
        assertThat(items.get(0).getOwner().getName(), is(item1.getOwner().getName()));
        assertThat(items.get(0).getOwner().getEmail(), is(item1.getOwner().getEmail()));
        assertThat(items.get(1).getId(), notNullValue());
        assertThat(items.get(1).getName(), is(item2.getName()));
        assertThat(items.get(1).getDescription(), is(item2.getDescription()));
        assertThat(items.get(1).isAvailable(), is(item2.isAvailable()));
        assertThat(items.get(1).getOwner().getId(), is(item2.getOwner().getId()));
        assertThat(items.get(1).getOwner().getName(), is(item2.getOwner().getName()));
        assertThat(items.get(1).getOwner().getEmail(), is(item2.getOwner().getEmail()));
    }

    @Test
    void searchAvailByText() {
        String text = "name";
        int defaultFromPage = 0;
        int defaultSize = 20;
        List<Item> items =
                itemRepository.searchAvailByText(text, PageRequest.of(defaultFromPage, defaultSize)).toList();

        assertThat(items.size(), is(2));
        assertThat(items.get(0).getId(), notNullValue());
        assertThat(items.get(0).getName(), is(item1.getName()));
        assertThat(items.get(0).getDescription(), is(item1.getDescription()));
        assertThat(items.get(0).isAvailable(), is(item1.isAvailable()));
        assertThat(items.get(0).getOwner().getId(), is(item1.getOwner().getId()));
        assertThat(items.get(0).getOwner().getName(), is(item1.getOwner().getName()));
        assertThat(items.get(0).getOwner().getEmail(), is(item1.getOwner().getEmail()));
        assertThat(items.get(1).getId(), notNullValue());
        assertThat(items.get(1).getName(), is(item2.getName()));
        assertThat(items.get(1).getDescription(), is(item2.getDescription()));
        assertThat(items.get(1).isAvailable(), is(item2.isAvailable()));
        assertThat(items.get(1).getOwner().getId(), is(item2.getOwner().getId()));
        assertThat(items.get(1).getOwner().getName(), is(item2.getOwner().getName()));
        assertThat(items.get(1).getOwner().getEmail(), is(item2.getOwner().getEmail()));
    }
}