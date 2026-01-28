package com.hdaf.eduapp.supabase.models;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for syncing analytics sessions to Supabase.
 * Maps to the 'analytics_logs' table.
 */
public class AnalyticsLogModel {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("activity_type")
    private String activityType;

    @SerializedName("subject")
    private String subject;

    @SerializedName("start_time")
    private String startTime; // ISO 8601 string preferably

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("duration_seconds")
    private long durationSeconds;

    @SerializedName("metadata")
    private String metadataJson; // Store map as JSON string

    public AnalyticsLogModel(String sessionId, String userId, String activityType, String subject, 
                             String startTime, String endTime, long durationSeconds, String metadataJson) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.activityType = activityType;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.metadataJson = metadataJson;
    }
}
