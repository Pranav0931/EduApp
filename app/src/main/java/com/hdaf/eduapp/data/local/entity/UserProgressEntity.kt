package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity for User Progress (Gamification).
 */
@Entity(
    tableName = "user_progress",
    indices = [Index(value = ["user_id"], unique = true)]
)
data class UserProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "total_xp")
    val totalXp: Int = 0,
    
    @ColumnInfo(name = "current_level")
    val level: Int = 1,
    
    @ColumnInfo(name = "xp_to_next_level")
    val xpToNextLevel: Int = 150,
    
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,
    
    @ColumnInfo(name = "max_streak")
    val maxStreak: Int = 0,
    
    @ColumnInfo(name = "quizzes_completed")
    val quizzesCompleted: Int = 0,
    
    @ColumnInfo(name = "chapters_completed")
    val chaptersCompleted: Int = 0,
    
    @ColumnInfo(name = "books_completed")
    val booksCompleted: Int = 0,
    
    @ColumnInfo(name = "total_study_minutes")
    val totalStudyTimeMinutes: Int = 0,
    
    @ColumnInfo(name = "perfect_scores")
    val perfectScores: Int = 0,
    
    // Subject-specific quiz counts
    @ColumnInfo(name = "math_quizzes")
    val mathQuizzes: Int = 0,
    
    @ColumnInfo(name = "science_quizzes")
    val scienceQuizzes: Int = 0,
    
    @ColumnInfo(name = "english_quizzes")
    val englishQuizzes: Int = 0,
    
    @ColumnInfo(name = "hindi_quizzes")
    val hindiQuizzes: Int = 0,
    
    @ColumnInfo(name = "social_science_quizzes")
    val socialScienceQuizzes: Int = 0,
    
    // Leaderboard ranks
    @ColumnInfo(name = "global_rank")
    val globalRank: Int = 0,
    
    @ColumnInfo(name = "weekly_rank")
    val weeklyRank: Int = 0,
    
    @ColumnInfo(name = "last_activity_date")
    val lastActivityDate: Date? = null,
    
    @ColumnInfo(name = "last_sync_date")
    val lastSyncDate: Date? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)

/**
 * Room Entity for User Badge.
 */
@Entity(
    tableName = "user_badges",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["badge_id"]),
        Index(value = ["user_id", "badge_id"], unique = true)
    ]
)
data class UserBadgeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "badge_id")
    val badgeId: String,
    
    @ColumnInfo(name = "badge_type")
    val badgeType: String,
    
    @ColumnInfo(name = "rarity")
    val rarity: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "icon_url")
    val iconUrl: String,
    
    @ColumnInfo(name = "earned_at")
    val earnedAt: Date = Date(),
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)
