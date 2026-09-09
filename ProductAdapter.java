package com.pricecompare.app.pricecompare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pricecompare.app.R;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface Listener {
        void onView(ProductGroup group);
        void onDelete(ProductGroup group);
        default void onFavourite(ProductGroup group) {}
        default void onAddToShoppingList(ProductGroup group) {}
    }

    private List<ProductGroup> groups;
    private final Listener listener;

    public ProductAdapter(List<ProductGroup> groups, Listener listener) {
        this.groups   = groups;
        this.listener = listener;
    }

    public void updateData(List<ProductGroup> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductGroup group = groups.get(position);
        holder.tvProductName.setText(group.getProductName());

        PriceEntry lowest = group.getLowestEntry();
        if (lowest != null) {
            holder.tvLowestPrice.setText(String.format(Locale.getDefault(),
                    "Lowest: RM%.2f", lowest.getPrice()));
        } else {
            holder.tvLowestPrice.setText("");
        }

        int storeCount = group.getStoreCount();
        int entryCount = group.getEntryCount();
        String storeText = storeCount == 1 ? "1 store" : storeCount + " stores";
        String entryText = entryCount == 1 ? "1 entry" : entryCount + " entries";
        holder.tvStoreCount.setText(storeText + " • " + entryText + " recorded");

        
        
        String category = group.getCategory();
        holder.ivCategoryIcon.setImageResource(getCategoryIcon(category));

        String imagePath = group.getFirstImagePath();
        if (imagePath != null) {
            java.io.File imageFile = new java.io.File(imagePath);
            if (imageFile.exists()) {
                holder.ivProductThumb.setVisibility(View.VISIBLE);
                holder.ivCategoryIcon.setVisibility(View.GONE);
                com.bumptech.glide.Glide.with(holder.ivProductThumb.getContext())
                        .load(imageFile)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivProductThumb);
            } else {
                holder.ivProductThumb.setVisibility(View.GONE);
                holder.ivCategoryIcon.setVisibility(View.VISIBLE);
            }
        } else {
            holder.ivProductThumb.setVisibility(View.GONE);
            holder.ivCategoryIcon.setVisibility(View.VISIBLE);
        }

        holder.btnView.setOnClickListener(v -> listener.onView(group));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(group));
        holder.btnFavourite.setOnClickListener(v -> listener.onFavourite(group));
        holder.btnAddToShoppingList.setOnClickListener(v -> listener.onAddToShoppingList(group));
    }

    
    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_category_others;
        switch (category.toLowerCase(Locale.getDefault())) {
            case "groceries":   return R.drawable.ic_category_groceries;
            case "electronics": return R.drawable.ic_category_electronics;
            case "toiletries":  return R.drawable.ic_category_toiletries;
            case "clothing":    return R.drawable.ic_category_clothing;
            default:            return R.drawable.ic_category_others;
        }
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvProductName, tvLowestPrice, tvStoreCount;
        ImageView   ivCategoryIcon, ivProductThumb;
        Button      btnView, btnDelete;
        ImageButton btnFavourite, btnAddToShoppingList;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName        = itemView.findViewById(R.id.tvProductName);
            tvLowestPrice        = itemView.findViewById(R.id.tvLowestPrice);
            tvStoreCount         = itemView.findViewById(R.id.tvStoreCount);
            ivCategoryIcon       = itemView.findViewById(R.id.ivCategoryIcon);
            ivProductThumb       = itemView.findViewById(R.id.ivProductThumb);
            btnView              = itemView.findViewById(R.id.btnView);
            btnDelete            = itemView.findViewById(R.id.btnDelete);
            btnFavourite         = itemView.findViewById(R.id.btnFavourite);
            btnAddToShoppingList = itemView.findViewById(R.id.btnAddToShoppingList);
        }
    }
}
