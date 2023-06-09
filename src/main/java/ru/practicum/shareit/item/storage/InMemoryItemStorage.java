package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> storage = new HashMap<>();
    private long idGenerator = 1;

    @Override
    public Item addNew(Item item) {
        long newId = idGenerator++;
        item.setId(newId);
        storage.put(newId, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(Long itemId) {
        Item item = storage.get(itemId);
        if (item == null) {
            throw new NotFoundException(String.format("User ID = %d not found!", itemId));
        }
        return item;
    }

    @Override
    public List<Item> getAllByOwnerId(Long userId) {
        return storage.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findAvailableByText(String text) {
        return storage.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }
}
