package com.example.ticketbooking.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.LoginActivity;
import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Category;
import com.example.ticketbooking.domain.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserMainActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventUserAdapter adapter;
    private List<Event> events = new ArrayList<>();
    private DBHelper dbHelper;

    private Spinner spinnerCategory, spinnerGenre;
    private Button btnDateToday, btnDateTomorrow, btnDateAll;
    private TextView tvWelcome, tvUserInfo;

    private String selectedCategory = "Все категории";
    private String selectedGenre = "Все жанры";
    private String selectedDateFilter = "future";

    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        // Получаем данные пользователя
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        userName = prefs.getString("full_name", "Пользователь");

        dbHelper = new DBHelper(this);

        initViews();
        setupUI();
        loadEvents();
    }

    private void initViews() {
        rvEvents = findViewById(R.id.rvEvents);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        btnDateToday = findViewById(R.id.btnDateToday);
        btnDateTomorrow = findViewById(R.id.btnDateTomorrow);
        btnDateAll = findViewById(R.id.btnDateAll);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        // Настройка RecyclerView
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventUserAdapter(this, events, new EventUserAdapter.OnEventClickListener() {
            @Override
            public void onEventDetails(Event event) {
                showEventDetails(event);
            }

            @Override
            public void onEventBook(Event event) {
                startBooking(event);
            }
        }, dbHelper);
        rvEvents.setAdapter(adapter);

        // Настройка нижнего меню
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Уже на главной
        });

        findViewById(R.id.btnMyBookings).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBookingsActivity.class));
        });

        findViewById(R.id.btnExit).setOnClickListener(v -> exitApp());
    }

    private void setupUI() {
        // Приветствие
        tvWelcome.setText("Добро пожаловать, " + userName + "!");

        // Текущая дата
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        tvUserInfo.setText("Сегодня: " + currentDate);

        // Настройка категорий
        List<String> categories = new ArrayList<>();
        categories.add("Все категории");
        categories.addAll(Arrays.asList(Category.getDisplayNames()));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Настройка жанров (изначально все)
        List<String> genres = new ArrayList<>();
        genres.add("Все жанры");

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genres
        );
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // Обработчики фильтров
        setupFilterListeners();
    }

    private void setupFilterListeners() {
        // Категория
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);
                updateGenresSpinner();
                loadEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Жанр
        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = (String) parent.getItemAtPosition(position);
                loadEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Фильтры по дате
        btnDateToday.setOnClickListener(v -> {
            selectedDateFilter = "today";
            updateDateButtons();
            loadEvents();
        });

        btnDateTomorrow.setOnClickListener(v -> {
            selectedDateFilter = "tomorrow";
            updateDateButtons();
            loadEvents();
        });

        btnDateAll.setOnClickListener(v -> {
            selectedDateFilter = "future";
            updateDateButtons();
            loadEvents();
        });
    }

    private void updateGenresSpinner() {
        List<String> genres = new ArrayList<>();
        genres.add("Все жанры");

        if (!selectedCategory.equals("Все категории")) {
            List<String> dbGenres = dbHelper.getGenresByCategory(selectedCategory);
            genres.addAll(dbGenres);
        }

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genres
        );
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);
    }

    private void updateDateButtons() {
        btnDateToday.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateToday.setTextColor(getResources().getColor(R.color.primary));

        btnDateTomorrow.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateTomorrow.setTextColor(getResources().getColor(R.color.primary));

        btnDateAll.setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
        btnDateAll.setTextColor(getResources().getColor(R.color.primary));

        switch (selectedDateFilter) {
            case "today":
                btnDateToday.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateToday.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "tomorrow":
                btnDateTomorrow.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateTomorrow.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "future":
                btnDateAll.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                btnDateAll.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }

    private void loadEvents() {
        events.clear();
        events.addAll(dbHelper.getEventsWithFilters(
                selectedCategory.equals("Все категории") ? "" : selectedCategory,
                selectedGenre.equals("Все жанры") ? "" : selectedGenre,
                selectedDateFilter
        ));

        adapter.notifyDataSetChanged();

        if (events.isEmpty()) {
            Toast.makeText(this, "Мероприятий не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEventDetails(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    private void startBooking(Event event) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra("event_id", event.getId());
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
        loadEvents();
    }
}