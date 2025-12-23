package com.example.ticketbooking.admin;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Category;
import com.example.ticketbooking.domain.Event;
import com.example.ticketbooking.domain.Genre;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminEditActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private int eventId;
    private Event event;

    private EditText etTitle, etDate, etTime, etDescription;
    private Spinner spCategory, spGenre, spHall;
    private ImageView ivEventImage;
    private Button btnSelectImage, btnSave, btnDelete;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit);

        dbHelper = new DBHelper(this);
        eventId = getIntent().getIntExtra("event_id", -1);

        if (eventId == -1) {
            Toast.makeText(this, "Ошибка: мероприятие не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadEventData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDescription = findViewById(R.id.etDescription);

        spCategory = findViewById(R.id.spCategory);
        spGenre = findViewById(R.id.spGenre);
        spHall = findViewById(R.id.spHall);

        ivEventImage = findViewById(R.id.ivEventImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadEventData() {
        // Получаем мероприятие из базы данных
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

        // Заполняем поля данными
        etTitle.setText(event.getTitle());
        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etDescription.setText(event.getDescription());

        // Загружаем изображение
        loadImageFromBase64(event.getImage(), ivEventImage);
        encodedImage = event.getImage();

        // Настраиваем спиннер категорий
        setupCategorySpinner();

        // Настраиваем спиннер жанров
        setupGenreSpinner();

        // Настраиваем спиннер залов
        setupHallSpinner();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Category.getDisplayNames()
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Устанавливаем текущую категорию
        String currentCategory = event.getCategoryString();
        for (int i = 0; i < spCategory.getCount(); i++) {
            if (spCategory.getItemAtPosition(i).toString().equals(currentCategory)) {
                spCategory.setSelection(i);
                break;
            }
        }
    }

    private void setupGenreSpinner() {
        Category category = event.getCategory();
        String[] genres = Genre.getGenresForCategory(category);

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genres
        );
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(genreAdapter);

        // Устанавливаем текущий жанр
        String currentGenre = event.getGenre();
        for (int i = 0; i < spGenre.getCount(); i++) {
            if (spGenre.getItemAtPosition(i).toString().equals(currentGenre)) {
                spGenre.setSelection(i);
                break;
            }
        }

        // Обновляем жанры при изменении категории
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = (String) parent.getItemAtPosition(position);
                Category category = Category.fromString(selectedCategory);

                String[] genres = Genre.getGenresForCategory(category);
                ArrayAdapter<String> newGenreAdapter = new ArrayAdapter<>(
                        AdminEditActivity.this,
                        android.R.layout.simple_spinner_item,
                        genres
                );
                newGenreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spGenre.setAdapter(newGenreAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupHallSpinner() {
        List<String> halls = dbHelper.getAllHallsNames();
        ArrayAdapter<String> hallAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                halls
        );
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHall.setAdapter(hallAdapter);

        // Устанавливаем текущий зал
        String hallName = dbHelper.getHallNameById(event.getHallId());
        for (int i = 0; i < spHall.getCount(); i++) {
            if (spHall.getItemAtPosition(i).toString().equals(hallName)) {
                spHall.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> saveEvent());
        btnDelete.setOnClickListener(v -> deleteEvent());

        // Кнопка "Назад"
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    private void saveEvent() {
        // Валидация данных
        if (TextUtils.isEmpty(etTitle.getText())) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etDate.getText()) || !isValidDate(etDate.getText().toString())) {
            Toast.makeText(this, "Введите корректную дату (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etTime.getText()) || !isValidTime(etTime.getText().toString())) {
            Toast.makeText(this, "Введите корректное время (HH:mm)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем значения из формы
        String title = etTitle.getText().toString();
        String category = (String) spCategory.getSelectedItem();
        String genre = (String) spGenre.getSelectedItem();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String description = etDescription.getText().toString();
        String hallName = (String) spHall.getSelectedItem();

        // Получаем ID зала
        int hallId = dbHelper.getHallIdByName(hallName);
        if (hallId == -1) {
            Toast.makeText(this, "Ошибка: зал не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Обновляем данные в базе
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("category", category);
        cv.put("genre", genre);
        cv.put("hall_id", hallId);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("description", description);
        cv.put("image", encodedImage);

        int rowsAffected = dbHelper.getWritableDatabase().update(
                "events",
                cv,
                "id = ?",
                new String[]{String.valueOf(eventId)}
        );

        if (rowsAffected > 0) {
            Toast.makeText(this, "Мероприятие обновлено", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteEvent() {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить это мероприятие?\nВсе бронирования также будут удалены.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // Удаляем сначала все бронирования этого мероприятия
                    dbHelper.getWritableDatabase().delete(
                            "bookings",
                            "event_id = ?",
                            new String[]{String.valueOf(eventId)}
                    );

                    // Удаляем само мероприятие
                    int rowsAffected = dbHelper.getWritableDatabase().delete(
                            "events",
                            "id = ?",
                            new String[]{String.valueOf(eventId)}
                    );

                    if (rowsAffected > 0) {
                        Toast.makeText(this, "Мероприятие удалено", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                ivEventImage.setImageBitmap(bitmap);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
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

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date d = sdf.parse(date);
            return d != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setLenient(false);
        try {
            Date d = sdf.parse(time);
            return d != null;
        } catch (ParseException e) {
            return false;
        }
    }
}