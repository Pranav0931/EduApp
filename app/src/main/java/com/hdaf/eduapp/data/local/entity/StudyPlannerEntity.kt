package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for study sessions.
 */
@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["scheduledDate"]),
        Index(value = ["subjectId"]),
        Index(value = ["chapterId"]),
        Index(value = ["isCompleted"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subjectId: String? = null,
    val chapterId: String? = null,
    val scheduledDate: Long,
    val startTime: String, // HH:mm format
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val actualDurationMinutes: Int? = null,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for study plans.
 */
@Entity(
    tableName = "study_plans",
    indices = [
        Index(value = ["isActive"])
    ]
)
data class StudyPlanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val weeklyGoalMinutes: Int,
    val preferredDays: String, // Comma-separated list: "MONDAY,TUESDAY,WEDNESDAY"
    val preferredStartTime: String, // HH:mm format
    val sessionDurationMinutes: Int = 30,
    val breakDurationMinutes: Int = 5,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for study goals.
 */
@Entity(
    tableName = "study_goals",
    indices = [
        Index(value = ["targetDate"]),
        Index(value = ["isCompleted"])
    ]
)
data class StudyGoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String? = null,
    val targetDate: Long,
    val targetMinutes: Int? = null,
    val targetChapters: Int? = null,
    val targetQuizScore: Int? = null,
    val progressMinutes: Int = 0,
    val progressChapters: Int = 0,
    val progressQuizScore: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for daily study summaries (aggregated stats).
 */
@Entity(
    tableName = "daily_study_summaries",
    indices = [
        Index(value = ["date"], unique = true)
    ]
)
data class DailyStudySummaryEntity(
    @PrimaryKey
    val date: Long, // Date at midnight (e.g., 2024-01-15T00:00:00)
    val totalMinutesStudied: Int = 0,
    val chaptersCompleted: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageQuizScore: Float = 0f,
    val xpEarned: Int = 0,
    val badgesEarned: Int = 0,
    val streak: Int = 0
)
