package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder(toBuilder = true)
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private Long duration;

    @NotNull(message = "Рейтинг MPA обязателен")
    private MpaDto mpa;

    private List<GenreDto> genres;

    private Long countLikes;
}