package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Получен запрос на получение всех фильмов");
        Collection<Film> films = filmService.findAll();
        log.debug("Количество фильмов: {}", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.debug("Получен запрос на фильм с id: {}", id);
        Film film = filmService.findById(id);
        log.debug("Найден фильм: {}", film);
        return film;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Получен запрос на создание фильма: {}", film);
        Film createdFilm = filmService.create(film);
        log.debug("Создан фильм: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.debug("Получен запрос на обновление фильма: {}", film);
        Film updatedFilm = filmService.update(film);
        log.debug("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Запрос на добавление лайка пользователем {} к фильму {}", userId, id);
        filmService.addLike(id, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Запрос на удаление лайка пользователем {} с фильма {}", userId, id);
        filmService.removeLike(id, userId);
        log.debug("Пользователь {} удалил лайк с фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.debug("Запрос на получение популярных фильмов. Количество: {}", count);
        List<Film> popularFilms = filmService.getTopFilms(count);
        log.debug("Получено популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }
}
