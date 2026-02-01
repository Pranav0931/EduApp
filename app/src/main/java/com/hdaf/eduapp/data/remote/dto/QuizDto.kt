package com.hdaf.eduapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs for Quiz API.
 */

data class QuizDto(
    @SerializedName("id") val id: String,
    @SerializedName("chapter_id") val chapterId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("difficulty") val difficulty: String = "medium",
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("time_limit_minutes") val timeLimitMinutes: Int = 10,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean = false,
    @SerializedName("questions") val questions: List<QuizQuestionDto>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class QuizQuestionDto(
    @SerializedName("id") val id: String,
    @SerializedName("quiz_id") val quizId: String,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correct_answer_index") val correctAnswerIndex: Int,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("difficulty") val difficulty: String = "medium",
    @SerializedName("order_index") val orderIndex: Int = 0
)

data class QuizAttemptDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("quiz_id") val quizId: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    @SerializedName("score_percentage") val scorePercentage: Float,
    @SerializedName("time_taken_seconds") val timeTakenSeconds: Int,
    @SerializedName("weak_topics") val weakTopics: List<String>? = null,
    @SerializedName("answers") val answers: List<Int>? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = true,
    @SerializedName("attempted_at") val attemptedAt: String? = null
)
