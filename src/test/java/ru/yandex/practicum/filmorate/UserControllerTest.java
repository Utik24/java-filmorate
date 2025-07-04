package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    private UserController controller;
    private Validator validator;
    private User.UserBuilder validUserBuilder;

    @BeforeEach
    void setUp() {
        UserService userService = new UserService(new InMemoryUserStorage());
        controller = new UserController(userService);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validUserBuilder = User.builder()
                .login("valid_login")
                .email("valid@email.com")
                .birthday(LocalDate.now().minusYears(20));
    }

    @Test
    void shouldAutoGenerateNameFromLogin() {
        User user = User.builder()
                .name("Kirill")
                .login("test_login")
                .email("test@email.com")
                .birthday(LocalDate.now().minusYears(20))
                .build();

        User created = controller.create(user);
        assertEquals("test_login", created.getLogin());
    }

    @Test
    void shouldPassValidationWithValidData() {
        User user = validUserBuilder.build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenLoginHasSpaces() {
        User user = validUserBuilder.login("invalid login").build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Логин не может содержать пробелы");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User user = validUserBuilder.email("invalid-email").build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Некорректный формат email");
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        // Создаем пользователя с пустым именем
        User user = validUserBuilder
                .name("")
                .login("valid_login")
                .build();

        User createdUser = controller.create(user);

        assertThat(createdUser.getName()).isEqualTo("valid_login");
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = validUserBuilder
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
    }
}
