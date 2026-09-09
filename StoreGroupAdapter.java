package com.pricecompare.app.pricecompare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pricecompare.app.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoreGroupAdapter extends RecyclerView.Adapter<StoreGroupAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PriceEntry entry);
        void onDelete(PriceEntry entry);
    }

    private final List<String>                  storeNames = new ArrayList<>();
    private final Map<String, List<PriceEntry>> storeMap   = new LinkedHashMap<>();
    private final Listener                      listener;

    public StoreGroupAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setEntries(List<PriceEntry> entries) {
        storeNames.clear();
        storeMap.clear();

        for (PriceEntry e : entries) {
            String store = e.getStoreName() != null ? e.getStoreName() : "Unknown";
            if (!storeMap.containsKey(store)) {
                storeNames.add(store);
                storeMap.put(store, new ArrayList<>());
            }
            storeMap.get(store).add(e);
        }

        
        for (List<PriceEntry> list : storeMap.values()) {
            list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String           store        = storeNames.get(position);
        List<PriceEntry> storeEntries = storeMap.get(store);
        Context          ctx          = holder.itemView.getContext();

        holder.tvStoreHeader.setText(store);
        holder.llDateRows.removeAllViews();

        SimpleDateFormat sdf      = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        boolean          expanded = Boolean.TRUE.equals(holder.isExpanded);
        int showCount = (expanded || storeEntries.size() <= 3) ? storeEntries.size() : 3;

        for (int i = 0; i < showCount; i++) {
            PriceEntry e = storeEntries.get(i);

            
            LinearLayout outerRow = new LinearLayout(ctx);
            outerRow.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            outerParams.bottomMargin = dpToPx(ctx, 8);
            outerRow.setLayoutParams(outerParams);

            
            LinearLayout dateRow = new LinearLayout(ctx);
            dateRow.setOrientation(LinearLayout.HORIZONTAL);
            dateRow.setGravity(Gravity.CENTER_VERTICAL);
            dateRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            
            TextView tvDate = new TextView(ctx);
            tvDate.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvDate.setText(sdf.format(new Date(e.getTimestamp())));
            tvDate.setTextSize(13f);
            tvDate.setTextColor(Color.parseColor("#888888"));
            dateRow.addView(tvDate);

            
            TextView tvPrice = new TextView(ctx);
            tvPrice.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tvPrice.setText(String.format(Locale.getDefault(), "RM%.2f", e.getPrice()));
            tvPrice.setTextSize(13f);
            tvPrice.setTypeface(null, Typeface.BOLD);
            dateRow.addView(tvPrice);

            outerRow.addView(dateRow);

            
            String notes = e.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                LinearLayout notesRow = new LinearLayout(ctx);
                notesRow.setOrientation(LinearLayout.HORIZONTAL);
                notesRow.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams notesRowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                notesRowParams.topMargin = dpToPx(ctx, 2);
                notesRow.setLayoutParams(notesRowParams);

                View spacer = new View(ctx);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                notesRow.addView(spacer);

                TextView tvNote = new TextView(ctx);
                LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                noteParams.setMarginEnd(dpToPx(ctx, 4));
                tvNote.setLayoutParams(noteParams);
                tvNote.setText("📝 " + notes.trim());
                tvNote.setTextSize(11.5f);
                tvNote.setTypeface(null, Typeface.ITALIC);
                tvNote.setTextColor(Color.parseColor("#777777"));
                notesRow.addView(tvNote);

                outerRow.addView(notesRow);
            }

            holder.llDateRows.addView(outerRow);
        }

        
        String photoPath = null;
        for (PriceEntry e : storeEntries) {
            if (e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                photoPath = e.getImageUrl();
                break;
            }
        }
        if (photoPath != null) {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                holder.ivEntryPhoto.setVisibility(View.VISIBLE);
                Glide.with(ctx)
                        .load(imageFile)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivEntryPhoto);
            } else {
                holder.ivEntryPhoto.setVisibility(View.GONE);
            }
        } else {
            holder.ivEntryPhoto.setVisibility(View.GONE);
        }

        
        if (storeEntries.size() > 3) {
            holder.btnSeeAll.setVisibility(View.VISIBLE);
            holder.btnSeeAll.setText(expanded
                    ? "See less"
                    : "See all (" + storeEntries.size() + ")");
            holder.btnSeeAll.setOnClickListener(v -> {
                holder.isExpanded = !expanded;
                notifyItemChanged(position);
            });
        } else {
            holder.btnSeeAll.setVisibility(View.GONE);
        }

        
        PriceEntry latest = storeEntries.get(0);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(latest));

        
        holder.btnDelete.setOnClickListener(v -> {
            if (storeEntries.size() == 1) {
                listener.onDelete(storeEntries.get(0));
            } else {
                String[] labels = new String[storeEntries.size()];
                for (int i = 0; i < storeEntries.size(); i++) {
                    labels[i] = sdf.format(new Date(storeEntries.get(i).getTimestamp()))
                            + "  RM" + String.format(Locale.getDefault(), "%.2f",
                            storeEntries.get(i).getPrice());
                }
                new AlertDialog.Builder(ctx)
                        .setTitle("Delete which entry?")
                        .setItems(labels, (dialog, which) ->
                                listener.onDelete(storeEntries.get(which)))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() { return storeNames.size(); }

    private int dpToPx(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView     tvStoreHeader;
        LinearLayout llDateRows;
        ImageView    ivEntryPhoto;
        Button       btnSeeAll, btnEdit, btnDelete;
        Boolean      isExpanded = false;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreHeader = itemView.findViewById(R.id.tvStoreHeader);
            llDateRows    = itemView.findViewById(R.id.llDateRows);
            ivEntryPhoto  = itemView.findViewById(R.id.ivEntryPhoto);
            btnSeeAll     = itemView.findViewById(R.id.btnSeeAll);
            btnEdit       = itemView.findViewById(R.id.btnEdit);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }
    }
}
