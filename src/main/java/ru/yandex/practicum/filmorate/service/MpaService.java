package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.dbStorages.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaStorage;

    public List<MpaDto> getAllMpa() {
        return mpaStorage.findAll();
    }

    public MpaDto getMpaById(long id) {
        try {
            return mpaStorage.findById(id)
                    .orElseThrow(() -> new NotFoundException("MPA с ID " + id + " не найден"));
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA с ID " + id + " не найден");
        }
    }
}
