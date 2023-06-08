package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ImMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> storage = new HashMap<>();
    private long idGenerator = 1;

    @Override
    public User addNew(User user) {
        long newId = idGenerator++;
        user.setId(newId);
        storage.put(newId, user);
        return user;
    }

    @Override
    public User getById(Long id) {
        User user = storage.get(id);
        if (user == null) {
            throw new NotFoundException(String.format("User ID = %d not found!", id));
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User update(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public void remove(Long id) {
        getById(id);
        storage.remove(id);
    }

    @Override
    public boolean isFreeEmail(String email) {
        return storage.values().stream()
                .noneMatch(user -> user.getEmail().equals(email));
    }
}