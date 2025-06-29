package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.dbStorages.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

    @Test
    void testFindAll() {
        List<MpaDto> mpas = mpaStorage.findAll();
        assertThat(mpas).isNotEmpty();
        assertThat(mpas).extracting(MpaDto::getId).allMatch(id -> id > 0);
    }

    @Test
    void testFindByIdExists() {
        Optional<MpaDto> maybe = mpaStorage.findById(1L);
        assertThat(maybe).isPresent();
        MpaDto mpa = maybe.get();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isNotBlank();
    }

    @Test
    void testFindByIdNotExists() {
        Optional<MpaDto> maybe = mpaStorage.findById(999L);
        assertThat(maybe).isEmpty();
    }
}