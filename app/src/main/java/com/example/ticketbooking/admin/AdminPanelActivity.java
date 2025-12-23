package com.example.ticketbooking.admin;

import com.example.ticketbooking.LoginActivity;
import com.example.ticketbooking.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Category;
import com.example.ticketbooking.domain.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private EventAdminAdapter adapter;
    private List<Event> allEvents;
    private List<Event> filteredEvents;
    private DBHelper dbHelper;
    private Spinner spinnerCategory, spinnerGenre;

    private String selectedCategory = "";
    private String selectedGenre = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        dbHelper = new DBHelper(this);

        findViewById(R.id.btnExit).setOnClickListener(v -> exitAcc());
        findViewById(R.id.btnAdd).setOnClickListener(v -> addEvent());

        // Инициализация RecyclerView
        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        // Загружаем все мероприятия
        allEvents = dbHelper.getAllEvents();
        filteredEvents = new ArrayList<>(allEvents);

        // Инициализация адаптера
        adapter = new EventAdminAdapter(this, filteredEvents, new EventAdminAdapter.OnEventActionListener() {
            @Override
            public void onEventEdit(Event event) {
                // Открываем экран редактирования
                Intent intent = new Intent(AdminPanelActivity.this, AdminEditActivity.class);
                intent.putExtra("event_id", event.getId());
                startActivityForResult(intent, 1);
            }

            @Override
            public void onEventDetails(Event event) {
                Intent intent = new Intent(AdminPanelActivity.this, AdminEventDetailActivity.class);
                intent.putExtra("event_id", event.getId());
                startActivityForResult(intent, 1);
            }
        });

        rvEvents.setAdapter(adapter);

        // Инициализация спиннеров
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerGenre = findViewById(R.id.spinnerGenre);

        // Настройка категорий
        List<String> categories = new ArrayList<>();
        categories.add("Все категории"); // Добавляем опцию "Все"
        categories.addAll(Arrays.asList(Category.getDisplayNames()));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                categories
        );
        spinnerCategory.setAdapter(categoryAdapter);

        // Настройка жанров (изначально пустой)
        List<String> genres = new ArrayList<>();
        genres.add("Все жанры");

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                genres
        );
        spinnerGenre.setAdapter(genreAdapter);

        // Обработчик выбора категории
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);

                // Обновляем список жанров для выбранной категории
                updateGenresSpinner(selectedCategory);

                // Фильтруем мероприятия
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        // Обработчик выбора жанра
        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = (String) parent.getItemAtPosition(position);

                // Фильтруем мероприятия
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            refreshEvents();
            Toast.makeText(this, "Мероприятие обновлено", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для обновления спиннера с жанрами
    private void updateGenresSpinner(String category) {
        List<String> genres = new ArrayList<>();
        genres.add("Все жанры");

        if (!category.equals("Все категории")) {
            // Получаем жанры из базы данных для выбранной категории
            List<String> dbGenres = dbHelper.getGenresByCategory(category);
            genres.addAll(dbGenres);
        }

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                genres
        );
        spinnerGenre.setAdapter(genreAdapter);
        spinnerGenre.setSelection(0);
    }

    // Метод для фильтрации мероприятий
    private void filterEvents() {
        filteredEvents.clear();

        // Если ничего не выбрано - показываем все
        if (selectedCategory.equals("Все категории") && selectedGenre.equals("Все жанры")) {
            filteredEvents.addAll(allEvents);
        } else {
            // Фильтруем по выбранным критериям
            for (Event event : allEvents) {
                boolean matchesCategory = true;
                boolean matchesGenre = true;

                // Проверка категории
                if (!selectedCategory.equals("Все категории")) {
                    matchesCategory = event.getCategoryString().equals(selectedCategory);
                }

                // Проверка жанра
                if (!selectedGenre.equals("Все жанры")) {
                    matchesGenre = event.getGenre().equals(selectedGenre);
                }

                // Если мероприятие подходит под оба фильтра - добавляем
                if (matchesCategory && matchesGenre) {
                    filteredEvents.add(event);
                }
            }
        }

        // Обновляем адаптер
        adapter.notifyDataSetChanged();

        // Показываем сообщение, если ничего не найдено
        if (filteredEvents.isEmpty()) {
            Toast.makeText(this, "Мероприятия не найдены", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для обновления списка мероприятий (например, после добавления нового)
    private void refreshEvents() {
        allEvents = dbHelper.getAllEvents();
        filterEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список при возвращении на экран
        refreshEvents();
    }

    public void exitAcc() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void addEvent() {
        startActivity(new Intent(this, AdminAddActivity.class));
    }
}