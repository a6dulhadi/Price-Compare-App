package com.pricecompare.app.game.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pricecompare.app.R;
import com.pricecompare.app.game.GameSettings;

public class LevelFragment extends Fragment {

    private RecyclerView rvLevels;
    private LevelAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLevels = view.findViewById(R.id.rvLevels);
        rvLevels.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        
        adapter = new LevelAdapter(GameSettings.Level.values());
        rvLevels.setAdapter(adapter);
    }

    private class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.ViewHolder> {
        private final GameSettings.Level[] levels;

        public LevelAdapter(GameSettings.Level[] levels) {
            this.levels = levels;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GameSettings.Level level = levels[position];
            holder.tvName.setText(level.displayName.toUpperCase());
            holder.tvCards.setText(level.totalCards() + " Cards");
            holder.tvMoves.setText(level.maxMoves + " moves");
            holder.tvTime.setText(level.timeSeconds + "s");
            
            String icon = position == 0 ? "🌱" : (position == 1 ? "🔥" : "💀");
            holder.tvIcon.setText(icon);

            GameSettings.Level currentLevel = GameSettings.getLevel(requireContext());
            GameSettings.GameMode currentMode = GameSettings.getGameMode(requireContext());
            
            boolean isLevelSelected = currentLevel == level;
            
            holder.btnTime.setBackgroundResource(isLevelSelected && currentMode == GameSettings.GameMode.TIME_LIMIT 
                    ? R.drawable.bg_card_selected : R.drawable.bg_card);
            holder.btnMoves.setBackgroundResource(isLevelSelected && currentMode == GameSettings.GameMode.MOVES_LIMIT 
                    ? R.drawable.bg_card_selected : R.drawable.bg_card);
            
            holder.tvStatus.setVisibility(isLevelSelected ? View.VISIBLE : View.INVISIBLE);
            holder.tvStatus.setText("ACTIVE");

            holder.btnTime.setOnClickListener(v -> {
                GameSettings.setLevel(requireContext(), level);
                GameSettings.setGameMode(requireContext(), GameSettings.GameMode.TIME_LIMIT);
                notifyDataSetChanged();
                Toast.makeText(requireContext(), level.displayName + " (Time Limit) selected!", Toast.LENGTH_SHORT).show();
            });

            holder.btnMoves.setOnClickListener(v -> {
                GameSettings.setLevel(requireContext(), level);
                GameSettings.setGameMode(requireContext(), GameSettings.GameMode.MOVES_LIMIT);
                notifyDataSetChanged();
                Toast.makeText(requireContext(), level.displayName + " (Moves Limit) selected!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return levels.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvName, tvCards, tvMoves, tvTime, tvStatus;
            View btnTime, btnMoves;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tvLevelIcon);
                tvName = itemView.findViewById(R.id.tvLevelName);
                tvCards = itemView.findViewById(R.id.tvLevelCards);
                tvMoves = itemView.findViewById(R.id.tvLevelMoves);
                tvTime = itemView.findViewById(R.id.tvLevelTime);
                tvStatus = itemView.findViewById(R.id.tvSelectionStatus);
                btnTime = itemView.findViewById(R.id.btnModeTime);
                btnMoves = itemView.findViewById(R.id.btnModeMoves);
            }
        }
    }
}
