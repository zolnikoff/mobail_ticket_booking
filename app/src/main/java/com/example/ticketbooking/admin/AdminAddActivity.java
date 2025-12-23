package com.example.ticketbooking.admin;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.LoginActivity;
import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Category;
import com.example.ticketbooking.domain.Genre;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminAddActivity extends AppCompatActivity {

    DBHelper dbHelper;
    Spinner spinnerAddType;
    ImageView imagePreview;
    LinearLayout formContainer;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String encodedImage = ""; // Храним изображение как Base64 строку

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add);

        dbHelper = new DBHelper(this);

        spinnerAddType = findViewById(R.id.spinnerAddType);
        formContainer = findViewById(R.id.formContainer);

        findViewById(R.id.btnExit).setOnClickListener(v -> exitAcc());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Мероприятие", "Зал"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddType.setAdapter(adapter);

        spinnerAddType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                formContainer.removeAllViews();
                if (position == 0) {
                    showEventForm();
                } else {
                    showHallForm();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void showEventForm() {
        // Поля для мероприятия
        EditText title = new EditText(this);
        title.setHint("Название");

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Category.getDisplayNames()
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        Spinner genreSpinner = new Spinner(this);
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Выберите категорию сначала"}
        );
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = (String) parent.getItemAtPosition(position);
                Category category = Category.fromString(selectedCategory);

                String[] genres = Genre.getGenresForCategory(category);
                ArrayAdapter<String> newGenreAdapter = new ArrayAdapter<>(
                        AdminAddActivity.this,
                        android.R.layout.simple_spinner_item,
                        genres
                );
                newGenreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                genreSpinner.setAdapter(newGenreAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner hallSpinner = new Spinner(this);
        List<String> halls = dbHelper.getAllHallsNames();
        ArrayAdapter<String> hallAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, halls);
        hallAdapter.setDropDownViewResource(R.layout.spinner_item);
        hallSpinner.setAdapter(hallAdapter);

        EditText date = new EditText(this);
        date.setHint("Дата (dd.MM.yyyy)");

        EditText time = new EditText(this);
        time.setHint("Время (HH:mm)");

        EditText desc = new EditText(this);
        desc.setHint("Описание");

        Button saveBtn = new Button(this);
        saveBtn.setText("Сохранить");

        saveBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(title.getText())
                    || halls.isEmpty()
                    || TextUtils.isEmpty(date.getText()) || TextUtils.isEmpty(time.getText())) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }



            String selectedCategoryDisplay = (String) categorySpinner.getSelectedItem();
            Category selectedCategory = Category.fromString(selectedCategoryDisplay);
            String selectedGenre = (String) genreSpinner.getSelectedItem();

            if (selectedGenre.equals("Выберите категорию сначала")) {
                Toast.makeText(this, "Выберите жанр", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверка даты
            if (!isValidDate(date.getText().toString())) {
                Toast.makeText(this, "Неверный формат даты", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверка времени
            if (!isValidTime(time.getText().toString())) {
                Toast.makeText(this, "Неверный формат времени", Toast.LENGTH_SHORT).show();
                return;
            }

            int hallId = dbHelper.getHallIdByName((String) hallSpinner.getSelectedItem());

            ContentValues cv = new ContentValues();
            cv.put("title", title.getText().toString());
            cv.put("category", selectedCategory.getDisplayName());
            cv.put("genre", selectedGenre);
            cv.put("hall_id", hallId);
            cv.put("date", date.getText().toString());
            cv.put("time", time.getText().toString());
            cv.put("description", desc.getText().toString());
            cv.put("image", encodedImage); // Сохраняем Base64 строку

            dbHelper.getWritableDatabase().insert("events", null, cv);
            Toast.makeText(this, "Мероприятие добавлено", Toast.LENGTH_SHORT).show();

            // Очищаем форму после сохранения
            title.setText("");


            date.setText("");
            time.setText("");
            desc.setText("");
            encodedImage = "";
            imagePreview.setImageResource(R.drawable.ic_home);
            imagePreview.setVisibility(View.GONE);

            categorySpinner.setSelection(0);
            ArrayAdapter<String> resetGenreAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Выберите категорию сначала"}
            );
            resetGenreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            genreSpinner.setAdapter(resetGenreAdapter);

        });

        TextView titleLabel = new TextView(this);
        titleLabel.setText("Заголовок:");
        formContainer.addView(titleLabel);
        formContainer.addView(title);

        TextView categoryLabel = new TextView(this);

        categoryLabel.setText("Категория:");
        formContainer.addView(categoryLabel);
        formContainer.addView(categorySpinner);

        TextView genreLabel = new TextView(this);
        genreLabel.setText("Жанр:");
        formContainer.addView(genreLabel);
        formContainer.addView(genreSpinner);

        TextView hallLabel = new TextView(this);
        hallLabel.setText("Место:");
        formContainer.addView(hallLabel);
        formContainer.addView(hallSpinner);

        TextView timeLabel = new TextView(this);
        timeLabel.setText("Дата и время:");
        formContainer.addView(timeLabel);
        formContainer.addView(date);
        formContainer.addView(time);

        TextView descLabel = new TextView(this);
        descLabel.setText("Описание:");
        formContainer.addView(descLabel);
        formContainer.addView(desc);

        Button buttonSelectImage = new Button(this);
        buttonSelectImage.setText("Выбрать фото");
        buttonSelectImage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        formContainer.addView(buttonSelectImage);

        imagePreview = new ImageView(this);
        imagePreview.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
        ));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagePreview.setImageResource(R.drawable.ic_home);
        imagePreview.setVisibility(View.GONE);
        formContainer.addView(imagePreview);

        formContainer.addView(saveBtn);

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
        });
    }

    private void showHallForm() {
        EditText name = new EditText(this);
        name.setHint("Название зала");

        EditText rows = new EditText(this);
        rows.setHint("Количество рядов");

        EditText seats = new EditText(this);
        seats.setHint("Мест в ряду");

        Button saveBtn = new Button(this);
        saveBtn.setText("Сохранить");

        saveBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(rows.getText()) || TextUtils.isEmpty(seats.getText())) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            String rowsStr = rows.getText().toString();
            String seatsStr = seats.getText().toString();

            int rowsCount, seatsCount;
            try {
                rowsCount = Integer.parseInt(rowsStr);
                seatsCount = Integer.parseInt(seatsStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Количество рядов и мест должно быть числом", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put("name", name.getText().toString());
            cv.put("rows_count", rowsCount);
            cv.put("seats_per_row", seatsCount);

            dbHelper.getWritableDatabase().insert("halls", null, cv);
            Toast.makeText(this, "Зал добавлен", Toast.LENGTH_SHORT).show();

            name.setText("");
            rows.setText("");
            seats.setText("");
        });

        formContainer.addView(name);
        formContainer.addView(rows);
        formContainer.addView(seats);
        formContainer.addView(saveBtn);
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% качества
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Показываем превью
                imagePreview.setImageBitmap(bitmap);
                imagePreview.setVisibility(View.VISIBLE);

                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
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

    public void exitAcc() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}