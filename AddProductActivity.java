package com.pricecompare.app.pricecompare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private AutoCompleteTextView etProductName, etStoreName;
    private EditText etPrice, etNotes;
    private android.widget.Spinner spinnerCategory;
    private TextView tvError, tvScannedBarcode;
    private ImageView ivProductPhoto;
    private DatabaseReference productsRef;
    private String scannedBarcode = "";
    private Bitmap capturedImage;

    private static final int CAMERA_PERMISSION_CODE = 100;

    
    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String barcode = result.getData().getStringExtra(BarcodeScanActivity.EXTRA_BARCODE);
                    if (barcode != null) onBarcodeScanned(barcode);
                }
            });

    
    private final ActivityResultLauncher<Intent> photoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.os.Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data");
                        ivProductPhoto.setImageBitmap(capturedImage);
                        ivProductPhoto.setVisibility(View.VISIBLE);
                    }
                }
            });

    private static final String[] CATEGORIES =
            {"Groceries", "Electronics", "Toiletries", "Clothing", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        productsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("products");

        etProductName    = findViewById(R.id.etProductName);
        etStoreName      = findViewById(R.id.etStoreName);
        etPrice          = findViewById(R.id.etPrice);
        etNotes          = findViewById(R.id.etNotes);
        spinnerCategory  = findViewById(R.id.spinnerCategory);
        tvError          = findViewById(R.id.tvError);
        tvScannedBarcode = findViewById(R.id.tvScannedBarcode);
        ivProductPhoto   = findViewById(R.id.ivProductPhoto);

        String prefill = getIntent().getStringExtra("prefillProductName");
        if (prefill != null && !prefill.isEmpty()) {
            etProductName.setText(prefill);
        }

        ImageButton btnBack         = findViewById(R.id.btnBack);
        Button      btnCancel       = findViewById(R.id.btnCancel);
        Button      btnSave         = findViewById(R.id.btnSave);
        ImageButton btnScanBarcode  = findViewById(R.id.btnScanBarcode);
        ImageButton btnCapturePhoto = findViewById(R.id.btnCapturePhoto);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        setupAutoComplete();

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());

        btnScanBarcode.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openScanner();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        });

        btnCapturePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE + 1);
            }
        });
    }

    private void setupAutoComplete() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<String> productNames = new HashSet<>();
                Set<String> storeNames   = new HashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PriceEntry entry = child.getValue(PriceEntry.class);
                    if (entry == null) continue;
                    if (entry.getProductName() != null) productNames.add(entry.getProductName());
                    if (entry.getStoreName()   != null) storeNames.add(entry.getStoreName());
                }
                ArrayAdapter<String> productAdapter = new ArrayAdapter<>(AddProductActivity.this,
                        android.R.layout.simple_dropdown_item_1line, new ArrayList<>(productNames));
                etProductName.setAdapter(productAdapter);

                ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(AddProductActivity.this,
                        android.R.layout.simple_dropdown_item_1line, new ArrayList<>(storeNames));
                etStoreName.setAdapter(storeAdapter);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoLauncher.launch(intent);
    }

    private void openScanner() {
        scanLauncher.launch(new Intent(this, BarcodeScanActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openScanner();
            } else {
                Toast.makeText(this, "Camera permission required for barcode scan",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE + 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required for photo",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onBarcodeScanned(String barcode) {
        this.scannedBarcode = barcode;
        tvScannedBarcode.setText("Barcode: " + barcode);
        tvScannedBarcode.setVisibility(View.VISIBLE);
        fetchProductByBarcode(barcode);
    }

    private void fetchProductByBarcode(String barcode) {
        productsRef.orderByChild("barcode").equalTo(barcode).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                PriceEntry entry = child.getValue(PriceEntry.class);
                                if (entry != null) autoFillProduct(entry);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void autoFillProduct(PriceEntry entry) {
        etProductName.setText(entry.getProductName());
        etStoreName.setText(entry.getStoreName());
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(entry.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        Toast.makeText(this, "Auto-filled from previous entry", Toast.LENGTH_SHORT).show();
    }

    private void saveProduct() {
        String productName = etProductName.getText().toString().trim();
        String storeName   = etStoreName.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String notes       = etNotes.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(storeName)
                || TextUtils.isEmpty(priceStr)) {
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

        if (capturedImage != null) {
            saveImageLocallyAndSaveProduct(productName, storeName, price, notes, category);
        } else {
            finalizeSave(productName, storeName, price, notes, category, null);
        }
    }

    private void saveImageLocallyAndSaveProduct(String productName, String storeName,
                                                 double price, String notes, String category) {
        String fileName = "IMG_" + UUID.randomUUID() + ".jpg";
        File directory  = new File(getFilesDir(), "product_images");
        if (!directory.exists()) directory.mkdirs();

        File imageFile = new File(directory, fileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            finalizeSave(productName, storeName, price, notes, category, imageFile.getAbsolutePath());
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image locally", Toast.LENGTH_SHORT).show();
            finalizeSave(productName, storeName, price, notes, category, null);
        }
    }

    private void finalizeSave(String productName, String storeName, double price,
                               String notes, String category, String imageUrl) {
        PriceEntry entry = new PriceEntry(productName, storeName, price,
                notes, category, scannedBarcode, imageUrl);
        String key = productsRef.push().getKey();
        if (key == null) {
            showError("Could not save. Please try again.");
            return;
        }
        productsRef.child(key).setValue(entry)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> showError("Failed to save: " + e.getMessage()));
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
