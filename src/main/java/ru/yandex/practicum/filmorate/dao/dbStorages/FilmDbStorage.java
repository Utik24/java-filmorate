package ru.yandex.practicum.filmorate.dao.dbStorages;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    public Film create(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating is required");
        }
        checkMpaExists(film.getMpa().getId());

        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenresForFilm(film.getId(), film.getGenres());
        return findById(film.getId()).orElseThrow();
    }

    @Override
    @Transactional
    public Film update(Film film) {
        Film existing = findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден"));

        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating is required");
        }
        checkMpaExists(film.getMpa().getId());

        String sql = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм не был обновлен");
        }

        updateGenresForFilm(film.getId(), film.getGenres());
        return findById(film.getId()).orElseThrow();
    }


    private void saveGenresForFilm(Long filmId, List<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) return;

        // убираем дубли по id
        List<GenreDto> distinct = genres.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(GenreDto::getId, Function.identity(), (g1, g2) -> g1))
                .values()
                .stream().toList();

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GenreDto genre = distinct.get(i);
                // проверяем, что жанр существует
                Integer cnt = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genres WHERE id = ?", Integer.class, genre.getId());
                if (cnt == null || cnt == 0) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
                ps.setLong(1, filmId);
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return distinct.size();
            }
        });
    }

    // В updateGenresForFilm не лезем — он просто пересохраняет, переиспользует saveGenresForFilm

    /**
     * Проверяет, что в таблице ratings есть запись с таким id.
     */
    private void checkMpaExists(int mpaId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ratings WHERE id = ?", Integer.class, mpaId);
        if (cnt == null || cnt == 0) {
            throw new NotFoundException("MPA-рейтинг с ID " + mpaId + " не найден");
        }
    }

    private void updateGenresForFilm(Long filmId, List<GenreDto> genres) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        saveGenresForFilm(filmId, genres);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        loadAdditionalData(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        loadAdditionalData(Collections.singletonList(film));
        return Optional.of(film);
    }

    private void loadAdditionalData(List<Film> films) {
        if (films.isEmpty()) return;

        // Очистка существующих данных перед загрузкой
        for (Film film : films) {
            film.getLikes().clear();
            if (film.getGenres() != null) {
                film.getGenres().clear();
            }
        }

        loadMpa(films);
        loadLikes(films);
        loadGenres(films);
    }

    private void loadMpa(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String sql = "SELECT f.id, r.id AS mpa_id, r.name AS mpa_name " +
                "FROM films f JOIN ratings r ON f.rating_id = r.id " +
                "WHERE f.id IN (" + getPlaceholders(filmMap.size()) + ")";

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.setMpa(new MpaDto(
                        rs.getInt("mpa_id"),
                        rs.getString("mpa_name")
                ));
            }
        }, filmMap.keySet().toArray());
    }

    private void loadLikes(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String sql = "SELECT film_id, user_id FROM likes " +
                "WHERE film_id IN (" + getPlaceholders(filmMap.size()) + ")";

        jdbcTemplate.query(sql, rs -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                film.getLikes().add(rs.getLong("user_id"));
            }
        }, filmMap.keySet().toArray());
    }

    private void loadGenres(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genre fg JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + getPlaceholders(filmMap.size()) + ") " +
                "ORDER BY g.id";

        jdbcTemplate.query(sql, rs -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                if (film.getGenres() == null) {
                    film.setGenres(new ArrayList<>());
                }
                film.getGenres().add(new GenreDto(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        }, filmMap.keySet().toArray());
    }

    private String getPlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);
        if (deleted == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        loadAdditionalData(films);
        return films;
    }

}