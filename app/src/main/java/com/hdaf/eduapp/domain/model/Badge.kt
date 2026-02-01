package com.hdaf.eduapp.domain.model

/**
 * Domain model for user badge.
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val category: BadgeCategory
)

enum class BadgeCategory {
    STREAK,
    LEARNING,
    QUIZ,
    ACHIEVEMENT,
    SOCIAL
}
