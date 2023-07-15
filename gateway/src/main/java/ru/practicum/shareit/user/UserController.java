package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validator.validationGroups.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @Validated(OnCreate.class)
    public ResponseEntity<Object> addNew(@RequestBody @Valid UserRequestDto userRequestDto) {
        return userClient.addNew(userRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable
            @Min(value = 1,
                    message = "ID cannot be less than 1")
            Long id) {
        return userClient.getById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchUpdate(@PathVariable Long id,
                                              @RequestBody @Valid UserRequestDto userRequestDto) {
        return userClient.patchUpdate(id, userRequestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> remove(@PathVariable Long id) {
        return userClient.remove(id);
    }
}
