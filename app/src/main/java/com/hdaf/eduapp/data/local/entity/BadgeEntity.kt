package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Badge entity for Room database.
 */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val category: String,
    val xpReward: Int,
    val criteria: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
