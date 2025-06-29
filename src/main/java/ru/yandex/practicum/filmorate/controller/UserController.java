package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Получен запрос на получение всех пользователей");
        Collection<User> users = userService.findAll();
        log.debug("Количество пользователей: {}", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        log.debug("Получен запрос на пользователя с id: {}", id);
        User user = userService.findById(id);
        log.debug("Найден пользователь: {}", user);
        return user;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("Получен запрос на создание пользователя: {}", user);
        User createdUser = userService.create(user);
        log.debug("Создан пользователь: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.debug("Получен запрос на обновление пользователя: {}", user);
        User updatedUser = userService.update(user);
        log.debug("Обновлен пользователь: {}", updatedUser);
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Запрос на добавление в друзья: пользователь {} - друг {}", id, friendId);
        userService.addFriend(id, friendId);
        log.debug("Пользователь {} теперь друг с {}", id, friendId);
        return userService.findById(friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Запрос на удаление друга: пользователь {} - друг {}", id, friendId);
        User friend = userService.findById(friendId);
        userService.removeFriend(id, friendId);
        log.debug("Пользователь {} больше не друг с {}", id, friendId);
        return friend;
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.debug("Запрос на получение списка друзей пользователя с id: {}", id);
        List<User> friends = userService.getFriends(id);
        log.debug("Количество друзей: {}", friends.size());
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Запрос на получение общих друзей между пользователями {} и {}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.debug("Количество общих друзей: {}", commonFriends.size());
        return commonFriends;
    }
}
