package com.pricecompare.app.game.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pricecompare.app.R;
import com.pricecompare.app.game.GameSettings;

public class ProfileFragment extends Fragment {

    private ImageView ivProfile;
    private EditText etName, etAge;
    private TextView tvEmail, tvGamesPlayed, tvGamesWon, tvGamesLost;
    private LinearLayout historyList;
    private Uri selectedImageUri;

    private enum Filter { ALL, WINS, LOSSES }
    private Filter currentFilter = Filter.ALL;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfile.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfile = view.findViewById(R.id.ivProfile);
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvGamesPlayed = view.findViewById(R.id.tvGamesPlayed);
        tvGamesWon = view.findViewById(R.id.tvGamesWon);
        tvGamesLost = view.findViewById(R.id.tvGamesLost);
        historyList = view.findViewById(R.id.historyList);
        ImageButton btnEditPhoto = view.findViewById(R.id.btnEditPhoto);
        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        Button btnResetHistory = view.findViewById(R.id.btnResetHistory);
        LinearLayout btnFilterAll = view.findViewById(R.id.btnFilterAll);
        LinearLayout btnFilterWins = view.findViewById(R.id.btnFilterWins);
        LinearLayout btnFilterLosses = view.findViewById(R.id.btnFilterLosses);

        loadProfileData();
        populateHistory();

        btnEditPhoto.setOnClickListener(v -> getContent.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        btnResetHistory.setOnClickListener(v -> {
            GameSettings.resetGameHistory(requireContext());
            loadProfileData();
            populateHistory();
            Toast.makeText(requireContext(), "History reset!", Toast.LENGTH_SHORT).show();
        });

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = Filter.ALL;
            updateFilterUI(btnFilterAll, btnFilterWins, btnFilterLosses);
            populateHistory();
        });

        btnFilterWins.setOnClickListener(v -> {
            currentFilter = Filter.WINS;
            updateFilterUI(btnFilterAll, btnFilterWins, btnFilterLosses);
            populateHistory();
        });

        btnFilterLosses.setOnClickListener(v -> {
            currentFilter = Filter.LOSSES;
            updateFilterUI(btnFilterAll, btnFilterWins, btnFilterLosses);
            populateHistory();
        });
        
        updateFilterUI(btnFilterAll, btnFilterWins, btnFilterLosses);
    }

    private void updateFilterUI(View all, View wins, View losses) {
        all.setBackgroundResource(currentFilter == Filter.ALL ? R.drawable.bg_card_selected : R.drawable.bg_card);
        wins.setBackgroundResource(currentFilter == Filter.WINS ? R.drawable.bg_card_selected : R.drawable.bg_card);
        losses.setBackgroundResource(currentFilter == Filter.LOSSES ? R.drawable.bg_card_selected : R.drawable.bg_card);
    }

    private void loadProfileData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String savedName = GameSettings.getProfileName(requireContext());
        String savedAge = GameSettings.getProfileAge(requireContext());
        String savedImage = GameSettings.getProfileImage(requireContext());

        if (user != null) {
            tvEmail.setText(user.getEmail());
            if (savedName.isEmpty()) savedName = user.getDisplayName();
        }

        etName.setText(savedName);
        etAge.setText(savedAge);
        
        if (!savedImage.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(savedImage));
            } catch (Exception e) {
                ivProfile.setImageResource(R.mipmap.ic_launcher_round);
            }
        }

        int played = GameSettings.getGamesPlayed(requireContext());
        int won = GameSettings.getGamesWon(requireContext());
        tvGamesPlayed.setText(String.valueOf(played));
        tvGamesWon.setText(String.valueOf(won));
        tvGamesLost.setText(String.valueOf(played - won));
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        GameSettings.setProfileName(requireContext(), name);
        GameSettings.setProfileAge(requireContext(), age);
        if (selectedImageUri != null) {
            
            try {
                requireContext().getContentResolver().takePersistableUriPermission(
                        selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            GameSettings.setProfileImage(requireContext(), selectedImageUri.toString());
        }

        Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
    }

    private void populateHistory() {
        if (historyList == null) return;
        historyList.removeAllViews();
        
        Context context = getContext();
        if (context == null) return;

        String history = GameSettings.getGameHistory(context);
        if (history.isEmpty()) {
            TextView tvEmpty = new TextView(context);
            tvEmpty.setText("No games played yet.");
            tvEmpty.setPadding(20, 20, 20, 20);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray));
            historyList.addView(tvEmpty);
            return;
        }

        String[] games = history.split("\\|");
        LayoutInflater inflater = LayoutInflater.from(context);
        
        for (String game : games) {
            String[] parts = game.split(",");
            if (parts.length < 4) continue;

            boolean isWin = "WIN".equals(parts[1]);
            
            
            if (currentFilter == Filter.WINS && !isWin) continue;
            if (currentFilter == Filter.LOSSES && isWin) continue;

            View itemView = inflater.inflate(R.layout.item_game_history, historyList, false);
            TextView tvLevel = itemView.findViewById(R.id.tvLevel);
            TextView tvResult = itemView.findViewById(R.id.tvResult);
            TextView tvDetails = itemView.findViewById(R.id.tvDetails);

            tvLevel.setText(parts[0]);
            tvResult.setText(isWin ? "🏆 Win" : "❌ Lose");
            tvResult.setTextColor(androidx.core.content.ContextCompat.getColor(context, 
                    isWin ? R.color.primary : R.color.danger));

            tvDetails.setText("Score: " + parts[2] + " • Time: " + parts[3] + "s");

            historyList.addView(itemView);
        }
        
        if (historyList.getChildCount() == 0) {
            TextView tvEmpty = new TextView(context);
            tvEmpty.setText("No entries match the filter.");
            tvEmpty.setPadding(20, 20, 20, 20);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray));
            historyList.addView(tvEmpty);
        }
    }
}
