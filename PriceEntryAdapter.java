package com.pricecompare.app.pricecompare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pricecompare.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PriceEntryAdapter extends RecyclerView.Adapter<PriceEntryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PriceEntry entry);
        void onDelete(PriceEntry entry);
    }

    private final List<PriceEntry> entries;
    private final Listener listener;

    public PriceEntryAdapter(List<PriceEntry> entries, Listener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PriceEntry entry = entries.get(position);

        holder.tvStoreName.setText(entry.getStoreName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "RM%.2f", entry.getPrice()));

        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(entry.getTimestamp())));

        if (entry.getNotes() == null || entry.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.GONE);
        } else {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(entry.getNotes());
        }

        double lowest = Double.MAX_VALUE;
        for (PriceEntry e : entries) {
            if (e.getPrice() < lowest) lowest = e.getPrice();
        }
        holder.tvStarBest.setVisibility(entry.getPrice() == lowest ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(entry));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvPrice, tvNotes, tvStarBest, tvDate;
        Button btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvStarBest = itemView.findViewById(R.id.tvStarBest);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
