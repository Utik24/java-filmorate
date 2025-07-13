package ru.yandex.practicum.filmorate.dao.dbStorages;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MpaDto> findAll() {
        String sql = "SELECT id, name FROM ratings ORDER BY id";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new MpaDto(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<MpaDto> findById(Long id) {
        String sql = "SELECT id, name FROM ratings WHERE id = ?";
        List<MpaDto> list = jdbcTemplate.query(sql,
                (rs, rowNum) -> new MpaDto(rs.getInt("id"), rs.getString("name")), id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}

