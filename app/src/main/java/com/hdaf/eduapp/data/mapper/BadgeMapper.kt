package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.local.entity.BadgeEntity
import com.hdaf.eduapp.data.remote.dto.BadgeDto
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.BadgeCategory
import javax.inject.Inject

/**
 * Mapper for Badge conversions between layers.
 */
class BadgeMapper @Inject constructor() {

    fun toDomain(entity: BadgeEntity): Badge {
        return Badge(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            iconUrl = entity.iconUrl,
            isUnlocked = entity.isUnlocked,
            unlockedAt = entity.unlockedAt,
            category = BadgeCategory.valueOf(entity.category.uppercase())
        )
    }

    fun toEntity(badge: Badge): BadgeEntity {
        return BadgeEntity(
            id = badge.id,
            name = badge.name,
            description = badge.description,
            iconUrl = badge.iconUrl,
            category = badge.category.name,
            xpReward = 0,
            criteria = "",
            isUnlocked = badge.isUnlocked,
            unlockedAt = badge.unlockedAt
        )
    }

    fun toDomain(dto: BadgeDto): Badge {
        return Badge(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            iconUrl = dto.iconUrl,
            isUnlocked = dto.isUnlocked,
            unlockedAt = dto.unlockedAt,
            category = try { 
                BadgeCategory.valueOf(dto.category.uppercase()) 
            } catch (e: Exception) { 
                BadgeCategory.ACHIEVEMENT 
            }
        )
    }

    fun toEntity(dto: BadgeDto): BadgeEntity {
        return BadgeEntity(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            iconUrl = dto.iconUrl,
            category = dto.category,
            xpReward = dto.xpReward,
            criteria = "",
            isUnlocked = dto.isUnlocked,
            unlockedAt = dto.unlockedAt
        )
    }
}
