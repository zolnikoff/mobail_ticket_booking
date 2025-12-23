package com.example.ticketbooking.domain;

import java.util.HashMap;
import java.util.Map;

public class Genre {
    private static final Map<Category, String[]> GENRES_BY_CATEGORY = new HashMap<>();

    static {
        GENRES_BY_CATEGORY.put(Category.CINEMA, new String[]{
                "Боевик", "Комедия", "Драма", "Фантастика", "Ужасы",
                "Мультфильм", "Мелодрама", "Триллер", "Детектив"
        });

        GENRES_BY_CATEGORY.put(Category.THEATER, new String[]{
                "Драма", "Комедия", "Мюзикл", "Трагедия", "Мелодрама",
                "Сказка", "Сатира", "Экспериментальный"
        });

        GENRES_BY_CATEGORY.put(Category.FOOTBALL, new String[]{
                "Чемпионат России", "Лига Чемпионов", "Кубок России",
                "Товарищеский матч", "Международный матч"
        });

        GENRES_BY_CATEGORY.put(Category.HOCKEY, new String[]{
                "КХЛ", "Чемпионат России", "Кубок Гагарина",
                "Товарищеский матч", "Международный матч"
        });
    }

    public static String[] getGenresForCategory(Category category) {
        return GENRES_BY_CATEGORY.getOrDefault(category, new String[]{"Другое"});
    }

    public static String[] getGenresForCategory(String categoryDisplayName) {
        Category category = Category.fromString(categoryDisplayName);
        return getGenresForCategory(category);
    }
}