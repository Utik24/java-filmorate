package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
        validFilm = Film.builder()
                .name("Valid Film")
                .description("A valid film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film createdFilm = controller.createFilm(validFilm);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(controller.findAll()).hasSize(1);
    }



    @Test
    void shouldUpdateFilmSuccessfully() {
        Film createdFilm = controller.createFilm(validFilm);
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
        Film invalidFilm = validFilm.toBuilder()
                .id(999L)
                .build();

        assertThatThrownBy(() -> controller.updateFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Фильм с таким id не найден");
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film invalidFilm = validFilm.toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        assertThatThrownBy(() -> controller.createFilm(invalidFilm))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата релиза не может быть раньше 28 декабря 1895 года");
    }
}