package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAnalytics
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.Subject
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quiz operations.
 * Supports AI-generated quizzes and analytics.
 */
interface QuizRepository {

    // ==================== Quiz Fetching ====================

    /**
     * Get all available quizzes.
     */
    fun getAllQuizzes(): Flow<Resource<List<Quiz>>>

    /**
     * Get quizzes for a specific chapter.
     */
    fun getQuizzesByChapter(chapterId: String): Flow<Resource<List<Quiz>>>

    /**
     * Get quizzes by subject.
     */
    fun getQuizzesBySubject(subject: Subject): Flow<Resource<List<Quiz>>>

    /**
     * Get a specific quiz by ID with all questions.
     */
    fun getQuizById(quizId: String): Flow<Resource<Quiz>>

    /**
     * Search quizzes by title or topic.
     */
    fun searchQuizzes(query: String): Flow<Resource<List<Quiz>>>

    // ==================== AI Quiz Generation ====================

    /**
     * Generate a quiz using AI based on chapter content.
     */
    suspend fun generateAiQuiz(
        chapterId: String,
        numberOfQuestions: Int = 10,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    ): Resource<Quiz>

    /**
     * Generate a practice quiz based on weak topics.
     */
    suspend fun generatePracticeQuiz(
        subject: Subject,
        weakTopics: List<String>,
        numberOfQuestions: Int = 10
    ): Resource<Quiz>

    /**
     * Generate adaptive quiz based on user's performance.
     */
    suspend fun generateAdaptiveQuiz(
        subject: Subject,
        userId: String
    ): Resource<Quiz>

    // ==================== Quiz Attempts ====================

    /**
     * Submit a quiz attempt.
     */
    suspend fun submitQuizAttempt(
        quizId: String,
        answers: List<Int>,
        timeTakenSeconds: Int
    ): Resource<QuizAttempt>

    /**
     * Get all quiz attempts for a user.
     */
    fun getQuizAttempts(userId: String): Flow<Resource<List<QuizAttempt>>>

    /**
     * Get attempts for a specific quiz.
     */
    fun getQuizAttemptsByQuiz(quizId: String): Flow<Resource<List<QuizAttempt>>>

    /**
     * Get recent quiz attempts.
     */
    fun getRecentAttempts(limit: Int = 10): Flow<List<QuizAttempt>>

    /**
     * Get best attempt for a quiz.
     */
    suspend fun getBestAttempt(quizId: String): Resource<QuizAttempt?>

    // ==================== Analytics ====================

    /**
     * Get quiz analytics for a subject.
     */
    suspend fun getSubjectAnalytics(subject: Subject): Resource<QuizAnalytics>

    /**
     * Get overall quiz analytics for user.
     */
    suspend fun getOverallAnalytics(): Resource<List<QuizAnalytics>>

    /**
     * Get weak topics across all subjects.
     */
    suspend fun getWeakTopics(): Resource<Map<Subject, List<String>>>

    /**
     * Get average score by subject.
     */
    fun getAverageScoreBySubject(): Flow<Map<Subject, Float>>

    // ==================== Offline Support ====================

    /**
     * Cache quiz for offline use.
     */
    suspend fun cacheQuiz(quizId: String): Resource<Unit>

    /**
     * Get cached quizzes.
     */
    fun getCachedQuizzes(): Flow<List<Quiz>>

    /**
     * Sync offline attempts with server.
     */
    suspend fun syncOfflineAttempts(): Resource<Int>
}
