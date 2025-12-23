package com.example.ticketbooking.domain;

public class BookingWithDetails extends Booking {
    private String eventTitle;
    private String eventDate;
    private String eventTime;
    private String eventCategory;
    private String eventGenre;
    private String eventImage;
    private String hallName;

    public BookingWithDetails(int id, int userId, int eventId, int rowNumber,
                              int seatNumber, String bookingCode,
                              String eventTitle, String eventDate, String eventTime,
                              String eventCategory, String eventGenre, String eventImage,
                              String hallName) {
        super(id, userId, eventId, rowNumber, seatNumber, bookingCode);
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventCategory = eventCategory;
        this.eventGenre = eventGenre;
        this.eventImage = eventImage;
        this.hallName = hallName;
    }

    public String getEventTitle() { return eventTitle; }
    public String getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }
    public String getEventCategory() { return eventCategory; }
    public String getEventGenre() { return eventGenre; }
    public String getEventImage() { return eventImage; }
    public String getHallName() { return hallName; }

    // Метод для форматированной информации о бронировании
    public String getFormattedInfo() {
        return String.format(
                "%s\nДата: %s %s\nЗал: %s\nМесто: Ряд %d, Место %d\nКод брони: %s",
                eventTitle, eventDate, eventTime, hallName,
                getRowNumber(), getSeatNumber(), getBookingCode()
        );
    }
}