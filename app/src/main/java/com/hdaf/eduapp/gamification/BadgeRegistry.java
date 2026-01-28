package com.hdaf.eduapp.gamification;

import com.hdaf.eduapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all available badges in the app.
 * Contains predefined badge definitions and lookup methods.
 */
public class BadgeRegistry {
    
    // Badge IDs
    public static final String BADGE_FIRST_QUIZ = "first_quiz";
    public static final String BADGE_QUIZ_MASTER = "quiz_master";
    public static final String BADGE_PERFECT_SCORE = "perfect_score";
    public static final String BADGE_STREAK_3 = "streak_3";
    public static final String BADGE_STREAK_7 = "streak_7";
    public static final String BADGE_STREAK_30 = "streak_30";
    public static final String BADGE_MATH_EXPERT = "math_expert";
    public static final String BADGE_SCIENCE_EXPERT = "science_expert";
    
    private static BadgeRegistry instance;
    private final Map<String, Badge> badgeMap;
    private final List<Badge> allBadges;
    
    private BadgeRegistry() {
        badgeMap = new HashMap<>();
        allBadges = new ArrayList<>();
        initializeBadges();
    }
    
    public static synchronized BadgeRegistry getInstance() {
        if (instance == null) {
            instance = new BadgeRegistry();
        }
        return instance;
    }
    
    /**
     * Initialize all predefined badges.
     */
    private void initializeBadges() {
        // Quiz badges
        addBadge(new Badge.Builder()
            .id(BADGE_FIRST_QUIZ)
            .name("First Quiz")
            .nameHindi("पहला क्विज़")
            .description("Complete your first quiz")
            .descriptionHindi("Apna pehla quiz complete karo")
            .iconResId(R.drawable.ic_badge_first_quiz)
            .requirement("Complete 1 quiz")
            .xpReward(50)
            .category(Badge.Category.QUIZ)
            .build());
        
        addBadge(new Badge.Builder()
            .id(BADGE_QUIZ_MASTER)
            .name("Quiz Master")
            .nameHindi("क्विज़ मास्टर")
            .description("Complete 10 quizzes")
            .descriptionHindi("10 quiz complete karo")
            .iconResId(R.drawable.ic_badge_quiz_master)
            .requirement("Complete 10 quizzes")
            .xpReward(200)
            .category(Badge.Category.QUIZ)
            .build());
        
        addBadge(new Badge.Builder()
            .id(BADGE_PERFECT_SCORE)
            .name("Perfect Score")
            .nameHindi("परफेक्ट स्कोर")
            .description("Score 100% on any quiz")
            .descriptionHindi("Kisi bhi quiz mein 100% score karo")
            .iconResId(R.drawable.ic_badge_perfect)
            .requirement("Get 100% on a quiz")
            .xpReward(100)
            .category(Badge.Category.QUIZ)
            .build());
        
        // Streak badges
        addBadge(new Badge.Builder()
            .id(BADGE_STREAK_3)
            .name("On Fire")
            .nameHindi("आग लगी है")
            .description("3-day learning streak")
            .descriptionHindi("3 din ka streak banao")
            .iconResId(R.drawable.ic_badge_streak_3)
            .requirement("Maintain 3-day streak")
            .xpReward(75)
            .category(Badge.Category.STREAK)
            .build());
        
        addBadge(new Badge.Builder()
            .id(BADGE_STREAK_7)
            .name("Week Warrior")
            .nameHindi("हफ्ते का योद्धा")
            .description("7-day learning streak")
            .descriptionHindi("7 din ka streak banao")
            .iconResId(R.drawable.ic_badge_streak_7)
            .requirement("Maintain 7-day streak")
            .xpReward(150)
            .category(Badge.Category.STREAK)
            .build());
        
        addBadge(new Badge.Builder()
            .id(BADGE_STREAK_30)
            .name("Monthly Champion")
            .nameHindi("महीने का चैंपियन")
            .description("30-day learning streak")
            .descriptionHindi("30 din ka streak banao")
            .iconResId(R.drawable.ic_badge_streak_30)
            .requirement("Maintain 30-day streak")
            .xpReward(500)
            .category(Badge.Category.STREAK)
            .build());
        
        // Subject expert badges
        addBadge(new Badge.Builder()
            .id(BADGE_MATH_EXPERT)
            .name("Math Expert")
            .nameHindi("गणित विशेषज्ञ")
            .description("Complete 5 Math quizzes")
            .descriptionHindi("5 Math quiz complete karo")
            .iconResId(R.drawable.ic_badge_math)
            .requirement("Complete 5 Math quizzes")
            .xpReward(150)
            .category(Badge.Category.SUBJECT)
            .build());
        
        addBadge(new Badge.Builder()
            .id(BADGE_SCIENCE_EXPERT)
            .name("Science Expert")
            .nameHindi("विज्ञान विशेषज्ञ")
            .description("Complete 5 Science quizzes")
            .descriptionHindi("5 Science quiz complete karo")
            .iconResId(R.drawable.ic_badge_science)
            .requirement("Complete 5 Science quizzes")
            .xpReward(150)
            .category(Badge.Category.SUBJECT)
            .build());
    }
    
    /**
     * Add a badge to the registry.
     */
    private void addBadge(Badge badge) {
        badgeMap.put(badge.getId(), badge);
        allBadges.add(badge);
    }
    
    /**
     * Get a badge by ID.
     */
    public Badge getBadge(String badgeId) {
        return badgeMap.get(badgeId);
    }
    
    /**
     * Get all available badges.
     */
    public List<Badge> getAllBadges() {
        return new ArrayList<>(allBadges);
    }
    
    /**
     * Get badges by category.
     */
    public List<Badge> getBadgesByCategory(Badge.Category category) {
        List<Badge> result = new ArrayList<>();
        for (Badge badge : allBadges) {
            if (badge.getCategory() == category) {
                result.add(badge);
            }
        }
        return result;
    }
    
    /**
     * Get total number of badges.
     */
    public int getBadgeCount() {
        return allBadges.size();
    }
    
    /**
     * Check if a badge exists.
     */
    public boolean badgeExists(String badgeId) {
        return badgeMap.containsKey(badgeId);
    }
}
