package com.pricecompare.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.models.ShoppingItem;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView rvShoppingList;
    private ShoppingAdapter adapter;
    private final List<ShoppingItem> items = new ArrayList<>();
    private DatabaseReference shoppingRef;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        shoppingRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("shopping_list");

        rvShoppingList = findViewById(R.id.rvShoppingList);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        rvShoppingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShoppingAdapter(items, new ShoppingAdapter.Listener() {
            @Override
            public void onDelete(ShoppingItem item) {
                shoppingRef.child(item.getId()).removeValue();
            }

            @Override
            public void onToggle(ShoppingItem item) {
                shoppingRef.child(item.getId()).child("bought").setValue(item.isBought());
            }

            @Override
            public void onUpdate(ShoppingItem item) {
                showAddEditDialog(item);
            }
        });
        rvShoppingList.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        loadItems();
    }

    private void loadItems() {
        shoppingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ShoppingItem item = child.getValue(ShoppingItem.class);
                    if (item != null) {
                        item.setId(child.getKey());
                        items.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddEditDialog(ShoppingItem itemToEdit) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_shopping_item, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);

        if (itemToEdit != null) {
            etName.setText(itemToEdit.getName());
            etQuantity.setText(itemToEdit.getQuantity());
        }

        new AlertDialog.Builder(this)
                .setTitle(itemToEdit == null ? "Add Item" : "Update Item")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String qty = etQuantity.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (itemToEdit == null) {
                        
                        String id = shoppingRef.push().getKey();
                        if (id == null) {
                            Toast.makeText(this, "Failed to add item. Try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ShoppingItem newItem = new ShoppingItem(name, qty);
                        shoppingRef.child(id).setValue(newItem);
                    } else {
                        itemToEdit.setName(name);
                        itemToEdit.setQuantity(qty);
                        shoppingRef.child(itemToEdit.getId()).setValue(itemToEdit);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
