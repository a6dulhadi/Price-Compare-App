package com.pricecompare.app.game;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pricecompare.app.R;

public class GameMenuActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "memory_matching_prefs";
    public static final String KEY_HIGH_SCORE = "high_score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        TextView tvHighScore = findViewById(R.id.tvHighScore);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnHome = findViewById(R.id.btnHome);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvHighScore.setText(highScore > 0 ? "Peak Score: " + highScore : "Peak Score: --");

        btnPlay.setOnClickListener(v ->
                startActivity(new Intent(GameMenuActivity.this, GameActivity.class)));

        btnHome.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        TextView tvHighScore = findViewById(R.id.tvHighScore);
        tvHighScore.setText(highScore > 0 ? "Peak Score: " + highScore : "Peak Score: --");
    }
}
