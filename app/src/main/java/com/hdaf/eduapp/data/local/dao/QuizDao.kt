package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hdaf.eduapp.data.local.entity.QuizAttemptEntity
import com.hdaf.eduapp.data.local.entity.QuizEntity
import com.hdaf.eduapp.data.local.entity.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Quiz operations.
 */
@Dao
interface QuizDao {

    // ==================== Quiz Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: String): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE chapter_id = :chapterId")
    suspend fun getQuizzesByChapterId(chapterId: String): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE chapter_id = :chapterId")
    fun getQuizzesByChapter(chapterId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE subject = :subject ORDER BY created_at DESC")
    suspend fun getQuizzesBySubject(subject: String): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE subject = :subject ORDER BY created_at DESC")
    fun getQuizzesBySubjectFlow(subject: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes ORDER BY created_at DESC LIMIT :limit")
    fun getRecentQuizzes(limit: Int = 10): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes ORDER BY created_at DESC")
    suspend fun getAllQuizzes(): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE title LIKE :query OR subject LIKE :query")
    suspend fun searchQuizzes(query: String): List<QuizEntity>

    // ==================== Question Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quiz_questions WHERE quiz_id = :quizId ORDER BY order_index ASC")
    suspend fun getQuestionsByQuizId(quizId: String): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE quiz_id = :quizId ORDER BY order_index ASC")
    suspend fun getQuestionsByQuiz(quizId: String): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE quiz_id = :quizId ORDER BY order_index ASC")
    fun getQuestionsByQuizFlow(quizId: String): Flow<List<QuizQuestionEntity>>

    @Query("SELECT COUNT(*) FROM quiz_questions WHERE quiz_id = :quizId")
    suspend fun getQuestionCount(quizId: String): Int

    // ==================== Attempt Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts WHERE user_id = :userId ORDER BY attempted_at DESC")
    suspend fun getAttemptsByUserId(userId: String): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE user_id = :userId ORDER BY attempted_at DESC")
    fun getAttemptsByUser(userId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quiz_id = :quizId ORDER BY attempted_at DESC")
    suspend fun getAttemptsByQuizId(quizId: String): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE user_id = :userId AND quiz_id = :quizId ORDER BY attempted_at DESC")
    fun getAttemptsByUserAndQuiz(userId: String, quizId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE subject = :subject ORDER BY attempted_at DESC")
    suspend fun getAttemptsBySubject(subject: String): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE user_id = :userId AND subject = :subject ORDER BY attempted_at DESC")
    fun getAttemptsByUserAndSubject(userId: String, subject: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY attempted_at DESC LIMIT :limit")
    suspend fun getRecentAttempts(limit: Int = 10): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE user_id = :userId ORDER BY attempted_at DESC LIMIT :limit")
    fun getRecentAttemptsFlow(userId: String, limit: Int = 10): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quiz_id = :quizId ORDER BY score_percentage DESC LIMIT 1")
    suspend fun getBestAttemptForQuiz(quizId: String): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE is_synced = 0")
    suspend fun getUnsyncedAttempts(): List<QuizAttemptEntity>

    @Query("UPDATE quiz_attempts SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAttemptsAsSynced(ids: List<String>)

    // ==================== Analytics Queries ====================

    @Query("SELECT AVG(score_percentage) FROM quiz_attempts WHERE user_id = :userId")
    suspend fun getAverageScore(userId: String): Float?

    @Query("SELECT AVG(score_percentage) FROM quiz_attempts WHERE user_id = :userId AND subject = :subject")
    suspend fun getAverageScoreBySubject(userId: String, subject: String): Float?

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE user_id = :userId AND is_completed = 1")
    suspend fun getCompletedQuizCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE user_id = :userId AND score_percentage = 100")
    suspend fun getPerfectScoreCount(userId: String): Int

    @Query("""
        SELECT subject, COUNT(*) as count, AVG(score_percentage) as avg_score 
        FROM quiz_attempts 
        WHERE user_id = :userId AND is_completed = 1 
        GROUP BY subject
    """)
    suspend fun getSubjectStats(userId: String): List<SubjectStatsResult>

    // ==================== Cleanup ====================

    @Query("DELETE FROM quizzes")
    suspend fun deleteAllQuizzes()

    @Query("DELETE FROM quiz_questions")
    suspend fun deleteAllQuestions()

    @Query("DELETE FROM quiz_attempts WHERE user_id = :userId")
    suspend fun deleteUserAttempts(userId: String)

    // ==================== Transaction Operations ====================

    @Transaction
    suspend fun insertQuizWithQuestions(quiz: QuizEntity, questions: List<QuizQuestionEntity>) {
        insertQuiz(quiz)
        insertQuestions(questions)
    }
}

/**
 * Result class for subject statistics query.
 */
data class SubjectStatsResult(
    val subject: String,
    val count: Int,
    val avg_score: Float
)
