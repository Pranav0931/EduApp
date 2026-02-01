package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for gamification features.
 * Handles stats, badges, and additional gamification elements.
 */
interface GamificationRepository {

    // ==================== User Stats ====================

    /**
     * Get user statistics.
     */
    fun getUserStats(): Flow<Resource<UserStats>>

    /**
     * Update user stats after completing an activity.
     */
    suspend fun updateStats(
        minutesLearned: Int = 0,
        chapterCompleted: Boolean = false,
        quizCompleted: Boolean = false
    ): Resource<UserStats>

    // ==================== Badges ====================

    /**
     * Get all user badges.
     */
    fun getUserBadges(): Flow<Resource<List<Badge>>>

    /**
     * Get badges by category.
     */
    fun getBadgesByCategory(category: String): Flow<Resource<List<Badge>>>

    /**
     * Check for new badge eligibility.
     */
    suspend fun checkBadgeEligibility(): Resource<List<Badge>>

    /**
     * Unlock a badge for the user.
     */
    suspend fun unlockBadge(badgeId: String): Resource<Badge>

    // ==================== Leaderboard Extended ====================

    /**
     * Get leaderboard by filter.
     * @param filter weekly, monthly, or all_time
     */
    fun getLeaderboard(filter: String): Flow<Resource<List<LeaderboardEntryData>>>

    /**
     * Get current user's rank.
     */
    fun getCurrentUserRank(filter: String): Flow<Resource<Int>>

    /**
     * Refresh leaderboard data.
     */
    suspend fun refreshLeaderboard(): Resource<Unit>
}

/**
 * Leaderboard entry data class for the repository.
 */
data class LeaderboardEntryData(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val xp: Int,
    val level: Int,
    val isCurrentUser: Boolean
)
