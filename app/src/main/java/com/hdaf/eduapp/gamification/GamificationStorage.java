package com.hdaf.eduapp.gamification;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Storage helper for gamification data.
 * Uses SharedPreferences with Gson for JSON serialization.
 */
public class GamificationStorage {
    
    private static final String PREFS_NAME = "eduapp_gamification";
    private static final String KEY_USER_PROGRESS = "user_progress";
    private static final String KEY_DAILY_CHALLENGE = "daily_challenge";
    private static final String KEY_LAST_CHALLENGE_DATE = "last_challenge_date";
    
    private static GamificationStorage instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    
    private GamificationStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
    }
    
    public static synchronized GamificationStorage getInstance(Context context) {
        if (instance == null) {
            instance = new GamificationStorage(context);
        }
        return instance;
    }
    
    /**
     * Save user progress to storage.
     */
    public void saveUserProgress(UserProgress progress) {
        String json = gson.toJson(progress);
        preferences.edit().putString(KEY_USER_PROGRESS, json).apply();
    }
    
    /**
     * Load user progress from storage.
     * Returns a new UserProgress if none exists.
     */
    public UserProgress loadUserProgress() {
        String json = preferences.getString(KEY_USER_PROGRESS, null);
        if (json != null) {
            try {
                UserProgress progress = gson.fromJson(json, UserProgress.class);
                if (progress != null) {
                    return progress;
                }
            } catch (Exception e) {
                // If parsing fails, return new progress
            }
        }
        return new UserProgress();
    }
    
    /**
     * Save daily challenge to storage.
     */
    public void saveDailyChallenge(DailyChallenge challenge) {
        String json = gson.toJson(challenge);
        preferences.edit()
                .putString(KEY_DAILY_CHALLENGE, json)
                .putString(KEY_LAST_CHALLENGE_DATE, challenge.getDate())
                .apply();
    }
    
    /**
     * Load daily challenge from storage.
     * Returns null if no challenge or if it's for a different day.
     */
    public DailyChallenge loadDailyChallenge() {
        String json = preferences.getString(KEY_DAILY_CHALLENGE, null);
        if (json != null) {
            try {
                DailyChallenge challenge = gson.fromJson(json, DailyChallenge.class);
                if (challenge != null && challenge.isForToday()) {
                    return challenge;
                }
            } catch (Exception e) {
                // If parsing fails, return null
            }
        }
        return null;
    }
    
    /**
     * Check if there's a valid challenge for today.
     */
    public boolean hasTodayChallenge() {
        String lastDate = preferences.getString(KEY_LAST_CHALLENGE_DATE, null);
        if (lastDate == null) return false;
        return lastDate.equals(UserProgress.getTodayDateString());
    }
    
    /**
     * Clear all gamification data.
     * Use with caution - for testing/reset purposes only.
     */
    public void clearAllData() {
        preferences.edit().clear().apply();
    }
    
    /**
     * Add XP directly to stored progress.
     * Convenience method for quick updates.
     */
    public void addXP(int xp) {
        UserProgress progress = loadUserProgress();
        progress.addXP(xp);
        saveUserProgress(progress);
    }
    
    /**
     * Check if user has earned a specific badge.
     */
    public boolean hasBadge(String badgeId) {
        UserProgress progress = loadUserProgress();
        return progress.hasBadge(badgeId);
    }
    
    /**
     * Add a badge to user's collection.
     * @return true if badge was newly added
     */
    public boolean addBadge(String badgeId) {
        UserProgress progress = loadUserProgress();
        boolean added = progress.addBadge(badgeId);
        if (added) {
            saveUserProgress(progress);
        }
        return added;
    }
    
    /**
     * Get current streak.
     */
    public int getCurrentStreak() {
        return loadUserProgress().getCurrentStreak();
    }
    
    /**
     * Get total XP.
     */
    public int getTotalXP() {
        return loadUserProgress().getTotalXP();
    }
    
    /**
     * Get current level.
     */
    public int getCurrentLevel() {
        return loadUserProgress().getCurrentLevel();
    }
}
