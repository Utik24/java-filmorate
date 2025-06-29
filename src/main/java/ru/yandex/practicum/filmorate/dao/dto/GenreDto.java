package ru.yandex.practicum.filmorate.dao.dto;// GenreDto.java


import lombok.Data;

@Data
public class GenreDto {
    private int id;
    private String name;

    public GenreDto(int id, String name) {
        this.id = id;
        this.name = name;
    }
}