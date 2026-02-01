package com.hdaf.eduapp.domain.model

import java.util.Date

/**
 * Domain model for User Progress and Gamification.
 */
data class UserProgress(
    val userId: String,
    val totalXp: Int = 0,
    val level: Int = 1,
    val xpToNextLevel: Int = 150,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val quizzesCompleted: Int = 0,
    val chaptersCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val totalStudyTimeMinutes: Int = 0,
    val globalRank: Int = 0,
    val weeklyRank: Int = 0,
    val badges: List<Badge> = emptyList(),
    val lastActivityDate: Date? = null
) {
    val levelProgress: Float
        get() {
            val currentLevelXp = calculateXpForLevel(level)
            val nextLevelXp = calculateXpForLevel(level + 1)
            val xpIntoLevel = totalXp - currentLevelXp
            val xpNeededForLevel = nextLevelXp - currentLevelXp
            return (xpIntoLevel.toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
        }
    
    val rank: UserRank
        get() = UserRank.fromLevel(level)
    
    private fun calculateXpForLevel(level: Int): Int {
        // XP formula: 100 * level * (level + 1) / 2
        return 100 * level * (level + 1) / 2
    }
}

enum class BadgeType(val displayName: String) {
    STREAK("स्ट्रीक"),
    QUIZ("प्रश्नोत्तरी"),
    ACHIEVEMENT("उपलब्धि"),
    SUBJECT_MASTERY("विषय महारत"),
    SPECIAL("विशेष");
    
    companion object {
        fun fromString(value: String): BadgeType {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) 
            } ?: ACHIEVEMENT
        }
    }
}

enum class BadgeRarity(val displayName: String, val xpBonus: Int) {
    COMMON("सामान्य", 10),
    UNCOMMON("असामान्य", 25),
    RARE("दुर्लभ", 50),
    EPIC("महाकाव्य", 100),
    LEGENDARY("पौराणिक", 250);
    
    companion object {
        fun fromString(value: String): BadgeRarity {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) 
            } ?: COMMON
        }
    }
}

enum class UserRank(val displayName: String, val minLevel: Int) {
    BEGINNER("नवसिखुआ", 1),
    LEARNER("शिक्षार्थी", 5),
    EXPLORER("अन्वेषक", 10),
    SCHOLAR("विद्वान", 20),
    EXPERT("विशेषज्ञ", 35),
    MASTER("गुरु", 50),
    LEGEND("दिग्गज", 75);
    
    companion object {
        fun fromLevel(level: Int): UserRank {
            return entries.sortedByDescending { it.minLevel }
                .find { level >= it.minLevel } ?: BEGINNER
        }
    }
}

/**
 * User rank across leaderboards.
 */
data class UserRankInfo(
    val globalRank: Int,
    val weeklyRank: Int,
    val monthlyRank: Int,
    val percentile: Float,
    val totalUsers: Int
)

/**
 * Leaderboard entry.
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalXp: Int,
    val level: Int,
    val currentStreak: Int = 0,
    val isCurrentUser: Boolean = false
)

/**
 * Daily goal tracking.
 */
data class DailyGoal(
    val quizzesCompleted: Int = 0,
    val quizzesGoal: Int = 3,
    val xpEarned: Int = 0,
    val xpGoal: Int = 100,
    val isCompleted: Boolean = false,
    val date: Date = Date()
) {
    val quizProgress: Float
        get() = (quizzesCompleted.toFloat() / quizzesGoal).coerceAtMost(1f)
    
    val xpProgress: Float
        get() = (xpEarned.toFloat() / xpGoal).coerceAtMost(1f)
    
    val overallProgress: Float
        get() = maxOf(quizProgress, xpProgress)
}
