package ru.yandex.practicum.filmorate.dao.dbStorages;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (login, name, email, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());

        if (updatedRows == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        loadFriendsAndRequests(users);
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userMapper, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        loadFriendsAndRequests(Collections.singletonList(user));
        return Optional.of(user);
    }

    @Override
    @Transactional
    public void addFriendRequest(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        // Добавляем одностороннюю дружбу без запроса
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Transactional
    @Override
    public void confirmFriendship(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        String deleteRequestSql = "DELETE FROM friend_requests WHERE requester_id = ? AND target_id = ?";
        int deletedRows = jdbcTemplate.update(deleteRequestSql, friendId, userId);

        if (deletedRows == 0) {
            throw new NotFoundException("Запрос на дружбу от пользователя " + friendId + " не найден");
        }

        String insertFriendshipSql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(insertFriendshipSql, userId, friendId);
        jdbcTemplate.update(insertFriendshipSql, friendId, userId);
    }

    @Override
    @Transactional
    public void removeFriendship(Long userId, Long friendId) {
        // Проверяем, что оба пользователя существуют
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        // Удаляем только одну сторону дружбы, идемпотентно
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        // Не бросаем исключений, если ничего не удалилось
    }


    private User getUserOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    private void loadFriendsAndRequests(List<User> users) {
        if (users.isEmpty()) return;

        Map<Long, User> userMap = new HashMap<>();
        users.forEach(user -> userMap.put(user.getId(), user));

        loadFriends(userMap);
        loadFriendRequests(userMap);
    }

    private void loadFriends(Map<Long, User> userMap) {
        if (userMap.isEmpty()) return;

        String sql = "SELECT user_id, friend_id FROM friendship WHERE user_id IN (" +
                String.join(",", Collections.nCopies(userMap.size(), "?")) + ")";

        jdbcTemplate.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            Long friendId = rs.getLong("friend_id");
            userMap.get(userId).getFriends().add(friendId);
        }, userMap.keySet().toArray());
    }

    private void loadFriendRequests(Map<Long, User> userMap) {
        if (userMap.isEmpty()) return;

        String sql = "SELECT target_id, requester_id FROM friend_requests WHERE target_id IN (" +
                String.join(",", Collections.nCopies(userMap.size(), "?")) + ")";

        jdbcTemplate.query(sql, rs -> {
            Long targetId = rs.getLong("target_id");
            Long requesterId = rs.getLong("requester_id");
            userMap.get(targetId).getRequestedFriends().add(requesterId);
        }, userMap.keySet().toArray());
    }
}