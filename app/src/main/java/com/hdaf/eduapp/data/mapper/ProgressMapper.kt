package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.local.entity.UserBadgeEntity
import com.hdaf.eduapp.data.local.entity.UserProgressEntity
import com.hdaf.eduapp.data.remote.dto.LeaderboardEntryDto
import com.hdaf.eduapp.data.remote.dto.UserBadgeDto
import com.hdaf.eduapp.data.remote.dto.UserProgressDto
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.BadgeCategory
import com.hdaf.eduapp.domain.model.LeaderboardEntry
import com.hdaf.eduapp.domain.model.UserProgress
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mappers for UserProgress and Badge conversions between layers.
 */
@Singleton
class ProgressMapper @Inject constructor() {

    // ==================== UserProgress: DTO -> Entity ====================

    fun dtoToEntity(dto: UserProgressDto): UserProgressEntity {
        return UserProgressEntity(
            id = dto.id ?: UUID.randomUUID().toString(),
            userId = dto.userId,
            totalXp = dto.totalXp,
            level = dto.level,
            xpToNextLevel = dto.xpToNextLevel ?: 150,
            currentStreak = dto.currentStreak,
            maxStreak = dto.maxStreak,
            quizzesCompleted = dto.quizzesCompleted,
            chaptersCompleted = dto.chaptersCompleted,
            booksCompleted = dto.booksCompleted,
            totalStudyTimeMinutes = dto.totalStudyMinutes,
            globalRank = dto.globalRank ?: 0,
            weeklyRank = dto.weeklyRank ?: 0,
            lastActivityDate = dto.lastActivityDate,
            lastSyncDate = Date(),
            isSynced = true
        )
    }

    // ==================== UserProgress: Entity -> Domain ====================

    fun entityToDomain(
        entity: UserProgressEntity,
        badges: List<UserBadgeEntity> = emptyList()
    ): UserProgress {
        return UserProgress(
            userId = entity.userId,
            totalXp = entity.totalXp,
            level = entity.level,
            xpToNextLevel = entity.xpToNextLevel,
            currentStreak = entity.currentStreak,
            maxStreak = entity.maxStreak,
            quizzesCompleted = entity.quizzesCompleted,
            chaptersCompleted = entity.chaptersCompleted,
            booksCompleted = entity.booksCompleted,
            totalStudyTimeMinutes = entity.totalStudyTimeMinutes,
            globalRank = entity.globalRank,
            weeklyRank = entity.weeklyRank,
            badges = badges.map { badgeEntityToDomain(it) },
            lastActivityDate = entity.lastActivityDate
        )
    }

    // ==================== UserProgress: DTO -> Domain ====================

    fun dtoToDomain(dto: UserProgressDto): UserProgress {
        return UserProgress(
            userId = dto.userId,
            totalXp = dto.totalXp,
            level = dto.level,
            xpToNextLevel = dto.xpToNextLevel ?: 150,
            currentStreak = dto.currentStreak,
            maxStreak = dto.maxStreak,
            quizzesCompleted = dto.quizzesCompleted,
            chaptersCompleted = dto.chaptersCompleted,
            booksCompleted = dto.booksCompleted,
            totalStudyTimeMinutes = dto.totalStudyMinutes,
            globalRank = dto.globalRank ?: 0,
            weeklyRank = dto.weeklyRank ?: 0,
            badges = dto.badges.map { badgeDtoToDomain(it) },
            lastActivityDate = dto.lastActivityDate
        )
    }

    // ==================== Badge: DTO -> Entity ====================

    fun badgeDtoToEntity(dto: UserBadgeDto): UserBadgeEntity {
        return UserBadgeEntity(
            id = dto.id,
            userId = dto.userId,
            badgeId = dto.badgeId,
            badgeType = dto.badgeType,
            rarity = dto.rarity,
            title = dto.title,
            description = dto.description,
            iconUrl = dto.iconUrl,
            earnedAt = dto.earnedAt,
            isSynced = true
        )
    }

    // ==================== Badge: Entity -> Domain ====================

    fun badgeEntityToDomain(entity: UserBadgeEntity): Badge {
        return Badge(
            id = entity.badgeId,
            name = entity.title,
            description = entity.description,
            iconUrl = entity.iconUrl,
            isUnlocked = true,
            unlockedAt = entity.earnedAt.time,
            category = mapBadgeTypeToCategory(entity.badgeType)
        )
    }

    // ==================== Badge: DTO -> Domain ====================

    fun badgeDtoToDomain(dto: UserBadgeDto): Badge {
        return Badge(
            id = dto.badgeId,
            name = dto.title,
            description = dto.description,
            iconUrl = dto.iconUrl,
            isUnlocked = true,
            unlockedAt = dto.earnedAt.time,
            category = mapBadgeTypeToCategory(dto.badgeType)
        )
    }
    
    private fun mapBadgeTypeToCategory(badgeType: String): BadgeCategory {
        return when (badgeType.uppercase()) {
            "STREAK" -> BadgeCategory.STREAK
            "LEARNING" -> BadgeCategory.LEARNING
            "QUIZ" -> BadgeCategory.QUIZ
            "ACHIEVEMENT" -> BadgeCategory.ACHIEVEMENT
            "SOCIAL" -> BadgeCategory.SOCIAL
            else -> BadgeCategory.ACHIEVEMENT
        }
    }

    // ==================== Leaderboard Mappers ====================

    fun leaderboardDtoToDomain(dto: LeaderboardEntryDto): LeaderboardEntry {
        return LeaderboardEntry(
            userId = dto.userId,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            totalXp = dto.totalXp,
            level = dto.level,
            rank = dto.rank ?: 0,
            currentStreak = dto.currentStreak ?: 0,
            isCurrentUser = dto.isCurrentUser ?: false
        )
    }
}
