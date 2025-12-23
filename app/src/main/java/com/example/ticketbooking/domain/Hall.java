package com.example.ticketbooking.domain;

public class Hall {
    private int id;
    private String name;
    private int rowsCount;
    private int seatsPerRow;

    public Hall(int id, String name, int rowsCount, int seatsPerRow) {
        this.id = id;
        this.name = name;
        this.rowsCount = rowsCount;
        this.seatsPerRow = seatsPerRow;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public int getRowsCount() { return rowsCount; }
    public int getSeatsPerRow() { return seatsPerRow; }

    public void setName(String name) { this.name = name; }
    public void setRowsCount(int rowsCount) { this.rowsCount = rowsCount; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
}