package com.example.ticketbooking;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.database.DBUtils;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etUsername, etPassword, etPhone;
    Button btnRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DBHelper(this);

        btnRegister.setOnClickListener(v -> register());
    }

    private boolean validateInput(String fullName, String username, String password, String phone) {
        if(fullName.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!phone.matches("\\d{10,11}")) {
            Toast.makeText(this, "Введите корректный телефон (10-11 цифр)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.length() < 6){
            Toast.makeText(this, "Пароль должен быть минимум 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if(!validateInput(fullName, username, password, phone)) return;

        String hashedPassword = DBUtils.hashPassword(password);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("username", username);
        values.put("password", hashedPassword);
        values.put("phone", phone);
        values.put("role", "user");

        long result = db.insert("users", null, values);
        if(result == -1) {
            Toast.makeText(this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
