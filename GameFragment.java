package com.pricecompare.app.game.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pricecompare.app.R;
import com.pricecompare.app.game.GameActivity;
import com.pricecompare.app.game.GameSettings;

public class GameFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnPlay = view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), GameActivity.class)));

        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        refreshSummary(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            refreshSummary(getView());
        }
    }

    private void refreshSummary(View view) {
        TextView tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel);
        TextView tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        TextView tvHighScore = view.findViewById(R.id.tvHighScore);

        GameSettings.Level level = GameSettings.getLevel(requireContext());
        GameSettings.Theme theme = GameSettings.getTheme(requireContext());
        int highScore = GameSettings.getHighScore(requireContext(), level);

        tvCurrentLevel.setText("Level: " + level.displayName);
        tvCurrentTheme.setText("Theme: " + theme.displayName);
        tvHighScore.setText(highScore > 0
                ? "Best Score (" + level.displayName + "): " + highScore
                : "Best Score: --");
    }
}
