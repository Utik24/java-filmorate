package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String name;
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Long duration;
    // Всегда инициализируем поле пустым HashSet
    @JsonIgnore
    private final Set<Long> likes = new HashSet<>();

    // Валидация даты релиза
    @AssertTrue(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private boolean isValidReleaseDate() {
        if (releaseDate == null) return true;
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return !releaseDate.isBefore(minDate);
    }

}