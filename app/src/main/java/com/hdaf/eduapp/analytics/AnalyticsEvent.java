package com.hdaf.eduapp.analytics;

import java.util.Map;

/**
 * Represents a single analytics event (e.g., quiz_completed, lesson_viewed).
 */
public class AnalyticsEvent {
    
    public enum Type {
        QUIZ_COMPLETED,
        LESSON_STARTED,
        LESSON_COMPLETED,
        APP_OPENED,
        VOICE_COMMAND_USED,
        STREAK_UPDATED
    }

    private String eventId;
    private Type type;
    private long timestamp;
    private Map<String, Object> params;

    public AnalyticsEvent(Type type, Map<String, Object> params) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.params = params;
    }

    public String getEventId() { return eventId; }
    public Type getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public Map<String, Object> getParams() { return params; }
}
