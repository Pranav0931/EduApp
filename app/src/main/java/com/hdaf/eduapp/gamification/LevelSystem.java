package com.hdaf.eduapp.gamification;

/**
 * Utility class for level calculations and progression.
 * XP thresholds follow a quadratic progression for balanced difficulty.
 */
public class LevelSystem {
    
    // Level titles in Hinglish
    public static final String TITLE_BEGINNER = "Beginner / शुरुआती";
    public static final String TITLE_LEARNER = "Learner / सीखने वाला";
    public static final String TITLE_SCHOLAR = "Scholar / विद्वान";
    public static final String TITLE_EXPERT = "Expert / विशेषज्ञ";
    public static final String TITLE_MASTER = "Master / गुरु";
    public static final String TITLE_GRANDMASTER = "Grandmaster / महागुरु";
    
    // XP multiplier for level calculation
    private static final int XP_MULTIPLIER = 50;
    
    // Maximum level
    public static final int MAX_LEVEL = 50;
    
    /**
     * Calculate level from total XP.
     * Formula: Level N requires (N^2 * 50) total XP
     * Level 1: 0 XP (starting level)
     * Level 2: 200 XP
     * Level 3: 450 XP
     * Level 10: 5000 XP
     */
    public static int calculateLevel(int totalXP) {
        if (totalXP < 0) return 1;
        
        // Solve for level: n^2 * 50 <= totalXP
        // n <= sqrt(totalXP / 50)
        int level = (int) Math.floor(Math.sqrt((double) totalXP / XP_MULTIPLIER));
        
        // Minimum level is 1, maximum is MAX_LEVEL
        return Math.max(1, Math.min(level + 1, MAX_LEVEL));
    }
    
    /**
     * Get total XP required to reach a specific level.
     * @param level target level (1-50)
     * @return XP required
     */
    public static int getXPForLevel(int level) {
        if (level <= 1) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        
        // XP = (level - 1)^2 * 50
        return (level - 1) * (level - 1) * XP_MULTIPLIER;
    }
    
    /**
     * Get XP needed from current level to next level.
     * @param currentLevel current level
     * @return XP needed for next level
     */
    public static int getXPToNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return 0;
        return getXPForLevel(currentLevel + 1) - getXPForLevel(currentLevel);
    }
    
    /**
     * Get level title based on level number.
     * Titles progress as player advances.
     */
    public static String getLevelTitle(int level) {
        if (level <= 5) {
            return TITLE_BEGINNER;
        } else if (level <= 10) {
            return TITLE_LEARNER;
        } else if (level <= 20) {
            return TITLE_SCHOLAR;
        } else if (level <= 35) {
            return TITLE_EXPERT;
        } else if (level <= 45) {
            return TITLE_MASTER;
        } else {
            return TITLE_GRANDMASTER;
        }
    }
    
    /**
     * Get English-only title for display.
     */
    public static String getLevelTitleEnglish(int level) {
        if (level <= 5) {
            return "Beginner";
        } else if (level <= 10) {
            return "Learner";
        } else if (level <= 20) {
            return "Scholar";
        } else if (level <= 35) {
            return "Expert";
        } else if (level <= 45) {
            return "Master";
        } else {
            return "Grandmaster";
        }
    }
    
    /**
     * Get progress percentage to next level.
     * @param totalXP current total XP
     * @return percentage (0-100)
     */
    public static int getProgressPercentage(int totalXP) {
        int currentLevel = calculateLevel(totalXP);
        if (currentLevel >= MAX_LEVEL) return 100;
        
        int currentLevelXP = getXPForLevel(currentLevel);
        int nextLevelXP = getXPForLevel(currentLevel + 1);
        int xpInLevel = totalXP - currentLevelXP;
        int xpNeeded = nextLevelXP - currentLevelXP;
        
        if (xpNeeded <= 0) return 100;
        return Math.min(100, (xpInLevel * 100) / xpNeeded);
    }
    
    /**
     * Calculate XP award for quiz based on score.
     * Base XP is proportional to score percentage.
     * Perfect scores get a bonus.
     */
    public static int calculateQuizXP(int score, int totalQuestions, boolean isPerfect) {
        // Base XP: 5 XP per correct answer
        int baseXP = (score * totalQuestions) / 100 * 5;
        
        // Minimum 10 XP for attempting
        baseXP = Math.max(10, baseXP);
        
        // Perfect score bonus: +50%
        if (isPerfect) {
            baseXP = (int) (baseXP * 1.5);
        }
        
        // Cap at 100 XP per quiz
        return Math.min(100, baseXP);
    }
    
    /**
     * Get level up message in Hinglish.
     */
    public static String getLevelUpMessage(int newLevel) {
        String title = getLevelTitleEnglish(newLevel);
        return "🎉 Level Up! Ab tum Level " + newLevel + " ho - " + title + "!";
    }
    
    /**
     * Get formatted level display string.
     */
    public static String getFormattedLevel(int level) {
        return "Lvl " + level;
    }
    
    /**
     * XP level milestones for reference.
     * Level 1:  0 XP
     * Level 2:  50 XP
     * Level 3:  200 XP
     * Level 5:  800 XP
     * Level 10: 4050 XP
     * Level 20: 18050 XP
     * Level 50: 120050 XP
     */
    public static int[] getLevelMilestones() {
        int[] milestones = new int[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; i++) {
            milestones[i] = getXPForLevel(i + 1);
        }
        return milestones;
    }
}
