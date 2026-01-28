package com.hdaf.eduapp.gamification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing a daily challenge.
 * Daily challenges give bonus XP for completing specific tasks.
 */
public class DailyChallenge {
    
    /**
     * Types of daily challenges.
     */
    public enum Type {
        QUIZ,       // Complete a quiz
        LESSON,     // Complete a lesson
        REVISION,   // Revise a topic
        PERFECT,    // Get a perfect score
        STREAK      // Maintain streak
    }
    
    private String id;
    private Type type;
    private String description;
    private String descriptionHindi;
    private int xpReward;
    private boolean completed;
    private String date; // Format: yyyy-MM-dd
    private String targetSubject; // Optional, for subject-specific challenges
    
    public DailyChallenge() {
        this.completed = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        this.date = sdf.format(new Date());
    }
    
    public DailyChallenge(Type type, String description, String descriptionHindi, 
                          int xpReward, String targetSubject) {
        this();
        this.id = generateId();
        this.type = type;
        this.description = description;
        this.descriptionHindi = descriptionHindi;
        this.xpReward = xpReward;
        this.targetSubject = targetSubject;
    }
    
    // Getters
    public String getId() { return id; }
    public Type getType() { return type; }
    public String getDescription() { return description; }
    public String getDescriptionHindi() { return descriptionHindi; }
    public int getXpReward() { return xpReward; }
    public boolean isCompleted() { return completed; }
    public String getDate() { return date; }
    public String getTargetSubject() { return targetSubject; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(Type type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setDescriptionHindi(String descriptionHindi) { this.descriptionHindi = descriptionHindi; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setDate(String date) { this.date = date; }
    public void setTargetSubject(String targetSubject) { this.targetSubject = targetSubject; }
    
    /**
     * Mark challenge as completed.
     */
    public void complete() {
        this.completed = true;
    }
    
    /**
     * Get display description (bilingual format).
     */
    public String getDisplayDescription() {
        if (descriptionHindi != null && !descriptionHindi.isEmpty()) {
            return description + " / " + descriptionHindi;
        }
        return description;
    }
    
    /**
     * Check if this challenge is for today.
     */
    public boolean isForToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date());
        return today.equals(date);
    }
    
    /**
     * Get type icon emoji.
     */
    public String getTypeEmoji() {
        switch (type) {
            case QUIZ:
                return "📝";
            case LESSON:
                return "📚";
            case REVISION:
                return "🔄";
            case PERFECT:
                return "⭐";
            case STREAK:
                return "🔥";
            default:
                return "🎯";
        }
    }
    
    /**
     * Generate unique ID for challenge.
     */
    private String generateId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return "challenge_" + sdf.format(new Date()) + "_" + System.currentTimeMillis() % 1000;
    }
    
    /**
     * Get TalkBack announcement.
     */
    public String getAccessibilityAnnouncement() {
        String status = completed ? "Completed" : "In progress";
        return "Daily challenge: " + description + ". " +
               "Reward: " + xpReward + " XP. Status: " + status;
    }
    
    /**
     * Predefined challenge templates.
     */
    public static class Templates {
        
        public static DailyChallenge quizChallenge() {
            return new DailyChallenge(
                Type.QUIZ,
                "Complete any quiz today",
                "Aaj koi bhi quiz complete karo",
                50,
                null
            );
        }
        
        public static DailyChallenge perfectScoreChallenge() {
            return new DailyChallenge(
                Type.PERFECT,
                "Score 100% on any quiz",
                "Kisi bhi quiz mein 100% score karo",
                100,
                null
            );
        }
        
        public static DailyChallenge mathChallenge() {
            return new DailyChallenge(
                Type.QUIZ,
                "Complete a Math quiz",
                "Math ka quiz complete karo",
                75,
                "Math"
            );
        }
        
        public static DailyChallenge scienceChallenge() {
            return new DailyChallenge(
                Type.QUIZ,
                "Complete a Science quiz",
                "Science ka quiz complete karo",
                75,
                "Science"
            );
        }
        
        public static DailyChallenge streakChallenge() {
            return new DailyChallenge(
                Type.STREAK,
                "Maintain your streak",
                "Apna streak banaye rakho",
                25,
                null
            );
        }
    }
}
