package com.pricecompare.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pricecompare.app.game.GameHubActivity;
import com.pricecompare.app.models.UserProfile;
import com.pricecompare.app.pricecompare.PriceCompareHubActivity;
import com.pricecompare.app.utils.ThemeManager;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);

        LinearLayout cardPriceCompareApp = findViewById(R.id.cardPriceCompareApp);
        LinearLayout cardGameApp = findViewById(R.id.cardGameApp);
        Button btnLogout = findViewById(R.id.btnLogout);

        cardPriceCompareApp.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, PriceCompareHubActivity.class)));

        cardGameApp.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, GameHubActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("profile")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserProfile profile = snapshot.getValue(UserProfile.class);
                            if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                                tvWelcome.setText("Welcome, " + profile.getName() + "!");
                            } else {
                                
                                String authName = user.getDisplayName();
                                tvWelcome.setText(authName != null && !authName.isEmpty()
                                        ? "Welcome, " + authName + "!"
                                        : "Welcome!");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            String authName = user.getDisplayName();
                            tvWelcome.setText(authName != null && !authName.isEmpty()
                                    ? "Welcome, " + authName + "!"
                                    : "Welcome!");
                        }
                    });
        }
    }
    
}
