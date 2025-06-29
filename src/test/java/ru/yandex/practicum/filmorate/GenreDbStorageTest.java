package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.dbStorages.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.dto.GenreDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    void testFindAll() {
        List<GenreDto> genres = genreStorage.findAll();
        assertThat(genres).isNotEmpty();
        assertThat(genres).extracting(GenreDto::getId).allMatch(id -> id > 0);
    }

    @Test
    void testFindByIdExists() {
        Optional<GenreDto> maybe = genreStorage.findById(1L);
        assertThat(maybe).isPresent();
        GenreDto genre = maybe.get();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isNotBlank();
    }

    @Test
    void testFindByIdNotExists() {
        Optional<GenreDto> maybe = genreStorage.findById(999L);
        assertThat(maybe).isEmpty();
    }
}
