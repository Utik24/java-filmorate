package ru.yandex.practicum.filmorate.dao.dbStorages;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.List;
import java.util.Optional;

@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<GenreDto> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new GenreDto(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<GenreDto> findById(Long id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<GenreDto> list = jdbcTemplate.query(sql,
                (rs, rowNum) -> new GenreDto(rs.getInt("id"), rs.getString("name")), id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}