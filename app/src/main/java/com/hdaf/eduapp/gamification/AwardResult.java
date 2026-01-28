package com.hdaf.eduapp.gamification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hdaf.eduapp.gamification.Badge;

/**
 * Result object containing details of an XP award event.
 * Includes total XP awarded, level changes, and any badges earned.
 */
public class AwardResult implements Serializable {
    
    private int xpAwarded;
    private String reason;
    private int newTotalXP;
    private int newLevel;
    private boolean leveledUp;
    private int previousLevel;
    private List<String> newBadgeIds;
    private String message;
    
    // Non-serialized list of Badge objects for immediate display
    private transient List<Badge> newBadges;

    private AwardResult(Builder builder) {
        this.xpAwarded = builder.xpAwarded;
        this.reason = builder.reason;
        this.newTotalXP = builder.newTotalXP;
        this.newLevel = builder.newLevel;
        this.leveledUp = builder.leveledUp;
        this.previousLevel = builder.previousLevel;
        this.newBadges = builder.newBadges;
        this.message = builder.message;
        
        // Populate IDs for serialization
        this.newBadgeIds = new ArrayList<>();
        if (newBadges != null) {
            for (Badge badge : newBadges) {
                this.newBadgeIds.add(badge.getId());
            }
        }
    }
    
    // Getters
    public int getXpAwarded() { return xpAwarded; }
    public String getReason() { return reason; }
    public int getNewTotalXP() { return newTotalXP; }
    public int getNewLevel() { return newLevel; }
    public boolean isLeveledUp() { return leveledUp; }
    public int getPreviousLevel() { return previousLevel; }
    public List<Badge> getNewBadges() { return newBadges; }
    public List<String> getNewBadgeIds() { return newBadgeIds; }
    public String getMessage() { return message; }
    
    public void setMessage(String message) { this.message = message; }

    public static class Builder {
        private int xpAwarded;
        private String reason;
        private int newTotalXP;
        private int newLevel;
        private boolean leveledUp;
        private int previousLevel;
        private List<Badge> newBadges = new ArrayList<>();
        private String message;

        public Builder xpAwarded(int xpAwarded) { this.xpAwarded = xpAwarded; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder newTotalXP(int newTotalXP) { this.newTotalXP = newTotalXP; return this; }
        public Builder newLevel(int newLevel) { this.newLevel = newLevel; return this; }
        public Builder leveledUp(boolean leveledUp) { this.leveledUp = leveledUp; return this; }
        public Builder previousLevel(int previousLevel) { this.previousLevel = previousLevel; return this; }
        public Builder message(String message) { this.message = message; return this; }
        
        public Builder addBadge(Badge badge) {
            if (newBadges == null) newBadges = new ArrayList<>();
            newBadges.add(badge);
            return this;
        }

        public AwardResult build() {
            return new AwardResult(this);
        }
    }
}
