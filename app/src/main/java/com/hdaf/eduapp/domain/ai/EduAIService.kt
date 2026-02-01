package com.hdaf.eduapp.domain.ai

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.Subject

/**
 * AI Service interface for educational AI features.
 * Abstracts the AI provider (Gemini) from the domain layer.
 */
interface EduAIService {

    /**
     * Generate a quiz from chapter content.
     */
    suspend fun generateQuiz(
        chapterContent: String,
        subject: Subject,
        numberOfQuestions: Int = 10,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    ): Resource<Quiz>

    /**
     * Generate an adaptive quiz based on weak topics.
     */
    suspend fun generateAdaptiveQuiz(
        subject: Subject,
        weakTopics: List<String>,
        numberOfQuestions: Int = 10
    ): Resource<Quiz>

    /**
     * Explain a concept in simple terms.
     */
    suspend fun explainConcept(
        concept: String,
        subject: Subject,
        classLevel: Int
    ): Resource<ExplanationResult>

    /**
     * Answer a student's question.
     */
    suspend fun answerQuestion(
        question: String,
        context: String? = null,
        subject: Subject? = null
    ): Resource<AnswerResult>

    /**
     * Summarize chapter content.
     */
    suspend fun summarizeChapter(
        content: String,
        maxLength: Int = 500
    ): Resource<String>

    /**
     * Generate practice problems.
     */
    suspend fun generatePracticeProblems(
        topic: String,
        subject: Subject,
        difficulty: QuizDifficulty,
        count: Int = 5
    ): Resource<List<PracticeProblem>>

    /**
     * Provide personalized learning recommendations.
     */
    suspend fun getRecommendations(
        userId: String,
        weakTopics: Map<Subject, List<String>>,
        completedChapters: List<String>
    ): Resource<List<LearningRecommendation>>

    /**
     * Translate content to different language.
     */
    suspend fun translateContent(
        content: String,
        targetLanguage: String
    ): Resource<String>

    /**
     * Parse natural language intent for voice commands.
     */
    suspend fun parseIntent(
        userInput: String
    ): Resource<IntentParseResult>
}

// ==================== Supporting Data Classes ====================

data class ExplanationResult(
    val explanation: String,
    val examples: List<String> = emptyList(),
    val relatedTopics: List<String> = emptyList(),
    val funFact: String? = null
)

data class AnswerResult(
    val answer: String,
    val confidence: Float,
    val sources: List<String> = emptyList(),
    val followUpQuestions: List<String> = emptyList()
)

data class PracticeProblem(
    val question: String,
    val hints: List<String> = emptyList(),
    val solution: String,
    val explanation: String
)

data class LearningRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val resourceId: String? = null,
    val priority: Int = 0
)

enum class RecommendationType {
    CHAPTER_TO_READ,
    QUIZ_TO_TAKE,
    TOPIC_TO_REVIEW,
    PRACTICE_PROBLEMS,
    VIDEO_TO_WATCH
}

data class IntentParseResult(
    val intent: UserIntent,
    val entities: Map<String, String> = emptyMap(),
    val confidence: Float
)

enum class UserIntent {
    READ_CHAPTER,
    TAKE_QUIZ,
    ASK_QUESTION,
    EXPLAIN_CONCEPT,
    GO_HOME,
    GO_BACK,
    REPEAT,
    PAUSE,
    RESUME,
    NEXT,
    PREVIOUS,
    SELECT_ITEM,
    OPEN_SETTINGS,
    OPEN_PROFILE,
    UNKNOWN
}
