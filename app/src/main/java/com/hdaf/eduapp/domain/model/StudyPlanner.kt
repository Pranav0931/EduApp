package com.hdaf.eduapp.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain model for a study session plan.
 */
data class StudySession(
    val id: String,
    val title: String,
    val subjectId: String,
    val chapterId: String? = null,
    val scheduledDate: LocalDate,
    val startTime: LocalTime,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Weekly study plan configuration.
 */
data class StudyPlan(
    val id: String,
    val name: String,
    val weeklyGoalMinutes: Int = 300, // 5 hours default
    val preferredDays: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    ),
    val preferredStartTime: LocalTime = LocalTime.of(16, 0), // 4 PM default
    val sessionDurationMinutes: Int = 30,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Study goal tracking.
 */
data class StudyGoal(
    val id: String,
    val title: String,
    val targetDate: LocalDate,
    val targetMinutes: Int? = null,
    val targetChapters: Int? = null,
    val targetQuizScore: Int? = null,
    val progressPercent: Float = 0f,
    val isCompleted: Boolean = false
)

/**
 * Daily study summary.
 */
data class DailyStudySummary(
    val date: LocalDate,
    val totalMinutesStudied: Int,
    val chaptersCompleted: Int,
    val quizzesCompleted: Int,
    val averageQuizScore: Int,
    val streakDays: Int
)
