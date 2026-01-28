package com.hdaf.eduapp.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Model representing a single continuous learning activity
 * (video, audio, quiz, or general app usage).
 */
public class LearningSession {

    public void setDuration(long duration) {

    }

    public enum ActivityType {
        VIDEO_LESSON,
        AUDIO_LESSON,
        QUIZ,
        APP_USAGE
    }

    private final String sessionId;
    private final ActivityType activityType;
    private final String subject;
    private final long startTime;

    private long endTime;
    private long durationSeconds;

    private final Map<String, String> metadata;

    public LearningSession(ActivityType activityType, String subject, long sessionStartTime) {
        this.sessionId = UUID.randomUUID().toString();
        this.activityType = activityType;
        this.subject = subject;
        this.startTime = System.currentTimeMillis();
        this.metadata = new HashMap<>();
    }

    /**
     * Ends the session and calculates total duration.
     */
    public void endSession() {
        this.endTime = System.currentTimeMillis();
        this.durationSeconds = Math.max(0, (endTime - startTime) / 1000);
    }

    // -------- Getters --------

    public String getSessionId() {
        return sessionId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getSubject() {
        return subject;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // -------- Metadata Helpers --------

    public void addMetadata(String key, String value) {
        if (key != null && value != null) {
            metadata.put(key, value);
        }
    }
}
