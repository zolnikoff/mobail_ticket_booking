package com.example.ticketbooking.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Event;
import com.example.ticketbooking.domain.Hall;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEventDetailActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private int eventId;
    private Event event;
    private Hall hall;

    private LinearLayout seatLayout;
    private ImageView ivEventImage;
    private TextView tvEventTitle, tvCategory, tvDate, tvTime, tvHall, tvGenre, tvDescription;
    private TextView tvTotalSeats, tvBookedSeats, tvAvailableSeats, tvOccupancy, tvOccupancyRate;

    private Map<String, Button> seatButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        dbHelper = new DBHelper(this);
        eventId = getIntent().getIntExtra("event_id", -1);

        if (eventId == -1) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadEventInfo();
        createSeatLayout();
        updateStatistics();
    }

    private void initViews() {
        seatLayout = findViewById(R.id.seatLayout);
        ivEventImage = findViewById(R.id.ivEventImage);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvHall = findViewById(R.id.tvHall);
        tvGenre = findViewById(R.id.tvGenre);
        tvDescription = findViewById(R.id.tvDescription);
        tvTotalSeats = findViewById(R.id.tvTotalSeats);
        tvBookedSeats = findViewById(R.id.tvBookedSeats);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvOccupancy = findViewById(R.id.tvOccupancy);
        tvOccupancyRate = findViewById(R.id.tvOccupancyRate);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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

        // Заполняем информацию о мероприятии
        tvEventTitle.setText(event.getTitle());
        tvCategory.setText(event.getCategoryString());
        tvDate.setText(formatDate(event.getDate()));
        tvTime.setText(event.getTime());
        tvHall.setText(hall.getName());
        tvGenre.setText(event.getGenre());
        tvDescription.setText(event.getDescription());

        // Загружаем изображение
        loadImageFromBase64(event.getImage(), ivEventImage);
    }

    private void createSeatLayout() {
        if (hall == null) return;

        seatLayout.removeAllViews();
        seatButtons.clear();

        int rows = hall.getRowsCount();
        int seatsPerRow = hall.getSeatsPerRow();

        // Получаем список забронированных мест
        List<com.example.ticketbooking.domain.Booking> bookings = dbHelper.getBookingsForEvent(eventId);
        Map<String, com.example.ticketbooking.domain.Booking> bookedSeatsMap = new HashMap<>();
        for (com.example.ticketbooking.domain.Booking booking : bookings) {
            String seatKey = booking.getRowNumber() + "-" + booking.getSeatNumber();
            bookedSeatsMap.put(seatKey, booking);
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
            rowLabel.setWidth(100);
            rowLabel.setGravity(Gravity.CENTER);
            rowLabel.setTextSize(14);
            rowLabel.setTextColor(getResources().getColor(R.color.primary));
            rowContainer.addView(rowLabel);

            // Создаем места в ряду
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                String seatKey = row + "-" + seatNum;
                int finalSeatNum = seatNum;
                int finalRow = row;
                com.example.ticketbooking.domain.Booking booking = bookedSeatsMap.get(seatKey);

                // Создаем кнопку для места
                Button seatButton = new Button(this);
                seatButton.setText(String.valueOf(seatNum));
                seatButton.setTag(seatKey);

                // Настраиваем внешний вид кнопки
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        60, // Ширина кнопки
                        60  // Высота кнопки
                );
                params.setMargins(4, 4, 4, 4);
                seatButton.setLayoutParams(params);
                seatButton.setTextSize(12);

                if (booking != null) {
                    // Забронированное место - красное
                    seatButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    seatButton.setTextColor(android.graphics.Color.WHITE);
                    int finalRow1 = row;
                    int finalSeatNum1 = seatNum;
                    seatButton.setOnClickListener(v -> showBookingInfoDialog(finalRow1, finalSeatNum1, booking));
                } else {
                    // Свободное место - зеленое
                    seatButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    seatButton.setTextColor(android.graphics.Color.WHITE);
                    seatButton.setOnClickListener(v -> {
                        Toast.makeText(this,
                                "Место свободно: Ряд " + finalRow + ", Место " + finalSeatNum,
                                Toast.LENGTH_SHORT).show();
                    });
                }

                rowContainer.addView(seatButton);
                seatButtons.put(seatKey, seatButton);

                // Добавляем отступ после каждого 5-го места для удобства
                if (seatNum % 5 == 0 && seatNum < seatsPerRow) {
                    TextView spacer = new TextView(this);
                    spacer.setWidth(20);
                    rowContainer.addView(spacer);
                }
            }

            seatLayout.addView(rowContainer);

            // Добавляем отступ между рядами
            if (row < rows) {
                TextView rowSpacer = new TextView(this);
                rowSpacer.setHeight(8);
                seatLayout.addView(rowSpacer);
            }
        }
    }

    private void showBookingInfoDialog(int row, int seat, com.example.ticketbooking.domain.Booking booking) {
        // Получаем информацию о пользователе
        String userInfo = dbHelper.getUserInfoForSeat(eventId, row, seat);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_booking_info, null);

        // Находим элементы диалога
        TextView tvSeatInfo = dialogView.findViewById(R.id.tvSeatInfo);
        TextView tvBookingCode = dialogView.findViewById(R.id.tvBookingCode);
        TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
        TextView tvUserPhone = dialogView.findViewById(R.id.tvUserPhone);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Заполняем информацию
        tvSeatInfo.setText("Ряд " + row + ", Место " + seat);
        tvBookingCode.setText(booking.getBookingCode());

        if (userInfo != null) {
            String[] parts = userInfo.split(" \\(");
            if (parts.length >= 2) {
                tvUserName.setText(parts[0]);
                tvUserPhone.setText(parts[1].replace(")", ""));
            }
        } else {
            tvUserName.setText("Информация не найдена");
            tvUserPhone.setText("Нет данных");
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();


        // Закрытие диалога
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateStatistics() {
        if (hall == null || event == null) return;

        int totalSeats = hall.getRowsCount() * hall.getSeatsPerRow();
        int bookedSeats = dbHelper.getBookedSeatsCount(eventId);
        int availableSeats = totalSeats - bookedSeats;
        double occupancyRate = totalSeats > 0 ? (bookedSeats * 100.0 / totalSeats) : 0;

        tvTotalSeats.setText(String.valueOf(totalSeats));
        tvBookedSeats.setText(String.valueOf(bookedSeats));
        tvAvailableSeats.setText(String.valueOf(availableSeats));
        tvOccupancy.setText(bookedSeats + "/" + totalSeats);
        tvOccupancyRate.setText(String.format(Locale.getDefault(), "%.1f%%", occupancyRate));
    }

    private void loadImageFromBase64(String encodedImage, ImageView imageView) {
        if (encodedImage == null || encodedImage.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_home);
            return;
        }

        try {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (decodedBitmap != null) {
                imageView.setImageBitmap(decodedBitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_home);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_home);
        }
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

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Код бронирования", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createSeatLayout();
        updateStatistics();
    }
}