package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public interface UserStorage {
    User addNew(User user);

    User getById(Long id);

    List<User> getAll();

    User update(User user);

    void remove(Long id);

    boolean isFreeEmail(String email);
}