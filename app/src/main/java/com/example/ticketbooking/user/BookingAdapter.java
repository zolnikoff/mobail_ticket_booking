package com.example.ticketbooking.user;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.R;
import com.example.ticketbooking.domain.BookingWithDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private final Context context;
    private final List<BookingWithDetails> bookings;
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingDetails(BookingWithDetails booking);
    }

    public BookingAdapter(Context context, List<BookingWithDetails> bookings, OnBookingClickListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingWithDetails booking = bookings.get(position);

        // Заполняем данными
        holder.tvCategory.setText(booking.getEventCategory());
        holder.tvEventTitle.setText(booking.getEventTitle());
        holder.tvHall.setText(booking.getHallName());
        holder.tvSeat.setText("Ряд " + booking.getRowNumber() + ", Место " + booking.getSeatNumber());
        holder.tvBookingCode.setText(booking.getBookingCode());

        // Форматируем дату
        String formattedDate = formatDate(booking.getEventDate());
        holder.tvDate.setText(formattedDate);
        holder.tvDateTime.setText(formattedDate);
        holder.tvTime.setText(booking.getEventTime());

        // Детали мероприятия
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingDetails(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Код бронирования", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Код скопирован в буфер", Toast.LENGTH_SHORT).show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvEventTitle, tvDateTime, tvTime, tvHall, tvSeat, tvBookingCode;
        Button btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvHall = itemView.findViewById(R.id.tvHall);
            tvSeat = itemView.findViewById(R.id.tvSeat);
            tvBookingCode = itemView.findViewById(R.id.tvBookingCode);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}