package com.example.ticketbooking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ticketbooking.domain.Booking;
import com.example.ticketbooking.domain.BookingWithDetails;
import com.example.ticketbooking.domain.Category;
import com.example.ticketbooking.domain.Event;
import com.example.ticketbooking.domain.Hall;
import com.example.ticketbooking.domain.Seat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ticket_booking.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Пользователи
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT," +
                        "role TEXT," +
                        "full_name TEXT," +
                        "phone TEXT)"
        );

        // Залы
        db.execSQL(
                "CREATE TABLE halls (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "rows_count INTEGER," +
                        "seats_per_row INTEGER)"
        );

        // Мероприятия
        db.execSQL(
                "CREATE TABLE events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "category TEXT," +
                        "genre TEXT," +
                        "hall_id INTEGER," +
                        "date TEXT," +
                        "time TEXT," +
                        "description TEXT," +
                        "image TEXT)"
        );

        // Бронирования
        db.execSQL(
                "CREATE TABLE bookings (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "event_id INTEGER," +
                        "row_number INTEGER," +
                        "seat_number INTEGER," +
                        "booking_code TEXT)"
        );

        createDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS halls");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS bookings");
        onCreate(db);
    }

    private void createDefaultAdmin(SQLiteDatabase db) {
        String username = "admin";
        String password = DBUtils.hashPassword("admin");
        String role = "admin";
        String fullName = "Administrator";

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        values.put("full_name", fullName);

        db.insert("users", null, values);
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM events", null);

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("hall_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image"))
                );
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    public List<Event> getFilteredEvents(String category, String genre) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM events WHERE 1=1");
        List<String> args = new ArrayList<>();

        // Добавляем условия фильтрации
        if (category != null && !category.isEmpty() && !category.equals("Все категории")) {
            query.append(" AND category = ?");
            args.add(category);
        }

        if (genre != null && !genre.isEmpty() && !genre.equals("Все жанры")) {
            query.append(" AND genre = ?");
            args.add(genre);
        }

        // Сортируем по дате и времени
        query.append(" ORDER BY date, time");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("hall_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image"))
                );
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    public List<String> getAllCategories() {
        // Возвращаем список категорий из enum
        return Arrays.asList(Category.getDisplayNames());
    }

    public List<String> getGenresByCategory(String categoryDisplayName) {
        List<String> genres = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT DISTINCT genre FROM events WHERE category = ? ORDER BY genre",
                new String[]{categoryDisplayName}
        );

        while (c.moveToNext()) {
            genres.add(c.getString(0));
        }
        c.close();
        return genres;
    }

    public List<String> getAllHallsNames() {
        List<String> halls = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM halls ORDER BY name", null);
        while (c.moveToNext()) {
            halls.add(c.getString(0));
        }
        c.close();
        return halls;
    }

    public int getHallIdByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM halls WHERE name = ?", new String[]{name});
        int id = -1;
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }

    public String getHallNameById(int hallId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM halls WHERE id = ?", new String[]{String.valueOf(hallId)});
        String name = "";
        if (c.moveToFirst()) {
            name = c.getString(0);
        }
        c.close();
        return name;
    }

    public List<Event> getEventById(int eventId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM events WHERE id = ?",
                new String[]{String.valueOf(eventId)}
        );

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("hall_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image"))
                );
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    public int getTotalSeatsInHall(int hallId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT rows_count * seats_per_row FROM halls WHERE id = ?",
                new String[]{String.valueOf(hallId)}
        );

        int totalSeats = 0;
        if (c.moveToFirst()) {
            totalSeats = c.getInt(0);
        }
        c.close();
        return totalSeats;
    }

    public int getBookedSeatsCount(int eventId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM bookings WHERE event_id = ?",
                new String[]{String.valueOf(eventId)}
        );

        int bookedSeats = 0;
        if (c.moveToFirst()) {
            bookedSeats = c.getInt(0);
        }
        c.close();
        return bookedSeats;
    }

    public List<Event> getEventsWithFilters(String category, String genre, String dateFilter) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM events WHERE 1=1");
        List<String> args = new ArrayList<>();

        // Фильтр по категории
        if (category != null && !category.isEmpty() && !category.equals("Все категории")) {
            query.append(" AND category = ?");
            args.add(category);
        }

        // Фильтр по жанру
        if (genre != null && !genre.isEmpty() && !genre.equals("Все жанры")) {
            query.append(" AND genre = ?");
            args.add(genre);
        }

        // Фильтр по дате
        if (dateFilter != null && !dateFilter.isEmpty()) {
            switch (dateFilter) {
                case "today":
                    query.append(" AND date = date('now')");
                    break;
                case "tomorrow":
                    query.append(" AND date = date('now', '+1 day')");
                    break;
                case "week":
                    query.append(" AND date BETWEEN date('now') AND date('now', '+7 days')");
                    break;
                case "future":
                    query.append(" AND date >= date('now')");
                    break;
            }
        } else {
            // По умолчанию показываем только будущие мероприятия
            query.append(" AND date >= date('now')");
        }

        query.append(" ORDER BY date, time");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("hall_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image"))
                );
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    public List<String> createMultipleBookings(int userId, int eventId, List<Seat> seats) {
        List<String> bookingCodes = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            for (Seat seat : seats) {
                // Генерация уникального кода бронирования
                String bookingCode = "BK" + System.currentTimeMillis() + userId + seat.getRow() + seat.getNumber();

                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("event_id", eventId);
                values.put("row_number", seat.getRow());
                values.put("seat_number", seat.getNumber());
                values.put("booking_code", bookingCode);

                long result = db.insert("bookings", null, values);

                if (result != -1) {
                    bookingCodes.add(bookingCode);
                } else {
                    // Откатываем транзакцию в случае ошибки
                    db.endTransaction();
                    return new ArrayList<>();
                }
            }

            db.setTransactionSuccessful();
            return bookingCodes;

        } finally {
            db.endTransaction();
        }
    }

    public Hall getHallById(int hallId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM halls WHERE id = ?",
                new String[]{String.valueOf(hallId)}
        );

        Hall hall = null;
        if (cursor.moveToFirst()) {
            hall = new Hall(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("rows_count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("seats_per_row"))
            );
        }
        cursor.close();
        return hall;
    }

    public List<Booking> getBookingsForEvent(int eventId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM bookings WHERE event_id = ?",
                new String[]{String.valueOf(eventId)}
        );

        while (cursor.moveToNext()) {
            Booking booking = new Booking(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("event_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("row_number")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("seat_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("booking_code"))
            );
            bookings.add(booking);
        }
        cursor.close();
        return bookings;
    }

    public boolean isSeatBooked(int eventId, int row, int seat) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM bookings WHERE event_id = ? AND row_number = ? AND seat_number = ?",
                new String[]{String.valueOf(eventId), String.valueOf(row), String.valueOf(seat)}
        );
        boolean isBooked = cursor.getCount() > 0;
        cursor.close();
        return isBooked;
    }

    public String createBooking(int userId, int eventId, int row, int seat) {
        SQLiteDatabase db = getWritableDatabase();

        // Генерация кода бронирования
        String bookingCode = "BK" + System.currentTimeMillis() + userId;

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("event_id", eventId);
        values.put("row_number", row);
        values.put("seat_number", seat);
        values.put("booking_code", bookingCode);

        long result = db.insert("bookings", null, values);

        if (result != -1) {
            return bookingCode;
        }
        return null;
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT b.*, e.title, e.date, e.time, e.category, e.genre, h.name as hall_name " +
                        "FROM bookings b " +
                        "JOIN events e ON b.event_id = e.id " +
                        "JOIN halls h ON e.hall_id = h.id " +
                        "WHERE b.user_id = ? ORDER BY e.date DESC, e.time DESC",
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {
            Booking booking = new Booking(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("event_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("row_number")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("seat_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("booking_code"))
            );
            bookings.add(booking);
        }
        cursor.close();
        return bookings;
    }

    public String getUserInfoForSeat(int eventId, int row, int seat) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT u.full_name, u.phone FROM bookings b " +
                        "JOIN users u ON b.user_id = u.id " +
                        "WHERE b.event_id = ? AND b.row_number = ? AND b.seat_number = ?",
                new String[]{String.valueOf(eventId), String.valueOf(row), String.valueOf(seat)}
        );

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            cursor.close();
            return name + " (" + phone + ")";
        }
        cursor.close();
        return null;
    }


    public List<BookingWithDetails> getUserBookingsWithDetails(int userId) {
        List<BookingWithDetails> bookings = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Убедитесь, что у вас есть правильные имена столбцов в вашей базе данных
        String query = "SELECT b.*, e.title, e.date, e.time, e.category, e.genre, e.image, " +
                "h.name as hall_name " +
                "FROM bookings b " +
                "JOIN events e ON b.event_id = e.id " +
                "JOIN halls h ON e.hall_id = h.id " +
                "WHERE b.user_id = ? ORDER BY e.date DESC, e.time DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            try {
                BookingWithDetails booking = new BookingWithDetails(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("event_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("row_number")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("seat_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("booking_code")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image")),
                        cursor.getString(cursor.getColumnIndexOrThrow("hall_name"))
                );
                bookings.add(booking);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        return bookings;
    }

}
