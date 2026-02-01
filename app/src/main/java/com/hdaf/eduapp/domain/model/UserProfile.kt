package com.hdaf.eduapp.domain.model

/**
 * User profile model.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val avatarUrl: String?,
    val classLevel: Int,
    val medium: String,
    val xp: Int,
    val level: Int,
    val xpToNextLevel: Int,
    val streakDays: Int,
    val leaderboardRank: Int
)
