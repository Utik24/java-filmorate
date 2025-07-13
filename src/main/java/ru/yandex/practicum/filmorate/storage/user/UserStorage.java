package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    void addFriendRequest(Long requesterId, Long targetId);

    void confirmFriendship(Long userId, Long friendId);

    void removeFriendship(Long userId, Long friendId);

    List<User> findAllByIds(Collection<Long> ids);
}