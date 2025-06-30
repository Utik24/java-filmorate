package ru.yandex.practicum.filmorate.model;

public enum Ratings {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String label;

    Ratings(String label) {
        this.label = label;
    }

    public static Ratings fromLabel(String label) {
        for (Ratings rating : values()) {
            if (rating.label.equalsIgnoreCase(label)) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Unknown rating: " + label);
    }

    public String getLabel() {
        return label;
    }
}
