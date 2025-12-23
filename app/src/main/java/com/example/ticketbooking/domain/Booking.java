package com.example.ticketbooking.domain;

public class Booking {
    private int id;
    private int userId;
    private int eventId;
    private int rowNumber;
    private int seatNumber;
    private String bookingCode;

    public Booking(int id, int userId, int eventId, int rowNumber, int seatNumber, String bookingCode) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.bookingCode = bookingCode;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getEventId() { return eventId; }
    public int getRowNumber() { return rowNumber; }
    public int getSeatNumber() { return seatNumber; }
    public String getBookingCode() { return bookingCode; }

    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
}