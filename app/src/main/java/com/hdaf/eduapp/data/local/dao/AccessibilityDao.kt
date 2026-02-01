package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.AccessibilityProfileEntity
import com.hdaf.eduapp.data.local.entity.AIChatMessageEntity
import com.hdaf.eduapp.data.local.entity.OCRCacheEntity
import com.hdaf.eduapp.data.local.entity.StudyAnalyticsEntity
import com.hdaf.eduapp.data.local.entity.StudyRecommendationEntity
import com.hdaf.eduapp.data.local.entity.VoiceCommandEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessibility profile operations.
 */
@Dao
interface AccessibilityProfileDao {
    
    @Query("SELECT * FROM accessibility_profiles WHERE user_id = :userId")
    suspend fun getProfile(userId: String): AccessibilityProfileEntity?
    
    @Query("SELECT * FROM accessibility_profiles WHERE user_id = :userId")
    fun observeProfile(userId: String): Flow<AccessibilityProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AccessibilityProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: AccessibilityProfileEntity)
    
    @Query("DELETE FROM accessibility_profiles WHERE user_id = :userId")
    suspend fun deleteProfile(userId: String)
    
    @Query("UPDATE accessibility_profiles SET accessibility_mode = :mode, updated_at = :timestamp WHERE user_id = :userId")
    suspend fun updateAccessibilityMode(userId: String, mode: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE accessibility_profiles SET font_scale = :scale, updated_at = :timestamp WHERE user_id = :userId")
    suspend fun updateFontScale(userId: String, scale: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE accessibility_profiles SET contrast_level = :level, updated_at = :timestamp WHERE user_id = :userId")
    suspend fun updateContrastLevel(userId: String, level: String, timestamp: Long = System.currentTimeMillis())
}

/**
 * DAO for AI chat message operations.
 */
@Dao
interface AIChatDao {
    
    @Query("SELECT * FROM ai_chat_messages WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(userId: String, limit: Int = 50): List<AIChatMessageEntity>
    
    @Query("SELECT * FROM ai_chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun observeSessionMessages(sessionId: String): Flow<List<AIChatMessageEntity>>
    
    @Query("SELECT * FROM ai_chat_messages WHERE user_id = :userId AND context_subject = :subjectId ORDER BY timestamp DESC")
    suspend fun getMessagesBySubject(userId: String, subjectId: String): List<AIChatMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AIChatMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<AIChatMessageEntity>)
    
    @Query("DELETE FROM ai_chat_messages WHERE user_id = :userId AND session_id = :sessionId")
    suspend fun clearSession(userId: String, sessionId: String)
    
    @Query("DELETE FROM ai_chat_messages WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldMessages(beforeTimestamp: Long)
    
    @Query("SELECT * FROM ai_chat_messages WHERE is_cached = 1 AND message LIKE '%' || :query || '%' LIMIT 5")
    suspend fun searchCachedResponses(query: String): List<AIChatMessageEntity>
}

/**
 * DAO for study analytics.
 */
@Dao
interface StudyAnalyticsDao {
    
    @Query("SELECT * FROM study_analytics WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentAnalytics(userId: String, limit: Int = 100): List<StudyAnalyticsEntity>
    
    @Query("SELECT * FROM study_analytics WHERE user_id = :userId AND subject_id = :subjectId ORDER BY timestamp DESC")
    suspend fun getAnalyticsBySubject(userId: String, subjectId: String): List<StudyAnalyticsEntity>
    
    @Query("SELECT * FROM study_analytics WHERE user_id = :userId AND event_type = :eventType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAnalyticsByEventType(userId: String, eventType: String, limit: Int = 50): List<StudyAnalyticsEntity>
    
    @Query("SELECT AVG(score) FROM study_analytics WHERE user_id = :userId AND subject_id = :subjectId AND score IS NOT NULL")
    suspend fun getAverageScore(userId: String, subjectId: String): Float?
    
    @Query("SELECT SUM(mistakes_count) FROM study_analytics WHERE user_id = :userId AND subject_id = :subjectId")
    suspend fun getTotalMistakes(userId: String, subjectId: String): Int?
    
    @Query("SELECT SUM(duration_seconds) FROM study_analytics WHERE user_id = :userId AND timestamp > :sinceTimestamp")
    suspend fun getTotalStudyTime(userId: String, sinceTimestamp: Long): Int?
    
    @Query("SELECT content_type FROM study_analytics WHERE user_id = :userId GROUP BY content_type ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getPreferredContentType(userId: String): String?
    
    @Insert
    suspend fun insertAnalytics(analytics: StudyAnalyticsEntity): Long
    
    @Insert
    suspend fun insertAnalyticsBatch(analytics: List<StudyAnalyticsEntity>)
    
    @Query("DELETE FROM study_analytics WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldAnalytics(beforeTimestamp: Long)
}

/**
 * DAO for study recommendations.
 */
@Dao
interface StudyRecommendationDao {
    
    @Query("SELECT * FROM study_recommendations WHERE user_id = :userId AND is_completed = 0 AND expires_at > :currentTime ORDER BY priority DESC")
    suspend fun getActiveRecommendations(userId: String, currentTime: Long = System.currentTimeMillis()): List<StudyRecommendationEntity>
    
    @Query("SELECT * FROM study_recommendations WHERE user_id = :userId AND is_completed = 0 AND expires_at > :currentTime ORDER BY priority DESC")
    fun observeActiveRecommendations(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<StudyRecommendationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: StudyRecommendationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<StudyRecommendationEntity>)
    
    @Query("UPDATE study_recommendations SET is_completed = 1 WHERE id = :recommendationId")
    suspend fun markCompleted(recommendationId: String)
    
    @Query("DELETE FROM study_recommendations WHERE expires_at < :currentTime")
    suspend fun deleteExpiredRecommendations(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM study_recommendations WHERE user_id = :userId")
    suspend fun clearUserRecommendations(userId: String)
}

/**
 * DAO for OCR cache.
 */
@Dao
interface OCRCacheDao {
    
    @Query("SELECT * FROM ocr_cache WHERE image_hash = :imageHash LIMIT 1")
    suspend fun getCachedResult(imageHash: String): OCRCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: OCRCacheEntity)
    
    @Query("DELETE FROM ocr_cache WHERE created_at < :beforeTimestamp")
    suspend fun deleteOldCache(beforeTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM ocr_cache")
    suspend fun getCacheSize(): Int
}

/**
 * DAO for voice commands.
 */
@Dao
interface VoiceCommandDao {
    
    @Query("SELECT * FROM voice_commands WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentCommands(userId: String, limit: Int = 20): List<VoiceCommandEntity>
    
    @Query("SELECT recognized_action FROM voice_commands WHERE user_id = :userId AND was_successful = 1 GROUP BY recognized_action ORDER BY COUNT(*) DESC")
    suspend fun getMostUsedCommands(userId: String): List<String>
    
    @Insert
    suspend fun insertCommand(command: VoiceCommandEntity)
    
    @Query("DELETE FROM voice_commands WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldCommands(beforeTimestamp: Long)
}
