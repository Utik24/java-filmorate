package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dao.dto.MpaDto;

import java.util.List;
import java.util.Optional;

public interface MpaStorage {
    List<MpaDto> findAll();

    Optional<MpaDto> findById(Long id);
}