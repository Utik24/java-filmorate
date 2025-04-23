package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> filmMap = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", filmMap.size());
        return filmMap.values();
    }

    private long getNextId() {
        long currentMaxId = filmMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film.getReleaseDate());
        if (film.getName().isBlank()) {
            log.error("Фильм с пустым id не может быть создан");
            throw new ValidationException("Фильм не создан");
        }
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Создан фильм с ID: {}", film.getId());
        log.info("Создан фильм с именем: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film.getReleaseDate());

        if (!filmMap.containsKey(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }

        filmMap.put(film.getId(), film);
        log.info("Обновлен фильм с ID: {}", film.getId());
        return film;
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза {} раньше допустимой", releaseDate);
            if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        }
    }
}
