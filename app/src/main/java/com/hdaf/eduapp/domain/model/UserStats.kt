package com.hdaf.eduapp.domain.model

/**
 * Domain model for user statistics.
 */
data class UserStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val booksCompleted: Int,
    val chaptersCompleted: Int,
    val quizzesCompleted: Int,
    val totalMinutesLearned: Int,
    val leaderboardRank: Int,
    val totalXpEarned: Int,
    val averageQuizScore: Int
)
