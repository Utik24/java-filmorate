package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Установлено имя из логина: {}", user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь с ID: {}", user.getId());
        return user;
    }

    // вспомогательный метод для генерации идентификатора нового юзера
    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User existing = users.get(user.getId());

        if (user.getLogin() != null) {
            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин не может быть пустым или содержать пробелы");
            }
            existing.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            existing.setName(user.getName());
        }

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank() || !user.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный формат email");
            }
            existing.setEmail(user.getEmail());
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата рождения не может быть в будущем");
            }
            existing.setBirthday(user.getBirthday());
        }

        log.info("Обновлен пользователь с ID: {}", user.getId());
        return existing;
    }


}
