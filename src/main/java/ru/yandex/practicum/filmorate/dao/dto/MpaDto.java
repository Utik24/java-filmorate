package ru.yandex.practicum.filmorate.dao.dto;// MpaDto.java

import lombok.Data;

@Data
public class MpaDto {
    private int id;
    private String name;

    public MpaDto(int ratingId, String ratingName) {
        this.id = ratingId;
        this.name = ratingName;
    }
}