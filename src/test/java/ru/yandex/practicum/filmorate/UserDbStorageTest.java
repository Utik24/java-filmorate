package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.dbStorages.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({UserDbStorage.class, UserMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private User userTemplate;

    @BeforeEach
    void setUp() {
        userTemplate = User.builder()
                .login("user1")
                .name("User One")
                .email("u1@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void testCreateAndFindById() {
        User created = userStorage.create(userTemplate);
        assertThat(created.getId()).isPositive();
        Optional<User> maybe = userStorage.findById(created.getId());
        assertThat(maybe).isPresent();
        assertThat(maybe.get().getLogin()).isEqualTo("user1");
    }

    @Test
    void testUpdate() {
        User created = userStorage.create(userTemplate);
        created.setName("Updated Name");
        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testUpdateNotFoundThrows() {
        User notExists = User.builder()
                .id(999L)
                .login("nouser")
                .name("No User")
                .email("nouser@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        assertThatThrownBy(() -> userStorage.update(notExists))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testFindAll() {
        userStorage.create(userTemplate);
        List<User> all = userStorage.findAll();
        assertThat(all).isNotEmpty();
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> maybe = userStorage.findById(999L);
        assertThat(maybe).isEmpty();
    }

    @Test
    void testAddAndRemoveFriendRequest() {
        User u1 = userStorage.create(userTemplate);
        User u2 = userStorage.create(
                User.builder()
                        .login("user2")
                        .name("User Two")
                        .email("u2@example.com")
                        .birthday(LocalDate.of(1991, 2, 2))
                        .build());
        // add friend request (actually inserts into friendship)
        userStorage.addFriendRequest(u1.getId(), u2.getId());
        User loaded1 = userStorage.findById(u1.getId()).get();
        assertThat(loaded1.getFriends()).contains(u2.getId());

        // confirmFriendship should throw NotFoundException (no friend_requests table entry)
        assertThatThrownBy(() -> userStorage.confirmFriendship(u1.getId(), u2.getId()))
                .isInstanceOf(NotFoundException.class);

        // remove friendship
        userStorage.removeFriendship(u1.getId(), u2.getId());
        User loadedAfterRemove = userStorage.findById(u1.getId()).get();
        assertThat(loadedAfterRemove.getFriends()).doesNotContain(u2.getId());
    }
}
