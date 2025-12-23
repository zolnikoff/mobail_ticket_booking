package com.example.ticketbooking.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketbooking.R;
import com.example.ticketbooking.domain.Event;

import java.util.List;

public class EventAdminAdapter extends RecyclerView.Adapter<EventAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final OnEventActionListener listener;

    public EventAdminAdapter(Context context, List<Event> events, OnEventActionListener listener) {
        this.context = context;
        this.events = events;
        this.listener = listener;
    }

    public interface OnEventActionListener {
        void onEventEdit(Event event);
        void onEventDetails(Event event);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategoryString());
        holder.tvGenre.setText("Жанр: " + event.getGenre());
        holder.tvDateTime.setText(event.getDate() + " " + event.getTime());
        holder.tvDescription.setText(event.getDescription());

        loadImageFromBase64(event.getImage(), holder.imgEvent);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventEdit(event);
            }
        });


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

    // Метод для обновления данных
    public void updateData(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView tvTitle, tvCategory, tvGenre, tvDateTime, tvDescription;
        android.widget.Button btnDetails, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEvent = itemView.findViewById(R.id.imgEvent);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}