package com.hdaf.eduapp.supabase.models;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for syncing user profile to Supabase.
 * Maps to the 'profiles' table.
 */
public class ProfileModel {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("xp")
    private int xp;

    @SerializedName("level")
    private int level;

    @SerializedName("total_quizzes")
    private int totalQuizzes;

    @SerializedName("streak_days")
    private int streakDays;
    
    @SerializedName("last_active_date")
    private String lastActiveDate;

    public ProfileModel(String userId, int xp, int level, int totalQuizzes, int streakDays, String lastActiveDate) {
        this.userId = userId;
        this.xp = xp;
        this.level = level;
        this.totalQuizzes = totalQuizzes;
        this.streakDays = streakDays;
        this.lastActiveDate = lastActiveDate;
    }
    
    // Getters and setters omitted for brevity unless needed for deserialization debugging
}
