package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.dbStorages.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.dbStorages.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.dbStorages.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({FilmDbStorage.class, FilmMapper.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;
    private Film film;

    @BeforeEach
    void setUp() {
        // Вставляем пользователей для лайков
        jdbcTemplate.update(
                "INSERT INTO users(id, login, name, email, birthday) VALUES (?,?,?,?,?)",
                1L, "user1", "User One", "u1@example.com", Date.valueOf(LocalDate.of(1990, 1, 1))
        );
        jdbcTemplate.update(
                "INSERT INTO users(id, login, name, email, birthday) VALUES (?,?,?,?,?)",
                2L, "user2", "User Two", "u2@example.com", Date.valueOf(LocalDate.of(1991, 2, 2))
        );

        film = Film.builder()
                .name("Test Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L)
                .mpa(new MpaDto(1, ""))
                .build();
    }

    @Test
    void testCreateAndFindById() {
        Film created = filmStorage.create(film);
        assertThat(created.getId()).isPositive();
        Optional<Film> maybe = filmStorage.findById(created.getId());
        assertThat(maybe).isPresent();
        Film found = maybe.get();
        assertThat(found.getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdate() {
        Film created = filmStorage.create(film);
        created.setName("Updated");
        Film updated = filmStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void testUpdateNotFound() {
        film.setId(999L);
        assertThatThrownBy(() -> filmStorage.update(film))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testAddAndRemoveLike() {
        Film created = filmStorage.create(film);
        long fid = created.getId();

        // добавляем лайк — ожидаем countLikes == 1
        filmStorage.addLike(fid, 1L);
        Film withLike = filmStorage.findById(fid).get();
        assertThat(withLike.getCountLikes())
                .as("После добавления одного лайка countLikes должен стать 1")
                .isEqualTo(1L);

        // удаляем лайк — ожидаем countLikes == 0
        filmStorage.removeLike(fid, 1L);
        Film noLike = filmStorage.findById(fid).get();
        assertThat(noLike.getCountLikes())
                .as("После удаления лайка countLikes должен стать 0")
                .isEqualTo(0L);
    }


    @Test
    void testGetTopFilms() {
        Film a = filmStorage.create(Film.builder()
                .name("A").description("Desc").releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L).mpa(new MpaDto(1, "")).build());
        Film b = filmStorage.create(Film.builder()
                .name("B").description("Desc").releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L).mpa(new MpaDto(1, "")).build());
        filmStorage.addLike(a.getId(), 1L);
        filmStorage.addLike(a.getId(), 2L);
        filmStorage.addLike(b.getId(), 1L);
        List<Film> top = filmStorage.getTopFilms(2);
        assertThat(top).extracting(Film::getId).containsExactly(a.getId(), b.getId());
    }

    @Test
    void testCreateWithInvalidMpa() {
        film.setMpa(new MpaDto(999, ""));
        assertThatThrownBy(() -> filmStorage.create(film))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testCreateWithDuplicateGenres() {
        film.setGenres(List.of(new GenreDto(1, ""), new GenreDto(1, ""), new GenreDto(2, "")));
        Film created = filmStorage.create(film);
        assertThat(created.getGenres()).extracting(GenreDto::getId).containsExactly(1, 2);
    }
}