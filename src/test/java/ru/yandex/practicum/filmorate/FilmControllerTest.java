package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFilm_ShouldValidateAllFields() throws Exception {
        // Пустое название
        Film invalidFilm = Film.builder()
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));

        // Слишком длинное описание
        String longDescription = "a".repeat(201);
        Film invalidFilm2 = Film.builder()
                .name("Name")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(invalidFilm2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Описание не должно превышать 200 символов"));
    }

    @Test
    void createFilm_ShouldValidateReleaseDate() throws Exception {
        Film invalidFilm = Film.builder()
                .name("Invalid Date Film")
                .description("Description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120L)
                .build();

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilm_ShouldHandleEmptyRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void createFilm_ShouldAcceptBoundaryValues() throws Exception {
        // Проверка максимально допустимой длины описания
        String validDescription = "a".repeat(200);
        Film validFilm = Film.builder()
                .name("Boundary Film")
                .description(validDescription)
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1L)
                .build();

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }
}