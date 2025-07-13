package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return userStorage.findAll();
    }

    public User findById(Long id) {
        log.debug("Запрос на пользователя с id: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User create(User user) {
        log.debug("Запрос на создание пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        log.debug("Запрос на обновление пользователя: {}", user);
        // Проверка существования пользователя
        findById(user.getId());
        return userStorage.update(user);
    }

    @Transactional
    public void addFriend(Long userId, Long friendId) {
        log.debug("Пользователь {} отправляет запрос на дружбу пользователю {}", userId, friendId);
        // Проверка существования пользователей
        User user = findById(userId);
        User friend = findById(friendId);

        userStorage.addFriendRequest(userId, friendId);
        log.debug("Запрос на дружбу от {} к {} отправлен", userId, friendId);
    }

    @Transactional
    public void confirmFriend(Long userId, Long friendId) {
        log.debug("Пользователь {} подтверждает заявку в друзья от {}", userId, friendId);
        // Проверка существования пользователей
        User user = findById(userId);
        User friend = findById(friendId);

        userStorage.confirmFriendship(userId, friendId);
        log.debug("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Пользователь {} удаляет друга {}", userId, friendId);
        // Проверка существования пользователей
        User user = findById(userId);
        User friend = findById(friendId);

        userStorage.removeFriendship(userId, friendId);
        log.debug("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос на получение друзей пользователя с id: {}", userId);
        User user = findById(userId);
        Set<Long> friendIds = user.getFriends();
        // единый пакетный запрос вместо цикла
        return userStorage.findAllByIds(friendIds);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Запрос на получение общих друзей между пользователями {} и {}", userId, otherId);
        User user = findById(userId);
        User otherUser = findById(otherId);

        // пересечение множеств id-друзей
        Set<Long> common = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        // и снова единый запрос за всеми общими друзьями
        return userStorage.findAllByIds(common);
    }

}