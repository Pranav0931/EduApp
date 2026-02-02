package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdaf.eduapp.data.local.entity.ChapterAudioProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chapter audio progress.
 * Provides offline-first audio progress persistence.
 */
@Dao
interface ChapterAudioProgressDao {
    
    /**
     * Get audio progress for a specific chapter.
     */
    @Query("SELECT * FROM chapter_audio_progress WHERE chapterId = :chapterId")
    suspend fun getProgress(chapterId: String): ChapterAudioProgressEntity?
    
    /**
     * Get audio progress as a Flow for reactive updates.
     */
    @Query("SELECT * FROM chapter_audio_progress WHERE chapterId = :chapterId")
    fun observeProgress(chapterId: String): Flow<ChapterAudioProgressEntity?>
    
    /**
     * Insert or update audio progress.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ChapterAudioProgressEntity)
    
    /**
     * Update only the position for quick saves during playback.
     */
    @Query("""
        UPDATE chapter_audio_progress 
        SET positionMs = :positionMs, lastPlayedAt = :timestamp 
        WHERE chapterId = :chapterId
    """)
    suspend fun updatePosition(chapterId: String, positionMs: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Mark chapter audio as completed.
     */
    @Query("""
        UPDATE chapter_audio_progress 
        SET isCompleted = 1, playCount = playCount + 1 
        WHERE chapterId = :chapterId
    """)
    suspend fun markCompleted(chapterId: String)
    
    /**
     * Reset progress for a chapter (start over).
     */
    @Query("""
        UPDATE chapter_audio_progress 
        SET positionMs = 0, isCompleted = 0 
        WHERE chapterId = :chapterId
    """)
    suspend fun resetProgress(chapterId: String)
    
    /**
     * Get all in-progress chapters (started but not completed).
     */
    @Query("""
        SELECT * FROM chapter_audio_progress 
        WHERE positionMs > 0 AND isCompleted = 0 
        ORDER BY lastPlayedAt DESC
    """)
    fun getInProgressChapters(): Flow<List<ChapterAudioProgressEntity>>
    
    /**
     * Get recently played chapters.
     */
    @Query("""
        SELECT * FROM chapter_audio_progress 
        ORDER BY lastPlayedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyPlayed(limit: Int = 10): Flow<List<ChapterAudioProgressEntity>>
    
    /**
     * Get all completed chapters for a book.
     */
    @Query("""
        SELECT * FROM chapter_audio_progress 
        WHERE bookId = :bookId AND isCompleted = 1
    """)
    suspend fun getCompletedChaptersForBook(bookId: String): List<ChapterAudioProgressEntity>
    
    /**
     * Get total listen time for statistics.
     */
    @Query("SELECT SUM(totalListenTimeSeconds) FROM chapter_audio_progress")
    suspend fun getTotalListenTime(): Long?
    
    /**
     * Update playback speed for a chapter.
     */
    @Query("UPDATE chapter_audio_progress SET playbackSpeed = :speed WHERE chapterId = :chapterId")
    suspend fun updatePlaybackSpeed(chapterId: String, speed: Float)
    
    /**
     * Delete all audio progress (for account reset).
     */
    @Query("DELETE FROM chapter_audio_progress")
    suspend fun deleteAll()
}
