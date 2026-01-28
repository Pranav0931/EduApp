package com.hdaf.eduapp.analytics;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton manager to track user learning analytics.
 */
public class AnalyticsManager {

    private static final String TAG = "AnalyticsManager";
    private static AnalyticsManager instance;

    private final AnalyticsStorage storage;
    private LearningSession currentSession;
    private Map<String, SubjectPerformance> subjectPerformanceMap;

    private AnalyticsManager(Context context) {
        storage = AnalyticsStorage.getInstance(context.getApplicationContext());

        subjectPerformanceMap = storage.loadPerformanceMap();
        if (subjectPerformanceMap == null) {
            subjectPerformanceMap = new HashMap<>();
        }
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    // ------------------------------------------------
    // SESSION TRACKING
    // ------------------------------------------------

    public void startSession(LearningSession.ActivityType type, String subject) {
        currentSession = new LearningSession(
                type,
                subject,
                System.currentTimeMillis()
        );

        Log.d(TAG, "Session started: " + type + " - " + subject);
    }

    public void endSession() {
        if (currentSession == null) return;

        currentSession.endSession();

        // Ignore very short sessions (<5 sec)
        if (currentSession.getDurationSeconds() >= 5) {
            storage.saveSession(currentSession);
        }

        Log.d(TAG, "Session ended: " + currentSession.getActivityType()
                + " | Duration: " + currentSession.getDurationSeconds() + " sec");

        currentSession = null;
    }

    // ------------------------------------------------
    // QUIZ ANALYTICS
    // ------------------------------------------------

    public void logQuizResult(String subject, int score, List<String> weakTopics) {
        if (subject == null || subject.isEmpty()) subject = "General";

        SubjectPerformance performance = getOrCreatePerformance(subject);

        // total questions = 10 (default), correct = score%, time = 60s default
        performance.addQuizResult(10, score * 10, 60000);

        storage.savePerformanceMap(subjectPerformanceMap);

        LearningSession quizSession = new LearningSession(
                LearningSession.ActivityType.QUIZ,
                subject,
                System.currentTimeMillis()
        );

        quizSession.addMetadata("score", String.valueOf(score));
        quizSession.addMetadata("weak_topics", weakTopics.toString());

        quizSession.endSession();
        storage.saveSession(quizSession);

        Log.d(TAG, "Quiz logged: " + subject + " score=" + score);
    }

    // ------------------------------------------------
    // SUBJECT PERFORMANCE
    // ------------------------------------------------

    public SubjectPerformance getSubjectPerformance(String subject) {
        return getOrCreatePerformance(subject);
    }

    public Map<String, SubjectPerformance> getAllSubjectPerformance() {
        return subjectPerformanceMap;
    }

    private SubjectPerformance getOrCreatePerformance(String subject) {
        if (!subjectPerformanceMap.containsKey(subject)) {
            subjectPerformanceMap.put(subject, new SubjectPerformance(subject));
        }
        return subjectPerformanceMap.get(subject);
    }
}
