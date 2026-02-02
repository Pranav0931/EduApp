package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisting in-progress quiz attempts.
 * Enables resuming quizzes after app restart or interruption.
 */
@Entity(tableName = "quiz_progress")
data class QuizProgressEntity(
    @PrimaryKey
    val quizId: String,
    
    /** Chapter this quiz belongs to */
    val chapterId: String,
    
    /** Current question index (0-based) */
    val currentQuestionIndex: Int = 0,
    
    /** JSON string of selected answers map (questionIndex -> optionIndex) */
    val selectedAnswersJson: String = "{}",
    
    /** Time remaining in seconds */
    val timeRemainingSeconds: Int = 0,
    
    /** Quiz phase (INSTRUCTIONS, IN_PROGRESS, REVIEW, COMPLETED) */
    val quizPhase: String = "IN_PROGRESS",
    
    /** When this progress was last updated */
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    
    /** When the quiz was started */
    val startedAt: Long = System.currentTimeMillis()
)
