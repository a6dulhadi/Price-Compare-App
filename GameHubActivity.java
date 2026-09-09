package com.pricecompare.app.game;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pricecompare.app.R;
import com.pricecompare.app.game.fragments.GameFragment;
import com.pricecompare.app.game.fragments.LevelFragment;
import com.pricecompare.app.game.fragments.ProfileFragment;
import com.pricecompare.app.game.fragments.ThemeFragment;

public class GameHubActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TAB = "selected_tab";
    private int selectedTabId = R.id.nav_game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        boolean isDark = com.pricecompare.app.game.GameSettings.isDarkMode(this);
        int expectedMode = isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                                  : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != expectedMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(expectedMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_hub);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_game);
            bottomNav.setSelectedItemId(selectedTabId);
        } else {
            loadFragment(new GameFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            selectedTabId = item.getItemId();
            Fragment fragment = null;
            
            if (selectedTabId == R.id.nav_level) {
                fragment = new LevelFragment();
            } else if (selectedTabId == R.id.nav_theme) {
                fragment = new ThemeFragment();
            } else if (selectedTabId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (selectedTabId == R.id.nav_game) {
                fragment = new GameFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
