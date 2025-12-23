package com.example.ticketbooking.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.R;
import com.example.ticketbooking.database.DBHelper;
import com.example.ticketbooking.domain.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventUserAdapter extends RecyclerView.Adapter<EventUserAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final OnEventClickListener listener;
    private final DBHelper dbHelper;

    public interface OnEventClickListener {
        void onEventDetails(Event event);
        void onEventBook(Event event);
    }

    public EventUserAdapter(Context context, List<Event> events, OnEventClickListener listener, DBHelper dbHelper) {
        this.context = context;
        this.events = events;
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        // Основная информация
        holder.tvTitle.setText(event.getTitle());
        holder.tvCategoryBadge.setText(event.getCategoryString());
        holder.tvDateTime.setText(formatDate(event.getDate()));
        holder.tvTime.setText(event.getTime());
        holder.tvDescription.setText(event.getDescription());

        // Информация о зале
        String hallName = dbHelper.getHallNameById(event.getHallId());
        holder.tvHall.setText(hallName);

        // Загрузка изображения
        loadImageFromBase64(event.getImage(), holder.imgEvent);

        // Оставшиеся билеты
        int totalSeats = dbHelper.getTotalSeatsInHall(event.getHallId());
        int bookedSeats = dbHelper.getBookedSeatsCount(event.getId());
        int availableSeats = totalSeats - bookedSeats;

        holder.tvAvailableTickets.setText(String.valueOf(availableSeats));

        // Настройка кнопки "Купить"
        if (availableSeats > 0) {
            holder.btnBuy.setVisibility(View.VISIBLE);
            holder.btnBuy.setText("Купить (" + availableSeats + ")");
            holder.btnBuy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventBook(event);
                }
            });
        } else {
            holder.btnBuy.setVisibility(View.GONE);
            holder.btnDetails.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }

        // Кнопка "Подробнее"
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventDetails(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView tvTitle, tvCategoryBadge, tvDateTime, tvTime, tvHall, tvDescription, tvAvailableTickets;
        Button btnDetails, btnBuy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEvent = itemView.findViewById(R.id.imgEvent);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvHall = itemView.findViewById(R.id.tvHall);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAvailableTickets = itemView.findViewById(R.id.tvAvailableTickets);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}