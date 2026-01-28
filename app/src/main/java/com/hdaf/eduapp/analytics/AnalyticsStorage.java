package com.hdaf.eduapp.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage helper for analytics data.
 */
public class AnalyticsStorage {

    private static final String PREFS_NAME = "eduapp_analytics";

    private static final String KEY_SUBJECT_PERFORMANCE = "subject_performance";
    private static final String KEY_SESSION_LOGS = "session_logs";
    private static final String KEY_PARENT_PIN = "parent_pin";

    private static AnalyticsStorage instance;

    private final SharedPreferences preferences;
    private final Gson gson;

    private AnalyticsStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
    }

    public static synchronized AnalyticsStorage getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsStorage(context);
        }
        return instance;
    }

    // ------------------------------------------------
    // SUBJECT PERFORMANCE STORAGE
    // ------------------------------------------------

    public void savePerformanceMap(Map<String, SubjectPerformance> map) {
        String json = gson.toJson(map);
        preferences.edit().putString(KEY_SUBJECT_PERFORMANCE, json).apply();
    }

    public Map<String, SubjectPerformance> loadPerformanceMap() {
        String json = preferences.getString(KEY_SUBJECT_PERFORMANCE, null);
        if (json != null) {
            try {
                Type type = new TypeToken<HashMap<String, SubjectPerformance>>(){}.getType();
                Map<String, SubjectPerformance> map = gson.fromJson(json, type);
                if (map != null) return map;
            } catch (Exception ignored) {}
        }
        return new HashMap<>();
    }

    // ------------------------------------------------
    // SESSION LOG STORAGE
    // ------------------------------------------------

    public void saveSession(LearningSession session) {
        List<LearningSession> logs = loadSessionLogs();
        logs.add(session);
        saveSessionLogs(logs);
    }

    public List<LearningSession> loadSessionLogs() {
        String json = preferences.getString(KEY_SESSION_LOGS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<LearningSession>>(){}.getType();
        List<LearningSession> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    private void saveSessionLogs(List<LearningSession> logs) {
        preferences.edit()
                .putString(KEY_SESSION_LOGS, gson.toJson(logs))
                .apply();
    }

    // ------------------------------------------------
    // PARENT PIN STORAGE
    // ------------------------------------------------

    public void saveParentPin(String pin) {
        preferences.edit().putString(KEY_PARENT_PIN, pin).apply();
    }

    public boolean verifyPin(String pin) {
        String saved = preferences.getString(KEY_PARENT_PIN, null);
        return saved != null && saved.equals(pin);
    }

    public boolean isPinSet() {
        return preferences.contains(KEY_PARENT_PIN);
    }

    // ------------------------------------------------
    // UTIL
    // ------------------------------------------------

    public void clearAllData() {
        preferences.edit().clear().apply();
    }
}
