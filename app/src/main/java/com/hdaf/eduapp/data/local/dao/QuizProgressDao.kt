package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hdaf.eduapp.data.local.entity.QuizProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing in-progress quiz state.
 * Enables quiz resumption after app interruption.
 */
@Dao
interface QuizProgressDao {
    
    /**
     * Get progress for a specific quiz.
     */
    @Query("SELECT * FROM quiz_progress WHERE quizId = :quizId")
    suspend fun getProgress(quizId: String): QuizProgressEntity?
    
    /**
     * Get all in-progress quizzes.
     */
    @Query("SELECT * FROM quiz_progress WHERE quizPhase = 'IN_PROGRESS' ORDER BY lastUpdatedAt DESC")
    fun getInProgressQuizzes(): Flow<List<QuizProgressEntity>>
    
    /**
     * Get in-progress quiz for a specific chapter.
     */
    @Query("SELECT * FROM quiz_progress WHERE chapterId = :chapterId AND quizPhase = 'IN_PROGRESS' LIMIT 1")
    suspend fun getProgressForChapter(chapterId: String): QuizProgressEntity?
    
    /**
     * Save or update quiz progress.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: QuizProgressEntity)
    
    /**
     * Delete progress for a quiz (when completed or abandoned).
     */
    @Query("DELETE FROM quiz_progress WHERE quizId = :quizId")
    suspend fun deleteProgress(quizId: String)
    
    /**
     * Delete all in-progress quizzes for a chapter.
     */
    @Query("DELETE FROM quiz_progress WHERE chapterId = :chapterId")
    suspend fun deleteProgressForChapter(chapterId: String)
    
    /**
     * Delete old incomplete quizzes (older than 24 hours).
     */
    @Query("DELETE FROM quiz_progress WHERE lastUpdatedAt < :cutoffTime AND quizPhase != 'COMPLETED'")
    suspend fun deleteOldProgress(cutoffTime: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    
    /**
     * Check if there's an in-progress quiz for a chapter.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM quiz_progress WHERE chapterId = :chapterId AND quizPhase = 'IN_PROGRESS')")
    suspend fun hasInProgressQuiz(chapterId: String): Boolean
}
