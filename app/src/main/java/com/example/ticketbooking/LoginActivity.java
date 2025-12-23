package com.example.ticketbooking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.admin.AdminPanelActivity;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.database.DBUtils;
import com.example.ticketbooking.user.UserMainActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnRegister;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkAuthentication();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DBHelper(this);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void checkAuthentication() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "None");
        if (!role.equals("None")) {
            if (role.equals("admin")) {
                startActivity(new Intent(this, AdminPanelActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }
    }

    private void login() {


        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        var hashPassword = DBUtils.hashPassword(password);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, role, full_name  FROM users WHERE username=? AND password=?",
                new String[]{username, hashPassword}
        );

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            String role = cursor.getString(1);
            String fullName = cursor.getString(2);

            getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", userId)
                    .putString("user_role", role)
                    .putString("full_name", fullName)
                    .apply();

            if (role.equals("admin")) {
                startActivity(new Intent(this, AdminPanelActivity.class));
            } else {
                startActivity(new Intent(this, UserMainActivity.class));
            }
            finish();
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
