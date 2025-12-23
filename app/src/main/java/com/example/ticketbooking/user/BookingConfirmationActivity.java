package com.example.ticketbooking.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;

public class BookingConfirmationActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        dbHelper = new DBHelper(this);

        String eventTitle = getIntent().getStringExtra("event_title");
        String[] bookingCodes = getIntent().getStringArrayExtra("booking_codes");
        String seatsInfo = getIntent().getStringExtra("selected_seats");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDetails = findViewById(R.id.tvDetails);
        TextView tvCodes = findViewById(R.id.tvCodes);
        Button btnHome = findViewById(R.id.btnHome);
        Button btnTickets = findViewById(R.id.btnTickets);

        tvTitle.setText("Бронирование успешно!");
        tvDetails.setText("Мероприятие: " + eventTitle + "\n" +
                "Места: " + seatsInfo + "\n" +
                "Количество билетов: " + bookingCodes.length);

        StringBuilder codesText = new StringBuilder("Коды бронирования:\n");
        for (String code : bookingCodes) {
            codesText.append("• ").append(code).append("\n");
        }
        tvCodes.setText(codesText.toString());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnTickets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}