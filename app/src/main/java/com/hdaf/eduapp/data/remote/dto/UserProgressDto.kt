package com.hdaf.eduapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * DTOs for User Progress API.
 */

data class UserProgressDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("total_xp") val totalXp: Int = 0,
    @SerializedName("level") val level: Int = 1,
    @SerializedName("xp_to_next_level") val xpToNextLevel: Int? = null,
    @SerializedName("current_streak") val currentStreak: Int = 0,
    @SerializedName("max_streak") val maxStreak: Int = 0,
    @SerializedName("quizzes_completed") val quizzesCompleted: Int = 0,
    @SerializedName("chapters_completed") val chaptersCompleted: Int = 0,
    @SerializedName("books_completed") val booksCompleted: Int = 0,
    @SerializedName("total_study_minutes") val totalStudyMinutes: Int = 0,
    @SerializedName("global_rank") val globalRank: Int? = null,
    @SerializedName("weekly_rank") val weeklyRank: Int? = null,
    @SerializedName("badges") val badges: List<UserBadgeDto> = emptyList(),
    @SerializedName("last_activity_date") val lastActivityDate: Date? = null
)

data class UserBadgeDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("badge_id") val badgeId: String,
    @SerializedName("badge_type") val badgeType: String,
    @SerializedName("rarity") val rarity: String = "COMMON",
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("earned_at") val earnedAt: Date = Date()
)

data class LeaderboardEntryDto(
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("total_xp") val totalXp: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("current_streak") val currentStreak: Int? = null,
    @SerializedName("is_current_user") val isCurrentUser: Boolean? = null
)

data class XpUpdateDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("xp_amount") val xpAmount: Int,
    @SerializedName("source") val source: String,
    @SerializedName("description") val description: String? = null
)

data class XpUpdateResponseDto(
    @SerializedName("new_total_xp") val newTotalXp: Int,
    @SerializedName("new_level") val newLevel: Int,
    @SerializedName("level_up") val levelUp: Boolean = false,
    @SerializedName("new_badges") val newBadges: List<UserBadgeDto>? = null
)

data class UserRankDto(
    @SerializedName("global_rank") val globalRank: Int,
    @SerializedName("weekly_rank") val weeklyRank: Int,
    @SerializedName("monthly_rank") val monthlyRank: Int,
    @SerializedName("percentile") val percentile: Float,
    @SerializedName("total_users") val totalUsers: Int
)
