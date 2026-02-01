package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for badge operations.
 */
@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, unlockedAt DESC")
    fun getUserBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE category = :category ORDER BY isUnlocked DESC")
    fun getBadgesByCategory(category: String): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: String): BadgeEntity?

    @Query("SELECT * FROM badges WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Update
    suspend fun updateBadge(badge: BadgeEntity)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :badgeId")
    suspend fun unlockBadge(badgeId: String, unlockedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteBadge(badge: BadgeEntity)

    @Query("DELETE FROM badges")
    suspend fun deleteAllBadges()

    @Query("SELECT COUNT(*) FROM badges WHERE isUnlocked = 1")
    suspend fun getUnlockedBadgeCount(): Int
}
