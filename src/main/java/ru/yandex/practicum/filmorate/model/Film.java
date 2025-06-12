package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    // Всегда инициализируем поле пустым HashSet
    private Set<Long> likes = new HashSet<>();

    // Кастомный конструктор для билдера
    @Builder
    public Film(Long id, String name, String description, LocalDate releaseDate, Long duration, Set<Long> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = (likes == null) ? new HashSet<>() : likes;
    }

    // Валидация даты релиза
    @AssertTrue(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private boolean isValidReleaseDate() {
        if (releaseDate == null) return true;
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return !releaseDate.isBefore(minDate);
    }

    // Кастомный сеттер для защиты от null
    public void setLikes(Set<Long> likes) {
        this.likes = (likes == null) ? new HashSet<>() : likes;
    }
}