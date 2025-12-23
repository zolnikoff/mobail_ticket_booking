package com.example.ticketbooking.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Event;
import com.example.ticketbooking.domain.Hall;
import com.example.ticketbooking.domain.Seat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SeatSelectionActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private int eventId;
    private int userId;
    private Event event;
    private Hall hall;

    private LinearLayout seatLayout;
    private TextView tvEventTitle, tvEventDate, tvEventTime, tvEventHall, tvSelectedCount;
    private Button btnBook;

    private Map<String, Seat> allSeats = new HashMap<>();
    private List<Seat> selectedSeats = new ArrayList<>();
    private Map<String, Button> seatButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        dbHelper = new DBHelper(this);
        eventId = getIntent().getIntExtra("event_id", -1);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (eventId == -1 || userId == -1) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadEventInfo();
        createSeatLayout();
        setupListeners();
    }

    private void initViews() {
        seatLayout = findViewById(R.id.seatLayout);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvEventHall = findViewById(R.id.tvEventHall);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnBook = findViewById(R.id.btnBook);

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void loadEventInfo() {
        // Получаем информацию о мероприятии
        List<Event> events = dbHelper.getAllEvents();
        for (Event e : events) {
            if (e.getId() == eventId) {
                event = e;
                break;
            }
        }

        if (event == null) {
            Toast.makeText(this, "Мероприятие не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Получаем информацию о зале
        hall = dbHelper.getHallById(event.getHallId());
        if (hall == null) {
            Toast.makeText(this, "Информация о зале не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Устанавливаем информацию о мероприятии
        tvEventTitle.setText(event.getTitle());
        tvEventDate.setText(formatDate(event.getDate()));
        tvEventTime.setText(event.getTime());
        tvEventHall.setText(hall.getName());
    }

    private void createSeatLayout() {
        if (hall == null) return;

        seatLayout.removeAllViews();
        allSeats.clear();
        selectedSeats.clear();
        seatButtons.clear();

        int rows = hall.getRowsCount();
        int seatsPerRow = hall.getSeatsPerRow();

        // Получаем список забронированных мест
        List<com.example.ticketbooking.domain.Booking> bookings = dbHelper.getBookingsForEvent(eventId);
        Map<String, Boolean> bookedSeats = new HashMap<>();
        for (com.example.ticketbooking.domain.Booking booking : bookings) {
            String seatKey = booking.getRowNumber() + "-" + booking.getSeatNumber();
            bookedSeats.put(seatKey, true);
        }

        // Создаем схему мест
        for (int row = 1; row <= rows; row++) {
            // Создаем контейнер для ряда
            LinearLayout rowContainer = new LinearLayout(this);
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            rowContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Добавляем номер ряда слева
            TextView rowLabel = new TextView(this);
            rowLabel.setText("Ряд " + row);
            rowLabel.setWidth(150);
            rowLabel.setGravity(Gravity.CENTER);
            rowLabel.setTextSize(14);
            rowLabel.setTextColor(Color.BLACK);
            rowContainer.addView(rowLabel);

            // Создаем места в ряду
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                // Проверяем, забронировано ли место
                String seatKey = row + "-" + seatNum;
                boolean isBooked = bookedSeats.containsKey(seatKey);

                // Создаем объект места
                Seat seat = new Seat(row, seatNum, !isBooked);
                allSeats.put(seatKey, seat);

                // Создаем кнопку для места
                Button seatButton = new Button(this);
                seatButton.setText(String.valueOf(seatNum));
                seatButton.setTag(seatKey);

                // Настраиваем внешний вид кнопки
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        99, // Ширина кнопки
                        99  // Высота кнопки
                );
                params.setMargins(4, 4, 4, 4);
                seatButton.setLayoutParams(params);
                seatButton.setTextSize(12);

                if (isBooked) {
                    // Забронированное место
                    seatButton.setBackgroundColor(Color.GRAY);
                    seatButton.setEnabled(false);
                    seatButton.setTextColor(Color.WHITE);
                } else {
                    // Свободное место
                    seatButton.setBackgroundColor(getResources().getColor(R.color.primary));
                    seatButton.setTextColor(Color.WHITE);
                    seatButton.setOnClickListener(v -> toggleSeatSelection(seatKey));
                }

                rowContainer.addView(seatButton);
                seatButtons.put(seatKey, seatButton);

                if (seatNum % 5 == 0 && seatNum < seatsPerRow) {
                    TextView spacer = new TextView(this);
                    spacer.setWidth(20);
                    rowContainer.addView(spacer);
                }
            }

            seatLayout.addView(rowContainer);

            if (row < rows) {
                TextView rowSpacer = new TextView(this);
                rowSpacer.setHeight(8);
                seatLayout.addView(rowSpacer);
            }
        }
    }

    private void toggleSeatSelection(String seatKey) {
        Seat seat = allSeats.get(seatKey);
        Button seatButton = seatButtons.get(seatKey);

        if (seat == null || seatButton == null) return;

        if (seat.isSelected()) {
            seat.setSelected(false);
            selectedSeats.remove(seat);
            seatButton.setBackgroundColor(getResources().getColor(R.color.primary));
            seatButton.setTextColor(Color.WHITE);
        } else {
            seat.setSelected(true);
            selectedSeats.add(seat);
            seatButton.setBackgroundColor(getResources().getColor(R.color.select_btn));
            seatButton.setTextColor(Color.WHITE);
        }

        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        int selectedCount = selectedSeats.size();
        tvSelectedCount.setText("Выбрано мест: " + selectedCount);

        // Активируем/деактивируем кнопку бронирования
        if (selectedCount > 0) {
            btnBook.setEnabled(true);
            btnBook.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            btnBook.setText("Забронировать " + selectedCount + " место(а)");
        } else {
            btnBook.setEnabled(false);
            btnBook.setBackgroundTintList(getResources().getColorStateList(R.color.disabled_button));
            btnBook.setText("Забронировать выбранные места");
        }
    }

    private void setupListeners() {
        btnBook.setOnClickListener(v -> bookSelectedSeats());
    }

    private void bookSelectedSeats() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы одно место", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, не забронированы ли места другим пользователем
        for (Seat seat : selectedSeats) {
            String seatKey = seat.getRow() + "-" + seat.getNumber();
            if (dbHelper.isSeatBooked(eventId, seat.getRow(), seat.getNumber())) {
                Toast.makeText(this, "Место " + seat.getSeatCode() + " уже забронировано", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Создаем бронирования
        List<String> bookingCodes = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            String bookingCode = dbHelper.createBooking(userId, eventId, seat.getRow(), seat.getNumber());
            if (bookingCode != null) {
                bookingCodes.add(bookingCode);
            } else {
                Toast.makeText(this, "Ошибка бронирования места " + seat.getSeatCode(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (bookingCodes.size() == selectedSeats.size()) {
            // Все места успешно забронированы
            showBookingConfirmation(bookingCodes);
        }
    }

    private void showBookingConfirmation(List<String> bookingCodes) {
        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("booking_codes", bookingCodes.toArray(new String[0]));
        intent.putExtra("selected_seats", getSelectedSeatsInfo());
        startActivity(intent);
        finish();
    }

    private String getSelectedSeatsInfo() {
        StringBuilder seatsInfo = new StringBuilder();
        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat seat = selectedSeats.get(i);
            seatsInfo.append(seat.getSeatCode());
            if (i < selectedSeats.size() - 1) {
                seatsInfo.append(", ");
            }
        }
        return seatsInfo.toString();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createSeatLayout();
    }
}