package com.example.ticketbooking.domain;

public class Event {
    private int id;
    private String title;
    private Category category;
    private String genre;
    private int hallId;
    private String date;
    private String time;
    private String description;
    private String image;
    private String hallName;

    public Event(int id, String title, Category  category, String genre,
                 int hallId, String date, String time, String description, String image) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.genre = genre;
        this.hallId = hallId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.image = image;
    }

    public Event(int id, String title, String categoryStr, String genre,
                 int hallId, String date, String time, String description, String image) {
        this.id = id;
        this.title = title;
        this.category = Category.fromString(categoryStr);
        this.genre = genre;
        this.hallId = hallId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.image = image;
    }


    public int getId() { return id; }
    public String getTitle() { return title; }
    public Category  getCategory() { return category; }
    public String getGenre() { return genre; }
    public int getHallId() { return hallId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public String getCategoryString() {
        return category != null ? category.getDisplayName() : "";
    }

    // Для удобства получения полной информации
    public String getFullInfo() {
        return String.format("%s (%s) - %s %s",
                title,
                category.getDisplayName(),
                date,
                time);
    }

    public void setCategory(Category category) { this.category = category; }
    public void setCategoryFromString(String categoryStr) {
        this.category = Category.fromString(categoryStr);
    }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image) { this.image = image; }
}