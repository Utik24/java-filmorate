package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.dbStorages.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;

    public List<GenreDto> getAllGenres() {
        return genreStorage.findAll();
    }

    public GenreDto getGenreById(long id) {
        try {
            return genreStorage.findById(id)
                    .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
    }
}
