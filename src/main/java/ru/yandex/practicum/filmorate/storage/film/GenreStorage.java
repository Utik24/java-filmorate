package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dao.dto.GenreDto;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<GenreDto> findAll();

    Optional<GenreDto> findById(Long id);
}