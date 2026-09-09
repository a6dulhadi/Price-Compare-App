package com.pricecompare.app.game.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.pricecompare.app.R;
import com.pricecompare.app.game.GameSettings;

public class ThemeFragment extends Fragment {

    private SwitchCompat switchDarkMode;
    private SeekBar seekBgMusic, seekClickSound;
    private LinearLayout cardFruits, cardAnimals, cardSpace;
    private View colorGreen, colorOrange, colorPurple, colorTeal, colorPink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        seekBgMusic = view.findViewById(R.id.seekBgMusic);
        seekClickSound = view.findViewById(R.id.seekClickSound);
        cardFruits = view.findViewById(R.id.cardFruits);
        cardAnimals = view.findViewById(R.id.cardAnimals);
        cardSpace = view.findViewById(R.id.cardSpace);
        colorGreen = view.findViewById(R.id.colorGreen);
        colorOrange = view.findViewById(R.id.colorOrange);
        colorPurple = view.findViewById(R.id.colorPurple);
        colorTeal = view.findViewById(R.id.colorTeal);
        colorPink = view.findViewById(R.id.colorPink);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        switchDarkMode.setChecked(GameSettings.isDarkMode(requireContext()));
        seekBgMusic.setProgress(GameSettings.getBgVolume(requireContext()));
        seekClickSound.setProgress(GameSettings.getSfxVolume(requireContext()));
        highlightTheme();
    }

    private void setupListeners() {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            GameSettings.setDarkMode(requireContext(), isChecked);
            
            
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }

            Toast.makeText(requireContext(), "Theme updated!", Toast.LENGTH_SHORT).show();
        });

        seekBgMusic.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) GameSettings.setBgVolume(requireContext(), progress);
            }
        });

        seekClickSound.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) GameSettings.setSfxVolume(requireContext(), progress);
            }
        });

        cardFruits.setOnClickListener(v -> selectTheme(GameSettings.Theme.FRUITS));
        cardAnimals.setOnClickListener(v -> selectTheme(GameSettings.Theme.ANIMALS));
        cardSpace.setOnClickListener(v -> selectTheme(GameSettings.Theme.SPACE));

        colorGreen.setOnClickListener(v -> selectColor("#2E7D32"));
        colorOrange.setOnClickListener(v -> selectColor("#EF6C00"));
        colorPurple.setOnClickListener(v -> selectColor("#4527A0"));
        colorTeal.setOnClickListener(v -> selectColor("#00838F"));
        colorPink.setOnClickListener(v -> selectColor("#C2185B"));
    }

    private void selectTheme(GameSettings.Theme theme) {
        GameSettings.setTheme(requireContext(), theme);
        highlightTheme();
        Toast.makeText(requireContext(), theme.displayName + " symbols selected", Toast.LENGTH_SHORT).show();
    }

    private void selectColor(String hex) {
        GameSettings.setCardColor(requireContext(), hex);
        Toast.makeText(requireContext(), "Card color updated!", Toast.LENGTH_SHORT).show();
    }

    private void highlightTheme() {
        GameSettings.Theme current = GameSettings.getTheme(requireContext());
        cardFruits.setBackgroundResource(current == GameSettings.Theme.FRUITS ? R.drawable.bg_card_selected : R.drawable.bg_card);
        cardAnimals.setBackgroundResource(current == GameSettings.Theme.ANIMALS ? R.drawable.bg_card_selected : R.drawable.bg_card);
        cardSpace.setBackgroundResource(current == GameSettings.Theme.SPACE ? R.drawable.bg_card_selected : R.drawable.bg_card);
    }

    private static abstract class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
