package com.pricecompare.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.game.GameSettings;
import com.pricecompare.app.models.UserProfile;
import com.pricecompare.app.pricecompare.ProductAdapter;
import com.pricecompare.app.pricecompare.ProductDetailActivity;
import com.pricecompare.app.pricecompare.ProductGroup;
import com.pricecompare.app.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etAge;
    private TextView tvEmail, tvNoFavourites;
    private ImageView ivProfilePic;
    private Switch switchDarkMode;
    private RecyclerView rvFavourites;
    private ProductAdapter favAdapter;
    private final List<ProductGroup> favGroups = new ArrayList<>();

    private DatabaseReference userRef;
    private String uid;

    // FIX: store the selected local URI (same approach as the game's ProfileFragment)
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // FIX: take persistable permission so the URI survives app restarts
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}

                    selectedImageUri = uri;

                    // Show immediately (local URI, no network needed — same as game)
                    ivProfilePic.setImageURI(uri);

                    // Persist locally right away so it survives without pressing Save
                    GameSettings.setProfileImage(this, uri.toString());

                    // Also update the DB profilePicUrl with the local URI string so
                    // loadProfile() won't overwrite it on next open
                    userRef.child("profile").child("profilePicUrl")
                            .setValue(uri.toString());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        etName        = findViewById(R.id.etName);
        etAge         = findViewById(R.id.etAge);
        tvEmail       = findViewById(R.id.tvEmail);
        tvNoFavourites= findViewById(R.id.tvNoFavourites);
        ivProfilePic  = findViewById(R.id.ivProfilePic);
        switchDarkMode= findViewById(R.id.switchDarkMode);
        rvFavourites  = findViewById(R.id.rvFavourites);

        ImageButton btnBack     = findViewById(R.id.btnBack);
        ImageButton btnEditPic  = findViewById(R.id.btnEditPic);
        Button btnSaveProfile   = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish());
        btnEditPic.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        switchDarkMode.setChecked(ThemeManager.isDarkMode(this));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                ThemeManager.setDarkMode(this, isChecked);
            }
        });

        rvFavourites.setLayoutManager(new LinearLayoutManager(this));
        favAdapter = new ProductAdapter(favGroups, new ProductAdapter.Listener() {
            @Override
            public void onView(ProductGroup group) {
                Intent intent = new Intent(ProfileActivity.this, ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_NAME, group.getProductName());
                startActivity(intent);
            }
            @Override
            public void onDelete(ProductGroup group) {
                removeFromFavourites(group);
            }
        });
        rvFavourites.setAdapter(favAdapter);

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadProfile();
        loadFavourites();
    }

    private void loadProfile() {
        // FIX: Try local SharedPrefs first (instant, no network) — same source as game
        String localImageUri = GameSettings.getProfileImage(this);
        if (!localImageUri.isEmpty()) {
            try {
                ivProfilePic.setImageURI(Uri.parse(localImageUri));
            } catch (Exception e) {
                ivProfilePic.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Then load text fields from Firebase
        userRef.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile profile = snapshot.getValue(UserProfile.class);
                if (profile != null) {
                    etName.setText(profile.getName());
                    etAge.setText(String.valueOf(profile.getAge() == 0 ? "" : profile.getAge()));
                    tvEmail.setText(profile.getEmail());

                    // FIX: Only load the DB picture URL if we have no local copy saved.
                    // The local copy is always preferred because it doesn't depend on
                    // Firebase Storage security rules or network availability.
                    String dbPicUrl = profile.getProfilePicUrl() != null ? profile.getProfilePicUrl() : "";
                    if (localImageUri.isEmpty() && !dbPicUrl.isEmpty()) {
                        try {
                            ivProfilePic.setImageURI(Uri.parse(dbPicUrl));
                        } catch (Exception e) {
                            ivProfilePic.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveProfile() {
        String name   = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String email  = tvEmail.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = 0;
        try { age = Integer.parseInt(ageStr); } catch (NumberFormatException ignored) {}

        // Update Firebase Auth display name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profileUpdates);
        }

        // FIX: Use the local URI string as the pic URL — consistent with game's approach
        String picUriString = selectedImageUri != null
                ? selectedImageUri.toString()
                : GameSettings.getProfileImage(this);

        UserProfile profile = new UserProfile(name, age, email, picUriString);

        // Sync to SharedPrefs so game fragment also picks up the latest name/age
        GameSettings.setProfileName(this, name);
        GameSettings.setProfileAge(this, ageStr);
        if (!picUriString.isEmpty()) {
            GameSettings.setProfileImage(this, picUriString);
        }

        userRef.child("profile").setValue(profile)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show());
    }

    private void loadFavourites() {
        userRef.child("favourites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favGroups.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ProductGroup group = child.getValue(ProductGroup.class);
                    if (group != null) favGroups.add(group);
                }
                favAdapter.notifyDataSetChanged();
                tvNoFavourites.setVisibility(favGroups.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void removeFromFavourites(ProductGroup group) {
        userRef.child("favourites").child(group.getProductName().toLowerCase()).removeValue();
    }
}
