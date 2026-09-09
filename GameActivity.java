package com.pricecompare.app.game;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pricecompare.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private GameSettings.Level level;
    private GameSettings.Theme theme;
    private String cardColorHex;

    private GridLayout gridCards;
    private TextView tvScore, tvMoves, tvTime;
    private ProgressBar progressTime;

    private final List<String> cardValues = new ArrayList<>();
    private final List<Button> cardButtons = new ArrayList<>();
    private boolean[] matched;

    private Integer firstSelectedIndex = null;
    private boolean inputLocked = false;

    private int score = 0;
    private int matchedPairs = 0;
    private int movesLeft;
    private int totalTimeSeconds;
    private int secondsLeft;
    private GameSettings.GameMode mode;

    private CountDownTimer countDownTimer;
    private boolean gameEnded = false;

    private SoundPool soundPool;
    private int flipSoundId, matchSoundId, failSoundId;
    private MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (GameSettings.isDarkMode(this)) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        }

        setContentView(R.layout.activity_game);

        level = GameSettings.getLevel(this);
        mode = GameSettings.getGameMode(this);
        theme = GameSettings.getTheme(this);
        cardColorHex = GameSettings.getCardColor(this);

        gridCards = findViewById(R.id.gridCards);
        tvScore = findViewById(R.id.tvScore);
        tvMoves = findViewById(R.id.tvMoves);
        tvTime = findViewById(R.id.tvTime);
        progressTime = findViewById(R.id.progressTime);
        Button btnStop = findViewById(R.id.btnStop);

        btnStop.setOnClickListener(v -> endGame(false));

        movesLeft = level.maxMoves;
        totalTimeSeconds = level.timeSeconds;
        secondsLeft = totalTimeSeconds;
        matched = new boolean[level.totalCards()];

        setupAudio();
        updateScore();
        
        if (mode == GameSettings.GameMode.TIME_LIMIT) {
            tvMoves.setVisibility(View.GONE);
            tvTime.setVisibility(View.VISIBLE);
            progressTime.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            tvTime.setVisibility(View.GONE);
            progressTime.setVisibility(View.GONE);
            tvMoves.setVisibility(View.VISIBLE);
            tvMoves.setGravity(android.view.Gravity.END); 
        }
        
        updateMoves();
        setupCards();
        
        
        gridCards.setAlpha(0f);
        gridCards.animate().alpha(1f).setDuration(500).start();
    }

    private void setupAudio() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build();
        
        
        flipSoundId = soundPool.load(this, R.raw.card_flip, 1);
        matchSoundId = soundPool.load(this, R.raw.card_match, 1);
        failSoundId = soundPool.load(this, R.raw.card_fail, 1);
        
        
        float bgVol = GameSettings.getBgVolume(this) / 100f;
        try {
            backgroundMusic = MediaPlayer.create(this, R.raw.game_background_music);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(bgVol, bgVol);
                backgroundMusic.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSfx(int soundId) {
        float sfxVol = GameSettings.getSfxVolume(this) / 100f;
        if (sfxVol > 0 && soundPool != null) {
            soundPool.play(soundId, sfxVol, sfxVol, 0, 0, 1f);
        }
    }

    private void setupCards() {
        cardValues.clear();
        for (int i = 0; i < level.pairs; i++) {
            String symbol = theme.symbols[i % theme.symbols.length];
            cardValues.add(symbol);
            cardValues.add(symbol);
        }
        Collections.shuffle(cardValues);

        gridCards.removeAllViews();
        gridCards.setColumnCount(level.columns);
        gridCards.setRowCount(level.rows);
        cardButtons.clear();

        int cardColor = Color.parseColor(cardColorHex);

        for (int i = 0; i < level.totalCards(); i++) {
            final int index = i;
            Button card = new Button(this);
            card.setText("?");
            
            
            float textSize = 24f; 
            if (level == GameSettings.Level.MEDIUM) textSize = 20f;
            if (level == GameSettings.Level.HARD) textSize = 16f;
            card.setTextSize(textSize);
            
            card.setBackgroundResource(R.drawable.bg_game_card_hidden);
            card.setBackgroundTintList(ColorStateList.valueOf(cardColor));
            card.setTextColor(Color.WHITE);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % level.columns, 1f);
            params.rowSpec = GridLayout.spec(i / level.columns, 1f);
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);

            card.setOnClickListener(v -> onCardClicked(index));

            gridCards.addView(card);
            cardButtons.add(card);
        }
    }

    private void onCardClicked(int index) {
        if (inputLocked || gameEnded || matched[index]) return;
        if (firstSelectedIndex != null && firstSelectedIndex == index) return;

        flipReveal(index);
        playSfx(flipSoundId);

        if (firstSelectedIndex == null) {
            firstSelectedIndex = index;
            return;
        }

        int first = firstSelectedIndex;
        firstSelectedIndex = null;

        movesLeft--;
        updateMoves();

        if (mode == GameSettings.GameMode.MOVES_LIMIT && movesLeft < 0) {
            return; 
        }

        if (cardValues.get(first).equals(cardValues.get(index))) {
            handleMatch(first, index);
        } else {
            handleMismatch(first, index);
        }
    }

    private void handleMatch(int first, int index) {
        matched[first] = true;
        matched[index] = true;
        animateMatch(cardButtons.get(first));
        animateMatch(cardButtons.get(index));
        playSfx(matchSoundId);
        score += 10;
        matchedPairs++;
        updateScore();

        if (matchedPairs == level.pairs) {
            gridCards.postDelayed(() -> endGame(true), 500);
        }
    }

    private void handleMismatch(int first, int index) {
        inputLocked = true;
        shake(cardButtons.get(first));
        shake(cardButtons.get(index));
        playSfx(failSoundId);
        
        gridCards.postDelayed(() -> {
            flipHide(first);
            flipHide(index);
            inputLocked = false;
            if (mode == GameSettings.GameMode.MOVES_LIMIT && movesLeft <= 0 && matchedPairs < level.pairs) {
                endGame(false);
            }
        }, 700);
    }

    private void flipReveal(int index) {
        Button card = cardButtons.get(index);
        card.animate().rotationY(90f).setDuration(150).setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    card.setText(cardValues.get(index));
                    card.setBackgroundResource(R.drawable.bg_game_card_shown);
                    card.setBackgroundTintList(null);
                    card.setTextColor(Color.BLACK);
                    card.setRotationY(-90f);
                    card.animate().rotationY(0f).setDuration(150)
                            .setInterpolator(new DecelerateInterpolator()).start();
                }).start();
    }

    private void flipHide(int index) {
        if (matched[index]) return;
        Button card = cardButtons.get(index);
        int cardColor = Color.parseColor(cardColorHex);
        card.animate().rotationY(90f).setDuration(150).setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    card.setText("?");
                    card.setBackgroundResource(R.drawable.bg_game_card_hidden);
                    card.setBackgroundTintList(ColorStateList.valueOf(cardColor));
                    card.setTextColor(Color.WHITE);
                    card.setRotationY(-90f);
                    card.animate().rotationY(0f).setDuration(150)
                            .setInterpolator(new DecelerateInterpolator()).start();
                }).start();
    }

    private void animateMatch(Button card) {
        card.setBackgroundResource(R.drawable.bg_game_card_matched);
        card.setBackgroundTintList(null);
        card.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                .withEndAction(() -> card.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start())
                .start();
    }

    private void shake(Button card) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(card, "translationX", 0f, -15f, 15f, -10f, 10f, 0f);
        anim.setDuration(400);
        anim.start();
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void updateMoves() {
        tvMoves.setText("Limit: " + Math.max(movesLeft, 0));
        if (movesLeft <= 5) tvMoves.setTextColor(Color.RED);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(secondsLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsLeft = (int) (millisUntilFinished / 1000);
                tvTime.setText("Time: " + secondsLeft + "s");
                progressTime.setProgress(Math.round((secondsLeft * 100f) / totalTimeSeconds));
                if (secondsLeft <= 10) progressTime.setProgressTintList(ColorStateList.valueOf(Color.RED));
            }

            @Override
            public void onFinish() {
                endGame(matchedPairs == level.pairs);
            }
        }.start();
    }

    private void endGame(boolean won) {
        if (gameEnded) return;
        gameEnded = true;
        inputLocked = true;
        if (countDownTimer != null) countDownTimer.cancel();
        
        GameSettings.recordGameResult(this, won);
        GameSettings.reportScore(this, level, score);
        
        
        String historyLabel = level.displayName + " (" + (mode == GameSettings.GameMode.TIME_LIMIT ? "Time" : "Moves") + ")";
        String historyEntry = historyLabel + "," + (won ? "WIN" : "LOSE") + "," + score + "," + (totalTimeSeconds - secondsLeft);
        GameSettings.addGameToHistory(this, historyEntry);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_WON, won);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_HIGH_SCORE, GameSettings.getHighScore(this, level));
        intent.putExtra(ResultActivity.EXTRA_LEVEL_NAME, level.displayName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
        }
        if (soundPool != null) soundPool.release();
    }
}
