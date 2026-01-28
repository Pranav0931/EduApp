package com.hdaf.eduapp.analytics;

import android.content.Context;
import android.util.Log;

public class AnalyticsManager {

    private static final String TAG = "AnalyticsManager";
    private static AnalyticsManager instance;

    private LearningSession currentSession;

    private AnalyticsManager(Context context) {
        // Application context only
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Start learning session tracking
     */
    public void startSession(LearningSession.ActivityType type, String subject) {
        currentSession = new LearningSession(type, subject);
        Log.d(TAG, "Session started: " + type + " - " + subject);
    }

    /**
     * End learning session tracking
     */
    public void endSession() {
        if (currentSession == null) return;

        currentSession.endSession();

        Log.d(TAG, "Session ended: " + currentSession.getActivityType()
                + " | Duration: " + currentSession.getDurationSeconds() + " sec");

        currentSession = null;
    }
}
