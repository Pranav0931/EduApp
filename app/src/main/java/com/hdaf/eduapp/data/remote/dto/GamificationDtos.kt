package com.hdaf.eduapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * User stats DTO.
 */
data class UserStatsDto(
    @SerializedName("current_streak") val currentStreak: Int,
    @SerializedName("max_streak") val maxStreak: Int,
    @SerializedName("books_completed") val booksCompleted: Int,
    @SerializedName("chapters_completed") val chaptersCompleted: Int,
    @SerializedName("quizzes_completed") val quizzesCompleted: Int,
    @SerializedName("minutes_learned") val minutesLearned: Int,
    @SerializedName("leaderboard_rank") val leaderboardRank: Int,
    @SerializedName("total_xp") val totalXp: Int,
    @SerializedName("current_level") val currentLevel: Int
)

/**
 * Badge DTO.
 */
data class BadgeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("category") val category: String,
    @SerializedName("xp_reward") val xpReward: Int,
    @SerializedName("criteria") val criteria: String,
    @SerializedName("is_unlocked") val isUnlocked: Boolean = false,
    @SerializedName("unlocked_at") val unlockedAt: Long? = null
)

/**
 * Leaderboard response DTO.
 */
data class LeaderboardResponseDto(
    @SerializedName("entries") val entries: List<LeaderboardEntryDto>,
    @SerializedName("total_count") val totalCount: Int
)

// Note: LeaderboardEntryDto and UserRankDto are defined in UserProgressDto.kt
