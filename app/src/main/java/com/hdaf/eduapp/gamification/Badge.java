package com.hdaf.eduapp.gamification;

/**
 * Model class representing a badge/achievement.
 * Badges are earned by completing specific goals.
 */
public class Badge {
    
    /**
     * Badge categories for grouping.
     */
    public enum Category {
        QUIZ,       // Quiz-related achievements
        STREAK,     // Streak-related achievements
        LEARNING,   // Learning/lesson achievements
        SUBJECT,    // Subject mastery achievements
        SPECIAL     // Special/rare achievements
    }
    
    private String id;
    private String name;
    private String nameHindi;
    private String description;
    private String descriptionHindi;
    private int iconResId;
    private String requirement;
    private int xpReward;
    private Category category;
    
    public Badge() {}
    
    public Badge(String id, String name, String nameHindi, String description, 
                 String descriptionHindi, int iconResId, String requirement,
                 int xpReward, Category category) {
        this.id = id;
        this.name = name;
        this.nameHindi = nameHindi;
        this.description = description;
        this.descriptionHindi = descriptionHindi;
        this.iconResId = iconResId;
        this.requirement = requirement;
        this.xpReward = xpReward;
        this.category = category;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameHindi() { return nameHindi; }
    public String getDescription() { return description; }
    public String getDescriptionHindi() { return descriptionHindi; }
    public int getIconResId() { return iconResId; }
    public String getRequirement() { return requirement; }
    public int getXpReward() { return xpReward; }
    public Category getCategory() { return category; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setNameHindi(String nameHindi) { this.nameHindi = nameHindi; }
    public void setDescription(String description) { this.description = description; }
    public void setDescriptionHindi(String descriptionHindi) { this.descriptionHindi = descriptionHindi; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public void setCategory(Category category) { this.category = category; }
    
    /**
     * Get display name (bilingual format).
     */
    public String getDisplayName() {
        if (nameHindi != null && !nameHindi.isEmpty()) {
            return name + " / " + nameHindi;
        }
        return name;
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
     * Get TalkBack announcement for accessibility.
     */
    public String getAccessibilityAnnouncement(boolean isEarned) {
        if (isEarned) {
            return "Badge earned: " + name + ". " + description + 
                   ". Reward: " + xpReward + " XP.";
        } else {
            return "Locked badge: " + name + ". " + requirement;
        }
    }
    
    /**
     * Builder pattern for creating badges.
     */
    public static class Builder {
        private String id;
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private int iconResId;
        private String requirement;
        private int xpReward;
        private Category category;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder nameHindi(String nameHindi) { this.nameHindi = nameHindi; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder descriptionHindi(String descriptionHindi) { this.descriptionHindi = descriptionHindi; return this; }
        public Builder iconResId(int iconResId) { this.iconResId = iconResId; return this; }
        public Builder requirement(String requirement) { this.requirement = requirement; return this; }
        public Builder xpReward(int xpReward) { this.xpReward = xpReward; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        
        public Badge build() {
            return new Badge(id, name, nameHindi, description, descriptionHindi,
                           iconResId, requirement, xpReward, category);
        }
    }
}
