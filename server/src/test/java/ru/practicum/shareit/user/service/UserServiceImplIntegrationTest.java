package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void testPatchUpdate() {
        //given
        UserResponseDto userDto1 = userService.addNew(UserRequestDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        //when
        String newName = "Updated";
        Long userId = userDto1.getId();
        UserRequestDto userDto = UserRequestDto.builder()
                .name(newName)
                .email(null)
                .build();
        userService.patchUpdate(userId, userDto);
        //then
        TypedQuery<User> query = entityManager.createQuery("SELECT u from User u WHERE u.id = :id ", User.class);
        User actualUser = query.setParameter("id", userDto1.getId())
                .getSingleResult();

        assertThat(actualUser.getId(), equalTo(userDto1.getId()));
        assertThat(actualUser.getName(), equalTo(newName));
        assertThat(actualUser.getEmail(), notNullValue());
        assertThat(actualUser.getEmail(), equalTo(userDto1.getEmail()));
    }
}
