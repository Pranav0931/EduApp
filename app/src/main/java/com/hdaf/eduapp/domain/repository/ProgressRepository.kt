package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.DailyGoal
import com.hdaf.eduapp.domain.model.LeaderboardEntry
import com.hdaf.eduapp.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for gamification and progress tracking.
 * Handles XP, levels, badges, streaks, and leaderboards.
 */
interface ProgressRepository {

    // ==================== XP System ====================

    /**
     * Add XP for completing an action.
     */
    suspend fun addXp(
        amount: Int,
        source: XpSource,
        description: String
    ): Resource<XpResult>

    /**
     * Get current user's progress.
     */
    fun getUserProgress(): Flow<Resource<UserProgress>>

    /**
     * Observe progress changes in real-time.
     */
    fun observeProgress(): Flow<UserProgress>

    /**
     * Sync progress with server.
     */
    suspend fun syncProgress(): Resource<Unit>

    // ==================== Streak System ====================

    /**
     * Update daily streak.
     */
    suspend fun updateStreak(): Resource<StreakStatus>

    /**
     * Get streak information.
     */
    fun getStreakInfo(): Flow<Resource<StreakStatus>>

    // ==================== Badge System ====================

    /**
     * Get all badges for user.
     */
    fun getUserBadges(): Flow<Resource<List<Badge>>>

    /**
     * Get all available badges with progress.
     */
    fun getAvailableBadges(): Flow<Resource<List<BadgeInfo>>>

    /**
     * Check and award badges based on current progress.
     */
    suspend fun checkAndAwardBadges(): Resource<List<Badge>>

    /**
     * Award a specific badge.
     */
    suspend fun awardBadge(badgeId: String): Resource<Badge>

    // ==================== Daily Goals ====================

    /**
     * Get today's goals.
     */
    fun getDailyGoal(): Flow<Resource<DailyGoal>>

    /**
     * Set custom daily goals.
     */
    suspend fun setDailyGoal(
        quizCount: Int,
        xpTarget: Int
    ): Resource<DailyGoal>

    /**
     * Check if daily goal is completed.
     */
    suspend fun checkDailyGoalCompletion(): Resource<Boolean>

    // ==================== Leaderboard ====================

    /**
     * Get leaderboard.
     */
    fun getLeaderboard(
        scope: String, // "global", "weekly", "monthly"
        limit: Int = 100
    ): Flow<Resource<List<LeaderboardEntry>>>

    /**
     * Get user's rank information.
     */
    fun getUserRank(): Flow<Resource<UserRank>>
}

// ==================== Supporting Data Classes ====================

enum class XpSource(val baseXp: Int) {
    QUIZ_COMPLETED(30),
    QUIZ_PERFECT_SCORE(100),
    CHAPTER_COMPLETED(50),
    BOOK_COMPLETED(200),
    DAILY_LOGIN(10),
    STREAK_BONUS(25),
    BADGE_EARNED(50),
    DAILY_GOAL(40),
    DAILY_BONUS(25),
    AI_CHALLENGE(50)
}

data class XpResult(
    val xpEarned: Int,
    val newTotalXp: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val streakBonus: Int = 0
)

data class StreakStatus(
    val currentStreak: Int,
    val maxStreak: Int,
    val streakBroken: Boolean,
    val hoursUntilStreakLost: Long,
    val isActiveToday: Boolean = false
)

data class UserRank(
    val globalRank: Int,
    val weeklyRank: Int,
    val monthlyRank: Int,
    val percentile: Float,
    val totalUsers: Int
)

data class BadgeInfo(
    val badge: Badge,
    val isEarned: Boolean,
    val progress: Float // 0 to 1
)
