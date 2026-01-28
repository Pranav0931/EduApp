package com.hdaf.eduapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing shared preferences
 */
public class PreferenceManager {

    private static PreferenceManager instance;
    private final SharedPreferences preferences;

    private PreferenceManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    // ---------------- MODE ----------------

    public void setLastMode(String mode) {
        preferences.edit().putString(Constants.PREF_LAST_MODE, mode).apply();
    }

    public String getLastMode() {
        return preferences.getString(Constants.PREF_LAST_MODE, Constants.MODE_AUDIO);
    }

    // ---------------- TTS ----------------

    public void setTtsSpeed(float speed) {
        preferences.edit().putFloat(Constants.PREF_TTS_SPEED, speed).apply();
    }

    public float getTtsSpeed() {
        return preferences.getFloat(Constants.PREF_TTS_SPEED, Constants.DEFAULT_TTS_SPEED);
    }

    // ---------------- NAVIGATION STATE ----------------

    public void setLastClass(String classId) {
        preferences.edit().putString(Constants.PREF_LAST_CLASS, classId).apply();
    }

    public String getLastClass() {
        return preferences.getString(Constants.PREF_LAST_CLASS, null);
    }

    public void setLastBook(String bookId) {
        preferences.edit().putString(Constants.PREF_LAST_BOOK, bookId).apply();
    }

    public String getLastBook() {
        return preferences.getString(Constants.PREF_LAST_BOOK, null);
    }

    public void setLastChapter(String chapterId) {
        preferences.edit().putString(Constants.PREF_LAST_CHAPTER, chapterId).apply();
    }

    public String getLastChapter() {
        return preferences.getString(Constants.PREF_LAST_CHAPTER, null);
    }

    // ---------------- GENERIC STRING STORAGE ----------------

    public String getStringPref(String key) {
        return preferences.getString(key, null);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    // ---------------- CLEAR ----------------

    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
