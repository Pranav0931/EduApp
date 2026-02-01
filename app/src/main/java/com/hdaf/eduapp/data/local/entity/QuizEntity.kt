package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity for Quiz.
 */
@Entity(
    tableName = "quizzes",
    indices = [
        Index(value = ["chapter_id"]),
        Index(value = ["subject"])
    ]
)
data class QuizEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: String? = null,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "subject")
    val subject: String,
    
    @ColumnInfo(name = "difficulty")
    val difficulty: String = "medium", // easy, medium, hard
    
    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int = 0,
    
    @ColumnInfo(name = "time_limit_minutes")
    val timeLimitMinutes: Int = 10,
    
    @ColumnInfo(name = "is_ai_generated")
    val isAiGenerated: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)

/**
 * Room Entity for Quiz Question.
 */
@Entity(
    tableName = "quiz_questions",
    indices = [Index(value = ["quiz_id"])]
)
data class QuizQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "quiz_id")
    val quizId: String,
    
    @ColumnInfo(name = "question_text")
    val questionText: String,
    
    @ColumnInfo(name = "options")
    val options: List<String>,
    
    @ColumnInfo(name = "correct_answer_index")
    val correctAnswerIndex: Int,
    
    @ColumnInfo(name = "explanation")
    val explanation: String? = null,
    
    @ColumnInfo(name = "topic")
    val topic: String? = null,
    
    @ColumnInfo(name = "difficulty")
    val difficulty: String = "medium",
    
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0
)

/**
 * Room Entity for Quiz Attempt.
 */
@Entity(
    tableName = "quiz_attempts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["quiz_id"]),
        Index(value = ["is_synced"])
    ]
)
data class QuizAttemptEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "quiz_id")
    val quizId: String,
    
    @ColumnInfo(name = "subject")
    val subject: String,
    
    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,
    
    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int,
    
    @ColumnInfo(name = "score_percentage")
    val scorePercentage: Float,
    
    @ColumnInfo(name = "time_taken_seconds")
    val timeTakenSeconds: Int,
    
    @ColumnInfo(name = "weak_topics")
    val weakTopics: List<String>? = null,
    
    @ColumnInfo(name = "answers")
    val answers: List<Int>? = null, // User's selected answer indices
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "attempted_at")
    val attemptedAt: Date = Date()
)
