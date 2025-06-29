package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;
    private Validator validator;
    private Film.FilmBuilder validFilm;

    @BeforeEach
    void setUp() {
        // use in-memory storage
        controller = new FilmController(new ru.yandex.practicum.filmorate.service.FilmService(
                new InMemoryFilmStorage(), new InMemoryUserStorage()));
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validFilm = Film.builder()
                .mpa(new MpaDto(1, "G"))
                .name("Valid Film")
                .description("A valid film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L);
    }

    @Test
    void shouldPassValidationWithValidData() {
        Film film = validFilm.build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = validFilm.name(" ").build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations)
                .hasSize(1)
                .allMatch(v -> v.getMessage().equals("Название фильма не может быть пустым"));
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        String longDesc = "a".repeat(201);
        Film film = validFilm.description(longDesc).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations)
                .hasSize(1)
                .allMatch(v -> v.getMessage().equals("Описание не должно превышать 200 символов"));
    }

    @Test
    void shouldAcceptBoundaryValues() {
        Film film = validFilm
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1L)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldThrowWhenReleaseDateTooEarly() {
        Film film = validFilm.releaseDate(LocalDate.of(1895, 12, 27)).build();
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertThat(ex.getMessage()).contains("Дата релиза не может быть раньше 28 декабря 1895 года");
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film created = controller.create(validFilm.build());
        assertThat(created.getId()).isNotNull();
        assertThat(controller.findAll()).hasSize(1);
    }

    @Test
    void shouldUpdateFilmSuccessfully() {
        Film created = controller.create(validFilm.build());
        Film update = created.toBuilder().name("Updated Name").build();
        Film result = controller.update(update);
        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldRejectEmptyNameOnCreate() {
        Film film = validFilm.name(" ").build();
        assertThatThrownBy(() -> controller.create(film))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Название фильма не может быть пустым");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film film = validFilm.id(999L).build();
        assertThatThrownBy(() -> controller.update(film))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с ID 999 не найден");
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateTooEarlyOnCreate() {
        Film film = validFilm.releaseDate(LocalDate.of(1895, 12, 27)).build();
        assertThatThrownBy(() -> controller.create(film))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата релиза не может быть раньше 28 декабря 1895 года");
    }
}
