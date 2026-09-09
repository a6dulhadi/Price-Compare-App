package com.pricecompare.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pricecompare.app.models.ShoppingItem;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    public interface Listener {
        void onDelete(ShoppingItem item);
        void onToggle(ShoppingItem item);
        void onUpdate(ShoppingItem item);
    }

    private final List<ShoppingItem> items;
    private final Listener listener;

    public ShoppingAdapter(List<ShoppingItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQty.setText("Qty: " + item.getQuantity());
        holder.cbBought.setChecked(item.isBought());

        holder.cbBought.setOnClickListener(v -> {
            item.setBought(holder.cbBought.isChecked());
            listener.onToggle(item);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        holder.itemView.setOnClickListener(v -> listener.onUpdate(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbBought;
        TextView tvName, tvQty;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            cbBought = itemView.findViewById(R.id.cbBought);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
