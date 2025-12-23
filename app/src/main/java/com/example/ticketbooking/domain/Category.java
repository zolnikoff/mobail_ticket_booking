package com.example.ticketbooking.domain;

public enum Category {
    CINEMA("Кино"),
    THEATER("Театр"),
    FOOTBALL("Футбол"),
    HOCKEY("Хоккей");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromString(String text) {
        for (Category category : Category.values()) {
            if (category.displayName.equals(text) ||
                    category.name().equalsIgnoreCase(text)) {
                return category;
            }
        }
        return CINEMA;
    }

    public static String[] getDisplayNames() {
        Category[] categories = values();
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].getDisplayName();
        }
        return names;
    }
}
