package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmTest {
    private Validator validator;
    private Film.FilmBuilder validFilmBuilder;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validFilmBuilder = Film.builder()
                .name("Valid Film")
                .description("Valid Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L);
    }

    @Test
    void shouldPassValidationWithValidData() {
        Film film = validFilmBuilder.build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = validFilmBuilder.name(" ").build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Название фильма не может быть пустым");
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        String longDescription = "a".repeat(201);
        Film film = validFilmBuilder.description(longDescription).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailWhenReleaseDateTooEarly() {
        Film film = validFilmBuilder
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
        Film film = validFilmBuilder
                .description("a".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1L)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertThat(violations).isEmpty();
    }
}