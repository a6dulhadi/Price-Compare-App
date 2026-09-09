package com.pricecompare.app.pricecompare;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity implements StoreGroupAdapter.Listener {

    public static final String EXTRA_PRODUCT_NAME = "productName";

    private String productName;
    private DatabaseReference productsRef;
    private ValueEventListener dbListener;

    private RecyclerView recyclerView;
    private StoreGroupAdapter adapter;
    private final List<PriceEntry> entries       = new ArrayList<>();
    private final List<PriceEntry> sortedForAxis = new ArrayList<>();

    private TextView  tvProductName, tvLowestPrice;
    private ImageView ivProductPhoto;
    private Button    btnViewGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        productsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("products");

        tvProductName  = findViewById(R.id.tvProductName);
        tvLowestPrice  = findViewById(R.id.tvLowestPrice);
        ivProductPhoto = findViewById(R.id.ivProductPhoto);
        recyclerView   = findViewById(R.id.recyclerView);
        btnViewGraph   = findViewById(R.id.btnViewGraph);
        ImageButton btnBack     = findViewById(R.id.btnBack);
        Button      btnAddEntry = findViewById(R.id.btnAddEntry);
        Button      btnDeleteAll = findViewById(R.id.btnDeleteAll);

        tvProductName.setText(productName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoreGroupAdapter(this);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnDeleteAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Remove \"" + productName + "\" and all its price records?")
                    .setPositiveButton("Delete", (d, w) -> {
                        for (PriceEntry entry : entries) {
                            productsRef.child(entry.getId()).removeValue();
                        }
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            intent.putExtra("prefillProductName", productName);
            startActivity(intent);
        });

        btnViewGraph.setOnClickListener(v -> showGraphDialog());
    }

    private void showGraphDialog() {
        if (entries.size() < 2) {
            new AlertDialog.Builder(this)
                    .setTitle("Not Enough Data")
                    .setMessage("Add at least 2 price entries to see the price history graph.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        sortedForAxis.clear();
        sortedForAxis.addAll(entries);
        sortedForAxis.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));

        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < sortedForAxis.size(); i++) {
            chartEntries.add(new Entry(i, (float) sortedForAxis.get(i).getPrice()));
        }

        LineChart chart = new LineChart(this);
        chart.setMinimumHeight(600);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < sortedForAxis.size()) {
                    return sdf.format(new Date(sortedForAxis.get(idx).getTimestamp()));
                }
                return "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "RM%.2f", value);
            }
        });
        chart.getAxisRight().setEnabled(false);

        LineDataSet dataSet = new LineDataSet(chartEntries, "Price History");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setCircleColor(getResources().getColor(R.color.accent));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary));
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));
        chart.invalidate();

        new AlertDialog.Builder(this)
                .setTitle("Price History: " + productName)
                .setView(chart)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                entries.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PriceEntry entry = child.getValue(PriceEntry.class);
                    if (entry == null) continue;
                    entry.setId(child.getKey());
                    if (productName != null && productName.equalsIgnoreCase(entry.getProductName())) {
                        entries.add(entry);
                    }
                }
                adapter.setEntries(entries);
                updateSummary();
                updateProductPhoto();
                btnViewGraph.setVisibility(entries.size() >= 2 ? View.VISIBLE : View.GONE);
                if (entries.isEmpty()) finish();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        productsRef.addValueEventListener(dbListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dbListener != null) productsRef.removeEventListener(dbListener);
    }

    private void updateSummary() {
        double lowest = Double.MAX_VALUE;
        for (PriceEntry e : entries) {
            if (e.getPrice() < lowest) lowest = e.getPrice();
        }
        if (!entries.isEmpty()) {
            tvLowestPrice.setText(String.format(Locale.getDefault(),
                    "Lowest Price: RM%.2f", lowest));
        }
    }

    
    private void updateProductPhoto() {
        String imagePath = null;
        for (PriceEntry e : entries) {
            if (e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                imagePath = e.getImageUrl();
                break;
            }
        }
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                ivProductPhoto.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageFile)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivProductPhoto);
            } else {
                ivProductPhoto.setVisibility(View.GONE);
            }
        } else {
            ivProductPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PriceEntry entry) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra(EditProductActivity.EXTRA_ENTRY_ID,     entry.getId());
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_NAME, entry.getProductName());
        intent.putExtra(EditProductActivity.EXTRA_STORE_NAME,   entry.getStoreName());
        intent.putExtra(EditProductActivity.EXTRA_PRICE,        entry.getPrice());
        intent.putExtra(EditProductActivity.EXTRA_NOTES,        entry.getNotes());
        startActivity(intent);
    }

    @Override
    public void onDelete(PriceEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Price Record")
                .setMessage("Remove the price from \"" + entry.getStoreName() + "\"?")
                .setPositiveButton("Delete", (d, w) ->
                        productsRef.child(entry.getId()).removeValue())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
