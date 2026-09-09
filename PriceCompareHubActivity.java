package com.pricecompare.app.pricecompare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.pricecompare.app.ProfileActivity;
import com.pricecompare.app.R;
import com.pricecompare.app.ShoppingListActivity;

public class PriceCompareHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_compare_hub);

        ImageButton btnBack = findViewById(R.id.btnBack);
        View cardProfile = findViewById(R.id.cardProfile);
        View cardShoppingList = findViewById(R.id.cardShoppingList);
        View cardPriceCompare = findViewById(R.id.cardPriceCompare);

        btnBack.setOnClickListener(v -> finish());

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        cardShoppingList.setOnClickListener(v ->
                startActivity(new Intent(this, ShoppingListActivity.class)));

        cardPriceCompare.setOnClickListener(v ->
                startActivity(new Intent(this, ProductListActivity.class)));
    }
}
