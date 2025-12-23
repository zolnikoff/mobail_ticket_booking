package com.example.ticketbooking.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.LoginActivity;
import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.BookingWithDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private LinearLayout emptyState;
    private DBHelper dbHelper;
    private int userId;

    private Button btnDateToday, btnDateTomorrow, btnDateAll;
    private TextView tvInfo;

    private List<BookingWithDetails> allBookings = new ArrayList<>();
    private List<BookingWithDetails> filteredBookings = new ArrayList<>();
    private String currentDateFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        // Получаем ID пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DBHelper(this);

        initViews();
        setupUI();
        loadBookings();
    }

    private void initViews() {
        rvBookings = findViewById(R.id.rvBookings);
        emptyState = findViewById(R.id.emptyState);
        tvInfo = findViewById(R.id.tvInfo);

        btnDateToday = findViewById(R.id.btnDateToday);
        btnDateTomorrow = findViewById(R.id.btnDateTomorrow);
        btnDateAll = findViewById(R.id.btnDateAll);

        // Настройка RecyclerView
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this, filteredBookings, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingDetails(BookingWithDetails booking) {
                // Открываем детали мероприятия
                openEventDetails(booking.getEventId());
            }
        });
        rvBookings.setAdapter(adapter);

        // Настройка нижнего меню
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(this, UserMainActivity.class));
            finish();
        });

        findViewById(R.id.btnMyBookings).setOnClickListener(v -> {
            // Уже на этой странице
        });

        findViewById(R.id.btnExit).setOnClickListener(v -> exitApp());

        // Кнопка "Смотреть мероприятия" в пустом состоянии
        findViewById(R.id.btnBrowseEvents).setOnClickListener(v -> {
            startActivity(new Intent(this, UserMainActivity.class));
            finish();
        });

        // Обработчики фильтров
        setupFilterListeners();
    }

    private void setupUI() {
        // Информация о пользователе
        String userName = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("full_name", "Пользователь");
        tvInfo.setText("Билеты пользователя: " + userName);
    }

    private void setupFilterListeners() {
        btnDateToday.setOnClickListener(v -> {
            currentDateFilter = "today";
            updateDateButtons();
            filterBookings();
        });

        btnDateTomorrow.setOnClickListener(v -> {
            currentDateFilter = "tomorrow";
            updateDateButtons();
            filterBookings();
        });

        btnDateAll.setOnClickListener(v -> {
            currentDateFilter = "all";
            updateDateButtons();
            filterBookings();
        });
    }

    private void updateDateButtons() {
        // Сбрасываем цвет всех кнопок
        btnDateToday.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateToday.setTextColor(getResources().getColor(R.color.primary));

        btnDateTomorrow.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateTomorrow.setTextColor(getResources().getColor(R.color.primary));

        btnDateAll.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateAll.setTextColor(getResources().getColor(R.color.primary));

        // Устанавливаем активную кнопку
        switch (currentDateFilter) {
            case "today":
                btnDateToday.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateToday.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "tomorrow":
                btnDateTomorrow.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateTomorrow.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "all":
                btnDateAll.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateAll.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }

    private void loadBookings() {
        allBookings.clear();
        allBookings.addAll(dbHelper.getUserBookingsWithDetails(userId));

        if (allBookings.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            filterBookings();
        }
    }

    private void filterBookings() {
        filteredBookings.clear();

        if (currentDateFilter.equals("all")) {
            filteredBookings.addAll(allBookings);
        } else {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            for (BookingWithDetails booking : allBookings) {
                String eventDate = booking.getEventDate();

                if (currentDateFilter.equals("today") && eventDate.equals(today)) {
                    filteredBookings.add(booking);
                } else if (currentDateFilter.equals("tomorrow")) {
                    // Получаем завтрашнюю дату
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date date = sdf.parse(today);
                        Date tomorrow = new Date(date.getTime() + (1000 * 60 * 60 * 24));
                        String tomorrowStr = sdf.format(tomorrow);

                        if (eventDate.equals(tomorrowStr)) {
                            filteredBookings.add(booking);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Проверяем, есть ли отфильтрованные билеты
        if (filteredBookings.isEmpty()) {
            showEmptyState();
            if (!currentDateFilter.equals("all")) {
                Toast.makeText(this, "На выбранную дату билетов нет", Toast.LENGTH_SHORT).show();
            }
        } else {
            hideEmptyState();
            // Обновляем информацию
            String filterText = currentDateFilter.equals("all") ? "Всего билетов" :
                    currentDateFilter.equals("today") ? "Билетов на сегодня" : "Билетов на завтра";
            tvInfo.setText(filterText + ": " + filteredBookings.size());
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvBookings.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvBookings.setVisibility(View.VISIBLE);
    }

    private void openEventDetails(int eventId) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }

    private void exitApp() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings(); // Обновляем список при возвращении
    }
}