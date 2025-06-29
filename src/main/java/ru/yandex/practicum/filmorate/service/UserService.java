package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    public List<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return userStorage.findAll();
    }

    public User findById(Long id) {
        log.debug("Запрос на пользователя с id: {}", id);
        return userStorage.findById(id);
    }

    public User create(User user) {
        log.debug("Запрос на создание пользователя: {}", user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        log.debug("Запрос на обновление пользователя: {}", user);
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.debug("Пользователь {} теперь друг с {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("Пользователь {} удаляет друга {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.debug("Пользователь {} больше не друг с {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос на получение друзей пользователя с id: {}", userId);
        User user = userStorage.findById(userId);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = userStorage.findById(userId).getFriends();
        Set<Long> otherFriends = userStorage.findById(otherId).getFriends();
        log.debug("Запрос на получение общих друзей между пользователями {} и {}", userId, otherId);
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
