package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return (List<Film>) filmStorage.findAll();
    }

    public Film findById(Long id) {
        log.debug("Запрос на получение фильма с id: {}", id);
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        if (film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        log.debug("Запрос на создание фильма: {}", film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Запрос на обновление фильма: {}", film);
        findById(film.getId());
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        userStorage.findById(userId); // Валидация наличия пользователя
        film.getLikes().add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Пользователь {} удаляет лайк с фильма {}", userId, filmId);
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        userStorage.findById(userId);
        film.getLikes().remove(userId);
        log.debug("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        log.debug("Запрос на получение ТОП фильмов. Количество: {}", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

}
