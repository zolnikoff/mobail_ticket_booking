package com.example.ticketbooking.domain;

public class Seat {
    private int row;
    private int number;
    private boolean isAvailable;
    private boolean isSelected;
    private String bookingId;

    public Seat(int row, int number, boolean isAvailable) {
        this.row = row;
        this.number = number;
        this.isAvailable = isAvailable;
        this.isSelected = false;
        this.bookingId = null;
    }

    public int getRow() { return row; }
    public int getNumber() { return number; }
    public boolean isAvailable() { return isAvailable; }
    public boolean isSelected() { return isSelected; }
    public String getBookingId() { return bookingId; }

    public void setAvailable(boolean available) { isAvailable = available; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getSeatCode() {
        return "Ряд " + row + ", Место " + number;
    }
}