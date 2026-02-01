package com.hdaf.eduapp.data.remote.api

import com.hdaf.eduapp.data.remote.dto.QuizAttemptDto
import com.hdaf.eduapp.data.remote.dto.QuizDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Quiz endpoints.
 */
interface QuizApi {

    /**
     * Get available quizzes for a subject.
     */
    @GET("quizzes")
    suspend fun getQuizzesBySubject(
        @Query("subject") subject: String,
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<List<QuizDto>>

    /**
     * Get quiz by ID with questions.
     */
    @GET("quizzes/{quizId}")
    suspend fun getQuizById(
        @Path("quizId") quizId: String
    ): Response<QuizDto>

    /**
     * Generate AI quiz for a topic.
     */
    @POST("quizzes/generate")
    suspend fun generateQuiz(
        @Body request: GenerateQuizRequest
    ): Response<QuizDto>

    /**
     * Submit quiz attempt.
     */
    @POST("quizzes/attempts")
    suspend fun submitAttempt(
        @Body attempt: QuizAttemptDto
    ): Response<QuizAttemptDto>

    /**
     * Get user's quiz attempts.
     */
    @GET("quizzes/attempts")
    suspend fun getUserAttempts(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<QuizAttemptDto>>

    /**
     * Get quiz statistics for user.
     */
    @GET("quizzes/stats/{userId}")
    suspend fun getQuizStats(
        @Path("userId") userId: String
    ): Response<QuizStatsResponse>
}

/**
 * Request body for AI quiz generation.
 */
data class GenerateQuizRequest(
    val subject: String,
    val chapter: String? = null,
    val topic: String? = null,
    val difficulty: String = "medium",
    val questionCount: Int = 10,
    val classLevel: Int = 5
)

/**
 * Response for quiz statistics.
 */
data class QuizStatsResponse(
    val totalAttempts: Int,
    val averageScore: Float,
    val perfectScores: Int,
    val subjectBreakdown: Map<String, SubjectStats>
)

data class SubjectStats(
    val attempts: Int,
    val averageScore: Float,
    val weakTopics: List<String>
)
