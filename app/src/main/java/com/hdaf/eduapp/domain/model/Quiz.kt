package com.hdaf.eduapp.domain.model

/**
 * Domain model for Quiz.
 */
data class Quiz(
    val id: String,
    val chapterId: String? = null,
    val title: String,
    val subject: Subject,
    val difficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
    val totalQuestions: Int,
    val timeLimitMinutes: Int = 10,
    val isAiGenerated: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val estimatedDurationSeconds: Int
        get() = timeLimitMinutes * 60
}

data class QuizQuestion(
    val id: String,
    val quizId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    val topic: String? = null,
    val difficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
    val orderIndex: Int = 0
) {
    fun isCorrect(selectedIndex: Int): Boolean = selectedIndex == correctAnswerIndex
    
    val correctAnswer: String
        get() = options.getOrNull(correctAnswerIndex) ?: ""
}

enum class QuizDifficulty(val displayName: String, val xpMultiplier: Float) {
    EASY("आसान", 1.0f),
    MEDIUM("मध्यम", 1.5f),
    HARD("कठिन", 2.0f);
    
    companion object {
        fun fromString(value: String): QuizDifficulty {
            return when (value.lowercase()) {
                "easy", "आसान" -> EASY
                "medium", "मध्यम" -> MEDIUM
                "hard", "कठिन" -> HARD
                else -> MEDIUM
            }
        }
    }
}

/**
 * Represents a completed quiz attempt.
 */
data class QuizAttempt(
    val id: String,
    val userId: String,
    val quizId: String,
    val subject: Subject,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Float,
    val timeTakenSeconds: Int,
    val weakTopics: List<String> = emptyList(),
    val answers: List<Int> = emptyList(),
    val isCompleted: Boolean = true,
    val attemptedAt: Long = System.currentTimeMillis()
) {
    val isPassing: Boolean
        get() = scorePercentage >= PASSING_THRESHOLD
    
    val grade: QuizGrade
        get() = when {
            scorePercentage >= 90 -> QuizGrade.EXCELLENT
            scorePercentage >= 75 -> QuizGrade.GOOD
            scorePercentage >= 60 -> QuizGrade.AVERAGE
            scorePercentage >= 40 -> QuizGrade.NEEDS_IMPROVEMENT
            else -> QuizGrade.POOR
        }
    
    val earnedXp: Int
        get() = (correctAnswers * BASE_XP_PER_QUESTION).toInt()
    
    companion object {
        const val PASSING_THRESHOLD = 40f
        const val BASE_XP_PER_QUESTION = 10
    }
}

enum class QuizGrade(val displayName: String, val emoji: String) {
    EXCELLENT("उत्कृष्ट", "🌟"),
    GOOD("अच्छा", "👍"),
    AVERAGE("औसत", "📚"),
    NEEDS_IMPROVEMENT("सुधार की आवश्यकता", "💪"),
    POOR("अभ्यास करें", "📖")
}

/**
 * Quiz analytics for a specific subject.
 */
data class QuizAnalytics(
    val subject: Subject,
    val totalAttempts: Int,
    val averageScore: Float,
    val bestScore: Float,
    val totalTimeSpentMinutes: Int,
    val weakTopics: List<String>,
    val strongTopics: List<String>,
    val improvementTrend: ImprovementTrend
)

enum class ImprovementTrend {
    IMPROVING,
    STABLE,
    DECLINING
}
