package com.pricecompare.app.pricecompare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pricecompare.app.R;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entryId";
    public static final String EXTRA_PRODUCT_NAME = "productName";
    public static final String EXTRA_STORE_NAME = "storeName";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_NOTES = "notes";

    private EditText etProductName, etStoreName, etPrice, etNotes;
    private TextView tvError;
    private DatabaseReference productsRef;
    private String entryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        productsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("products");

        etProductName = findViewById(R.id.etProductName);
        etStoreName = findViewById(R.id.etStoreName);
        etPrice = findViewById(R.id.etPrice);
        etNotes = findViewById(R.id.etNotes);
        tvError = findViewById(R.id.tvError);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);

        entryId = getIntent().getStringExtra(EXTRA_ENTRY_ID);
        etProductName.setText(getIntent().getStringExtra(EXTRA_PRODUCT_NAME));
        etStoreName.setText(getIntent().getStringExtra(EXTRA_STORE_NAME));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra(EXTRA_PRICE, 0)));
        etNotes.setText(getIntent().getStringExtra(EXTRA_NOTES));

        btnBack.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String productName = etProductName.getText().toString().trim();
        String storeName = etStoreName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(storeName) || TextUtils.isEmpty(priceStr)) {
            showError("Product name, store name and price are required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showError("Please enter a valid price.");
            return;
        }

        if (entryId == null) {
            showError("Could not find this record.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("productName", productName);
        updates.put("storeName", storeName);
        updates.put("price", price);
        updates.put("notes", notes);

        productsRef.child(entryId).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> showError("Failed to save: " + e.getMessage()));
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
