package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {
    private UserController controller = new UserController();

    @Test
    void shouldAutoGenerateNameFromLogin() {
        User user = User.builder()
                .login("test_login")
                .email("test@email.com")
                .birthday(LocalDate.now().minusYears(20))
                .build();

        User created = controller.createUser(user);
        assertEquals("test_login", created.getName());
    }
}