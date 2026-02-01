package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.hdaf.eduapp.data.local.entity.UserBadgeEntity
import com.hdaf.eduapp.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for User Progress and Gamification.
 */
@Dao
interface UserProgressDao {

    // ==================== User Progress ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)

    @Upsert
    suspend fun upsertProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    suspend fun getProgressByUser(userId: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    suspend fun getProgressForUser(userId: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    fun getProgressByUserFlow(userId: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    fun observeProgressForUser(userId: String): Flow<UserProgressEntity?>

    @Query("UPDATE user_progress SET total_xp = total_xp + :xp, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun addXp(userId: String, xp: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET current_level = :level, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateLevel(userId: String, level: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET current_streak = :streak, max_streak = MAX(max_streak, :streak), last_activity_date = :date, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateStreak(userId: String, streak: Int, date: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET quizzes_completed = quizzes_completed + 1, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun incrementQuizzesCompleted(userId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET chapters_completed = chapters_completed + 1, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun incrementLessonsCompleted(userId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET perfect_scores = perfect_scores + 1, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun incrementPerfectScores(userId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_progress SET total_study_minutes = total_study_minutes + :minutes, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun addStudyMinutes(userId: String, minutes: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM user_progress WHERE is_synced = 0")
    suspend fun getUnsyncedProgress(): List<UserProgressEntity>

    @Query("UPDATE user_progress SET is_synced = 1 WHERE user_id = :userId")
    suspend fun markAsSynced(userId: String)

    @Query("DELETE FROM user_progress WHERE user_id = :userId")
    suspend fun deleteProgress(userId: String)

    // ==================== Daily Goal Tracking ====================

    @Query("SELECT quizzes_completed FROM user_progress WHERE user_id = :userId AND updated_at >= :sinceDate")
    suspend fun getQuizzesCompletedSince(userId: String, sinceDate: Date): Int

    @Query("SELECT total_xp FROM user_progress WHERE user_id = :userId AND updated_at >= :sinceDate")
    suspend fun getXpEarnedSince(userId: String, sinceDate: Date): Int

    // ==================== Badges ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: UserBadgeEntity): Long

    @Upsert
    suspend fun upsertBadge(badge: UserBadgeEntity)

    @Query("SELECT * FROM user_badges WHERE user_id = :userId ORDER BY earned_at DESC")
    fun getBadgesByUser(userId: String): Flow<List<UserBadgeEntity>>

    @Query("SELECT * FROM user_badges WHERE user_id = :userId")
    suspend fun getBadgesByUserSync(userId: String): List<UserBadgeEntity>

    @Query("SELECT * FROM user_badges WHERE user_id = :userId")
    suspend fun getBadgesForUser(userId: String): List<UserBadgeEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM user_badges WHERE user_id = :userId AND badge_id = :badgeId)")
    suspend fun hasBadge(userId: String, badgeId: String): Boolean

    @Query("SELECT COUNT(*) FROM user_badges WHERE user_id = :userId")
    suspend fun getBadgeCount(userId: String): Int

    @Query("SELECT * FROM user_badges WHERE is_synced = 0")
    suspend fun getUnsyncedBadges(): List<UserBadgeEntity>

    @Query("UPDATE user_badges SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markBadgesAsSynced(ids: List<Long>)

    @Query("DELETE FROM user_badges WHERE user_id = :userId")
    suspend fun deleteUserBadges(userId: String)

    // ==================== Leaderboard ====================

    @Query("SELECT * FROM user_progress ORDER BY total_xp DESC LIMIT :limit")
    fun getLeaderboard(limit: Int = 100): Flow<List<UserProgressEntity>>

    @Query("SELECT COUNT(*) + 1 FROM user_progress WHERE total_xp > (SELECT total_xp FROM user_progress WHERE user_id = :userId)")
    suspend fun getUserRank(userId: String): Int
}
