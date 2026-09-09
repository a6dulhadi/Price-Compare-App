package com.pricecompare.app.game;

import android.content.Context;
import android.content.SharedPreferences;

public class GameSettings {

    private static final String PREFS = "memory_matching_prefs";

    private static final String KEY_LEVEL = "selected_level";
    private static final String KEY_THEME = "selected_theme";
    private static final String KEY_GAMES_PLAYED = "games_played";
    private static final String KEY_GAMES_WON = "games_won";
    private static final String KEY_HIGH_SCORE_PREFIX = "high_score_";

    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_CARD_COLOR = "card_color";
    private static final String KEY_BG_VOLUME = "bg_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";

    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_PROFILE_AGE = "profile_age";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_GAME_HISTORY = "game_history";
    private static final String KEY_MODE = "selected_mode";

    

    public enum GameMode {
        TIME_LIMIT, MOVES_LIMIT
    }

    public enum Level {
        EASY("Easy", 5, 60, 15, 2, 5),     
        MEDIUM("Medium", 10, 90, 30, 4, 5),  
        HARD("Hard", 15, 120, 45, 5, 6);     

        public final String displayName;
        public final int pairs;       
        public final int timeSeconds; 
        public final int maxMoves;    
        public final int columns;
        public final int rows;

        Level(String displayName, int pairs, int timeSeconds, int maxMoves, int columns, int rows) {
            this.displayName = displayName;
            this.pairs = pairs;
            this.timeSeconds = timeSeconds;
            this.maxMoves = maxMoves;
            this.columns = columns;
            this.rows = rows;
        }

        public int totalCards() {
            return pairs * 2;
        }
    }

    

    public enum Theme {
        FRUITS("Fruits", "#2E7D32", new String[]{
                "🍎", "🍌", "🍇", "🍒", "🍉", "🍍", "🥝", "🍓",
                "🍑", "🍋", "🍈", "🍏", "🥥", "🍐", "🍅"
        }),
        ANIMALS("Animals", "#EF6C00", new String[]{
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
                "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵"
        }),
        SPACE("Space", "#4527A0", new String[]{
                "🚀", "🛸", "🌟", "🪐", "🌙", "☄", "🌌", "👽",
                "🔭", "🌞", "🌍", "⭐", "🌠", "🔥", "💫"
        }),
        OCEAN("Ocean", "#00838F", new String[]{
                "🐠", "🐬", "🐳", "🐙", "🦀", "🐚", "🦈", "🐡",
                "🦑", "🐢", "🎣", "🌊", "⚓", "🏖", "🛶"
        });

        public final String displayName;
        public final String primaryColorHex;
        public final String[] symbols;

        Theme(String displayName, String primaryColorHex, String[] symbols) {
            this.displayName = displayName;
            this.primaryColorHex = primaryColorHex;
            this.symbols = symbols;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static Level getLevel(Context context) {
        String name = prefs(context).getString(KEY_LEVEL, Level.EASY.name());
        try {
            return Level.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Level.EASY;
        }
    }

    public static void setLevel(Context context, Level level) {
        prefs(context).edit().putString(KEY_LEVEL, level.name()).apply();
    }

    public static GameMode getGameMode(Context context) {
        String name = prefs(context).getString(KEY_MODE, GameMode.TIME_LIMIT.name());
        try {
            return GameMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return GameMode.TIME_LIMIT;
        }
    }

    public static void setGameMode(Context context, GameMode mode) {
        prefs(context).edit().putString(KEY_MODE, mode.name()).apply();
    }

    public static Theme getTheme(Context context) {
        String name = prefs(context).getString(KEY_THEME, Theme.FRUITS.name());
        try {
            return Theme.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Theme.FRUITS;
        }
    }

    public static void setTheme(Context context, Theme theme) {
        prefs(context).edit().putString(KEY_THEME, theme.name()).apply();
    }

    public static int getHighScore(Context context, Level level) {
        return prefs(context).getInt(KEY_HIGH_SCORE_PREFIX + level.name(), 0);
    }

    
    public static boolean reportScore(Context context, Level level, int score) {
        int current = getHighScore(context, level);
        if (score > current) {
            prefs(context).edit().putInt(KEY_HIGH_SCORE_PREFIX + level.name(), score).apply();
            return true;
        }
        return false;
    }

    public static int getGamesPlayed(Context context) {
        return prefs(context).getInt(KEY_GAMES_PLAYED, 0);
    }

    public static int getGamesWon(Context context) {
        return prefs(context).getInt(KEY_GAMES_WON, 0);
    }

    public static void recordGameResult(Context context, boolean won) {
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.putInt(KEY_GAMES_PLAYED, getGamesPlayed(context) + 1);
        if (won) {
            editor.putInt(KEY_GAMES_WON, getGamesWon(context) + 1);
        }
        editor.apply();
    }

    public static void resetGameHistory(Context context) {
        prefs(context).edit()
                .putInt(KEY_GAMES_PLAYED, 0)
                .putInt(KEY_GAMES_WON, 0)
                .putString(KEY_GAME_HISTORY, "")
                .apply();
    }

    

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public static String getCardColor(Context context) {
        return prefs(context).getString(KEY_CARD_COLOR, "#2E7D32");
    }

    public static void setCardColor(Context context, String colorHex) {
        prefs(context).edit().putString(KEY_CARD_COLOR, colorHex).apply();
    }

    public static int getBgVolume(Context context) {
        return prefs(context).getInt(KEY_BG_VOLUME, 50);
    }

    public static void setBgVolume(Context context, int volume) {
        prefs(context).edit().putInt(KEY_BG_VOLUME, volume).apply();
    }

    public static int getSfxVolume(Context context) {
        return prefs(context).getInt(KEY_SFX_VOLUME, 70);
    }

    public static void setSfxVolume(Context context, int volume) {
        prefs(context).edit().putInt(KEY_SFX_VOLUME, volume).apply();
    }

    public static String getProfileName(Context context) {
        return prefs(context).getString(KEY_PROFILE_NAME, "");
    }

    public static void setProfileName(Context context, String name) {
        prefs(context).edit().putString(KEY_PROFILE_NAME, name).apply();
    }

    public static String getProfileAge(Context context) {
        return prefs(context).getString(KEY_PROFILE_AGE, "");
    }

    public static void setProfileAge(Context context, String age) {
        prefs(context).edit().putString(KEY_PROFILE_AGE, age).apply();
    }

    public static String getProfileImage(Context context) {
        return prefs(context).getString(KEY_PROFILE_IMAGE, "");
    }

    public static void setProfileImage(Context context, String uri) {
        prefs(context).edit().putString(KEY_PROFILE_IMAGE, uri).apply();
    }

    public static String getGameHistory(Context context) {
        return prefs(context).getString(KEY_GAME_HISTORY, "");
    }

    public static void addGameToHistory(Context context, String historyEntry) {
        String current = getGameHistory(context);
        String updated = historyEntry + (current.isEmpty() ? "" : "|" + current);
        
        String[] parts = updated.split("\\|");
        if (parts.length > 50) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                sb.append(parts[i]);
                if (i < 49) sb.append("|");
            }
            updated = sb.toString();
        }
        prefs(context).edit().putString(KEY_GAME_HISTORY, updated).apply();
    }
}
