package com.example.ticketbooking.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private int eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        dbHelper = new DBHelper(this);
        eventId = getIntent().getIntExtra("event_id", -1);

        if (eventId == -1) {
            finish();
            return;
        }

        loadEventDetails();
        setupUI();
    }

    private void loadEventDetails() {
        List<Event> events = dbHelper.getAllEvents();
        for (Event e : events) {
            if (e.getId() == eventId) {
                event = e;
                break;
            }
        }
    }

    private void setupUI() {
        if (event == null) return;

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvHall = findViewById(R.id.tvHall);
        TextView tvDescription = findViewById(R.id.tvDescription);
        ImageView ivImage = findViewById(R.id.ivImage);
        Button btnBook = findViewById(R.id.btnBook);

        tvTitle.setText(event.getTitle());
        tvCategory.setText(event.getCategoryString());
        tvGenre.setText(event.getGenre());
        tvDate.setText(formatDate(event.getDate()));
        tvTime.setText(event.getTime());
        tvHall.setText(dbHelper.getHallNameById(event.getHallId()));
        tvDescription.setText(event.getDescription());

        // Загрузка изображения
        loadImageFromBase64(event.getImage(), ivImage);

        // Проверяем, есть ли свободные места
        int totalSeats = dbHelper.getTotalSeatsInHall(event.getHallId());
        int bookedSeats = dbHelper.getBookedSeatsCount(eventId);
        int availableSeats = totalSeats - bookedSeats;

        if (availableSeats > 0) {
            btnBook.setText("Купить билет (" + availableSeats + " осталось)");
            btnBook.setOnClickListener(v -> {
                Intent intent = new Intent(this, SeatSelectionActivity.class);
                intent.putExtra("event_id", eventId);
                startActivity(intent);
            });
        } else {
            btnBook.setText("Билетов нет");
            btnBook.setEnabled(false);
            btnBook.setBackgroundTintList(getResources().getColorStateList(R.color.disabled_button));
        }

        // Кнопка "Назад"
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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
}