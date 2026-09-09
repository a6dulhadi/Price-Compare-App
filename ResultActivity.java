package com.pricecompare.app.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pricecompare.app.R;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_WON = "extra_won";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_HIGH_SCORE = "extra_high_score";
    public static final String EXTRA_LEVEL_NAME = "extra_level_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean won = getIntent().getBooleanExtra(EXTRA_WON, false);

        if (GameSettings.isDarkMode(this)) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        }

        setContentView(R.layout.activity_result);

        TextView tvPlayerGreeting = findViewById(R.id.tvPlayerGreeting);
        View root               = findViewById(R.id.rootResult);
        TextView tvTitle        = findViewById(R.id.tvResultTitle);
        TextView tvIcon         = findViewById(R.id.tvIcon);
        TextView tvLevel        = findViewById(R.id.tvLevelLabel);
        TextView tvScore        = findViewById(R.id.tvScore);
        TextView tvBest         = findViewById(R.id.tvPeakScore);
        View scoreContainer     = findViewById(R.id.scoreContainer);
        Button btnPlayAgain     = findViewById(R.id.btnPlayAgain);
        Button btnHome          = findViewById(R.id.btnHome);

        int    scoreValue = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int    bestValue  = getIntent().getIntExtra(EXTRA_HIGH_SCORE, 0);
        String levelName  = getIntent().getStringExtra(EXTRA_LEVEL_NAME);

        if (won) {
            tvTitle.setText("Congratulations!\nYou won the game");
            tvIcon.setText("🏆");
            root.setBackgroundResource(R.drawable.bg_win_gradient);
        } else {
            tvTitle.setText("Oops!\nTry harder next time.");
            tvIcon.setText("❌");
            root.setBackgroundResource(R.drawable.bg_lose_gradient);
        }

        tvLevel.setText("Level: " + levelName);
        tvScore.setText("Score: " + scoreValue);
        tvBest.setText("Best Score: " + bestValue);

        
        
        String gameName = GameSettings.getProfileName(this);
        if (gameName == null || gameName.isEmpty()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                gameName = user.getDisplayName();
            } else {
                gameName = "PLAYER";
            }
        }
        tvPlayerGreeting.setText("HELLO " + gameName.toUpperCase());
        tvPlayerGreeting.setVisibility(View.VISIBLE);

        btnPlayAgain.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, GameActivity.class));
            finish();
        });
        btnHome.setOnClickListener(v -> finish());

        
        tvPlayerGreeting.setAlpha(0);
        tvPlayerGreeting.setTranslationY(-50f);
        tvPlayerGreeting.animate().alpha(1).translationY(0).setDuration(600).start();

        tvIcon.setAlpha(0);
        tvIcon.setTranslationY(-100f);
        tvIcon.animate().alpha(1).translationY(0).setDuration(700).setStartDelay(200).start();

        tvTitle.setAlpha(0);
        tvTitle.setScaleX(0.7f);
        tvTitle.setScaleY(0.7f);
        tvTitle.animate().alpha(1).scaleX(1).scaleY(1).setDuration(600).setStartDelay(400).start();

        scoreContainer.setAlpha(0);
        scoreContainer.setTranslationY(50f);
        scoreContainer.animate().alpha(1).translationY(0).setDuration(600).setStartDelay(600).start();

        btnPlayAgain.setAlpha(0);
        btnPlayAgain.animate().alpha(1).setDuration(500).setStartDelay(900).start();

        btnHome.setAlpha(0);
        btnHome.animate().alpha(1).setDuration(500).setStartDelay(1100).start();
    }
}
