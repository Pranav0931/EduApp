package com.hdaf.eduapp.gamification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Model class representing a user's gamification progress.
 * Tracks XP, level, streaks, and achievements.
 */
public class UserProgress {
    
    private int totalXP;
    private int currentLevel;
    private int currentStreak;
    private int longestStreak;
    private String lastActiveDate; // Format: yyyy-MM-dd
    private int lessonsCompleted;
    private int quizzesCompleted;
    private int perfectScores;
    private List<String> earnedBadgeIds;
    
    // Subject-specific quiz counts
    private int mathQuizzesCompleted;
    private int scienceQuizzesCompleted;
    private int englishQuizzesCompleted;
    private int hindiQuizzesCompleted;
    private int socialScienceQuizzesCompleted;
    
    public UserProgress() {
        this.totalXP = 0;
        this.currentLevel = 1;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lessonsCompleted = 0;
        this.quizzesCompleted = 0;
        this.perfectScores = 0;
        this.earnedBadgeIds = new ArrayList<>();
        this.mathQuizzesCompleted = 0;
        this.scienceQuizzesCompleted = 0;
        this.englishQuizzesCompleted = 0;
        this.hindiQuizzesCompleted = 0;
        this.socialScienceQuizzesCompleted = 0;
    }
    
    // Getters
    public int getTotalXP() { return totalXP; }
    public int getCurrentLevel() { return currentLevel; }
    public int getCurrentStreak() { return currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public String getLastActiveDate() { return lastActiveDate; }
    public int getLessonsCompleted() { return lessonsCompleted; }
    public int getQuizzesCompleted() { return quizzesCompleted; }
    public int getPerfectScores() { return perfectScores; }
    public List<String> getEarnedBadgeIds() { return earnedBadgeIds != null ? earnedBadgeIds : new ArrayList<>(); }
    
    public int getMathQuizzesCompleted() { return mathQuizzesCompleted; }
    public int getScienceQuizzesCompleted() { return scienceQuizzesCompleted; }
    public int getEnglishQuizzesCompleted() { return englishQuizzesCompleted; }
    public int getHindiQuizzesCompleted() { return hindiQuizzesCompleted; }
    public int getSocialScienceQuizzesCompleted() { return socialScienceQuizzesCompleted; }
    
    // Setters
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public void setLastActiveDate(String lastActiveDate) { this.lastActiveDate = lastActiveDate; }
    public void setLessonsCompleted(int lessonsCompleted) { this.lessonsCompleted = lessonsCompleted; }
    public void setQuizzesCompleted(int quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }
    public void setPerfectScores(int perfectScores) { this.perfectScores = perfectScores; }
    public void setEarnedBadgeIds(List<String> earnedBadgeIds) { this.earnedBadgeIds = earnedBadgeIds; }
    
    public void setMathQuizzesCompleted(int count) { this.mathQuizzesCompleted = count; }
    public void setScienceQuizzesCompleted(int count) { this.scienceQuizzesCompleted = count; }
    public void setEnglishQuizzesCompleted(int count) { this.englishQuizzesCompleted = count; }
    public void setHindiQuizzesCompleted(int count) { this.hindiQuizzesCompleted = count; }
    public void setSocialScienceQuizzesCompleted(int count) { this.socialScienceQuizzesCompleted = count; }
    
    /**
     * Add XP and recalculate level.
     * @return true if level increased
     */
    public boolean addXP(int xp) {
        int oldLevel = this.currentLevel;
        this.totalXP += xp;
        this.currentLevel = LevelSystem.calculateLevel(this.totalXP);
        return this.currentLevel > oldLevel;
    }
    
    /**
     * Increment quiz completion count.
     */
    public void incrementQuizzesCompleted() {
        this.quizzesCompleted++;
    }
    
    /**
     * Increment subject-specific quiz count.
     */
    public void incrementSubjectQuizCount(String subject) {
        if (subject == null) return;
        
        String subjectLower = subject.toLowerCase();
        if (subjectLower.contains("math") || subjectLower.contains("ganit")) {
            mathQuizzesCompleted++;
        } else if (subjectLower.contains("science") || subjectLower.contains("vigyan")) {
            scienceQuizzesCompleted++;
        } else if (subjectLower.contains("english") || subjectLower.contains("angrezi")) {
            englishQuizzesCompleted++;
        } else if (subjectLower.contains("hindi")) {
            hindiQuizzesCompleted++;
        } else if (subjectLower.contains("social") || subjectLower.contains("samajik")) {
            socialScienceQuizzesCompleted++;
        }
    }
    
    /**
     * Get quiz count for a specific subject.
     */
    public int getSubjectQuizCount(String subject) {
        if (subject == null) return 0;
        
        String subjectLower = subject.toLowerCase();
        if (subjectLower.contains("math") || subjectLower.contains("ganit")) {
            return mathQuizzesCompleted;
        } else if (subjectLower.contains("science") || subjectLower.contains("vigyan")) {
            return scienceQuizzesCompleted;
        } else if (subjectLower.contains("english") || subjectLower.contains("angrezi")) {
            return englishQuizzesCompleted;
        } else if (subjectLower.contains("hindi")) {
            return hindiQuizzesCompleted;
        } else if (subjectLower.contains("social") || subjectLower.contains("samajik")) {
            return socialScienceQuizzesCompleted;
        }
        return 0;
    }
    
    /**
     * Increment perfect score count.
     */
    public void incrementPerfectScores() {
        this.perfectScores++;
    }
    
    /**
     * Add a badge if not already earned.
     * @return true if badge was newly added
     */
    public boolean addBadge(String badgeId) {
        if (earnedBadgeIds == null) {
            earnedBadgeIds = new ArrayList<>();
        }
        if (!earnedBadgeIds.contains(badgeId)) {
            earnedBadgeIds.add(badgeId);
            return true;
        }
        return false;
    }
    
    /**
     * Check if a badge is earned.
     */
    public boolean hasBadge(String badgeId) {
        return earnedBadgeIds != null && earnedBadgeIds.contains(badgeId);
    }
    
    /**
     * Update last active date to today.
     */
    public void updateLastActiveDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        this.lastActiveDate = sdf.format(new Date());
    }
    
    /**
     * Get today's date string.
     */
    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }
    
    /**
     * Get XP progress to next level (0.0 to 1.0).
     */
    public float getProgressToNextLevel() {
        int currentLevelXP = LevelSystem.getXPForLevel(currentLevel);
        int nextLevelXP = LevelSystem.getXPForLevel(currentLevel + 1);
        int xpInCurrentLevel = totalXP - currentLevelXP;
        int xpNeededForLevel = nextLevelXP - currentLevelXP;
        
        if (xpNeededForLevel <= 0) return 1.0f;
        return Math.min(1.0f, (float) xpInCurrentLevel / xpNeededForLevel);
    }
    
    /**
     * Get XP needed to reach next level.
     */
    public int getXPToNextLevel() {
        int nextLevelXP = LevelSystem.getXPForLevel(currentLevel + 1);
        return Math.max(0, nextLevelXP - totalXP);
    }
    
    /**
     * Get level title based on current level.
     */
    public String getLevelTitle() {
        return LevelSystem.getLevelTitle(currentLevel);
    }
}
