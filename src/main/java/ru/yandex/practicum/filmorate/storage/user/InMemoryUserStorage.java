package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public void addFriendRequest(Long requesterId, Long targetId) {
        User requester = getUserOrThrow(requesterId);
        User target = getUserOrThrow(targetId);

        if (requester.getFriends().contains(targetId) ||
                target.getRequestedFriends().contains(requesterId) ||
                requester.getRequestedFriends().contains(targetId)) {
            throw new IllegalStateException("Запрос на дружбу уже существует");
        }

        target.getRequestedFriends().add(requesterId);
    }

    @Override
    public void confirmFriendship(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (!user.getRequestedFriends().contains(friendId)) {
            throw new NotFoundException("Запрос на дружбу от пользователя " + friendId + " не найден");
        }

        user.getRequestedFriends().remove(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriendship(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (!user.getFriends().contains(friendId) || !friend.getFriends().contains(userId)) {
            throw new NotFoundException("Дружба между пользователями не найдена");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    private User getUserOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}