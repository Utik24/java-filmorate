package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private FilmController controller;
    private Validator validator;
    private Film.FilmBuilder validFilm;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validFilm = Film.builder()
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

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Название фильма не может быть пустым");
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        String longDescription = "a".repeat(201);
        Film film = validFilm.description(longDescription).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailWhenReleaseDateTooEarly() {
        Film film = validFilm
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        // Тестируем кастомную валидацию через контроллер
        FilmController controller = new FilmController();
        Exception exception = assertThrows(RuntimeException.class,
                () -> controller.createFilm(film));

        assertThat(exception.getMessage())
                .contains("Дата релиза не может быть раньше 28 декабря 1895 года");
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
    void shouldCreateFilmWithValidData() {
        Film createdFilm = controller.createFilm(validFilm.build());
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(controller.findAll()).hasSize(1);
    }


    @Test
    void shouldUpdateFilmSuccessfully() {
        Film createdFilm = controller.createFilm(validFilm.build());
        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Name")
                .build();

        Film result = controller.updateFilm(updatedFilm);
        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldRejectEmptyName() {
        Film invalidFilm = Film.builder()
                .name(" ")
                .description("A valid film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThatThrownBy(() -> controller.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Фильм не создан");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film invalidFilm = validFilm.id(999L).build();

        assertThatThrownBy(() -> controller.updateFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Фильм с таким id не найден");
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film invalidFilm = validFilm.releaseDate(LocalDate.of(1895, 12, 27)).build();

        assertThatThrownBy(() -> controller.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата релиза не может быть раньше 28 декабря 1895 года");
    }
}