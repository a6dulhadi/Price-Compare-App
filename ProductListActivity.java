package com.pricecompare.app.pricecompare;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.Listener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private TextView tvEmpty;
    private EditText etSearch;
    private Spinner spinnerFilterCategory;

    private DatabaseReference productsRef;
    private ValueEventListener dbListener;

    private final List<ProductGroup> allGroups = new ArrayList<>();
    private final List<ProductGroup> filteredGroups = new ArrayList<>();

    private enum SortBy { NAME, PRICE_LOW, PRICE_HIGH, DATE_NEW }
    private SortBy currentSort = SortBy.NAME;
    private String currentCategory = "All";

    private static final String[] FILTER_CATEGORIES = {"All", "Groceries", "Electronics", "Toiletries", "Clothing", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        productsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("products");

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnAdd = findViewById(R.id.btnAdd);
        ImageButton btnSort = findViewById(R.id.btnSort);
        ImageButton btnShare = findViewById(R.id.btnShare);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(filteredGroups, this);
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FILTER_CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(catAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(ProductListActivity.this, AddProductActivity.class)));

        btnSort.setOnClickListener(v -> showSortDialog());
        btnShare.setOnClickListener(v -> shareBestDeals());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFiltersAndSort(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = FILTER_CATEGORIES[position];
                applyFiltersAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showSortDialog() {
        String[] options = {"Name (A-Z)", "Price (Lowest First)", "Price (Highest First)", "Date (Newest First)"};
        new AlertDialog.Builder(this)
                .setTitle("Sort Products")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSort = SortBy.NAME; break;
                        case 1: currentSort = SortBy.PRICE_LOW; break;
                        case 2: currentSort = SortBy.PRICE_HIGH; break;
                        case 3: currentSort = SortBy.DATE_NEW; break;
                    }
                    applyFiltersAndSort();
                })
                .show();
    }

    private void shareBestDeals() {
        if (allGroups.isEmpty()) {
            Toast.makeText(this, "No products to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔥 PriceCompare: Best Deals List 🔥\n\n");
        for (ProductGroup group : allGroups) {
            PriceEntry lowest = group.getLowestEntry();
            if (lowest != null) {
                sb.append("✅ ").append(group.getProductName()).append("\n");
                sb.append("   Price: RM").append(String.format("%.2f", lowest.getPrice())).append("\n");
                sb.append("   Store: ").append(lowest.getStoreName()).append("\n\n");
            }
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, "Share Best Deals via"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dbListener != null) {
            productsRef.removeEventListener(dbListener);
        }
    }

    private void attachListener() {
        dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, ProductGroup> grouped = new LinkedHashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PriceEntry entry = child.getValue(PriceEntry.class);
                    if (entry == null) continue;
                    entry.setId(child.getKey());

                    String key = entry.getProductName() == null
                            ? "" : entry.getProductName().trim().toLowerCase();
                    ProductGroup group = grouped.get(key);
                    if (group == null) {
                        group = new ProductGroup(entry.getProductName());
                        grouped.put(key, group);
                    }
                    group.addEntry(entry);
                }

                allGroups.clear();
                allGroups.addAll(grouped.values());
                applyFiltersAndSort();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        };
        productsRef.addValueEventListener(dbListener);
    }

    private void applyFiltersAndSort() {
        filteredGroups.clear();
        String query = etSearch.getText().toString().trim().toLowerCase();

        for (ProductGroup g : allGroups) {
            boolean matchesSearch = query.isEmpty() ||
                    (g.getProductName() != null && g.getProductName().toLowerCase().contains(query));

            boolean matchesCategory = currentCategory.equals("All");
            if (!matchesCategory) {
                for (PriceEntry e : g.getEntries()) {
                    if (currentCategory.equals(e.getCategory())) {
                        matchesCategory = true;
                        break;
                    }
                }
            }

            if (matchesSearch && matchesCategory) {
                filteredGroups.add(g);
            }
        }

        
        Collections.sort(filteredGroups, (a, b) -> {
            PriceEntry lowestA = a.getLowestEntry();
            PriceEntry lowestB = b.getLowestEntry();
            switch (currentSort) {
                case NAME:
                    String nameA = a.getProductName() != null ? a.getProductName() : "";
                    String nameB = b.getProductName() != null ? b.getProductName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                case PRICE_LOW:
                    if (lowestA == null && lowestB == null) return 0;
                    if (lowestA == null) return 1;
                    if (lowestB == null) return -1;
                    return Double.compare(lowestA.getPrice(), lowestB.getPrice());
                case PRICE_HIGH:
                    if (lowestA == null && lowestB == null) return 0;
                    if (lowestA == null) return 1;
                    if (lowestB == null) return -1;
                    return Double.compare(lowestB.getPrice(), lowestA.getPrice());
                case DATE_NEW:
                    if (lowestA == null && lowestB == null) return 0;
                    if (lowestA == null) return 1;
                    if (lowestB == null) return -1;
                    return Long.compare(lowestB.getTimestamp(), lowestA.getTimestamp());
                default:
                    return 0;
            }
        });

        adapter.updateData(filteredGroups);
        tvEmpty.setVisibility(filteredGroups.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onView(ProductGroup group) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, group.getProductName());
        startActivity(intent);
    }

    @Override
    public void onDelete(ProductGroup group) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Remove \"" + group.getProductName() + "\" and all its price records?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (PriceEntry entry : group.getEntries()) {
                        productsRef.child(entry.getId()).removeValue();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onFavourite(ProductGroup group) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("favourites");

        favRef.child(group.getProductName().toLowerCase()).setValue(group)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAddToShoppingList(ProductGroup group) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";
        DatabaseReference shoppingRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("shopping_list");

        String id = shoppingRef.push().getKey();
        
        if (id == null) {
            Toast.makeText(this, "Failed to add item. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        com.pricecompare.app.models.ShoppingItem item =
                new com.pricecompare.app.models.ShoppingItem(group.getProductName(), "1");
        shoppingRef.child(id).setValue(item)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Shopping List", Toast.LENGTH_SHORT).show());
    }
}
