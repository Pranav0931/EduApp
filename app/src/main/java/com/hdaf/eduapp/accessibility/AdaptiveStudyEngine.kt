package com.hdaf.eduapp.accessibility

import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.StudyAnalyticsDao
import com.hdaf.eduapp.data.local.dao.StudyRecommendationDao
import com.hdaf.eduapp.data.local.entity.StudyAnalyticsEntity
import com.hdaf.eduapp.data.local.entity.StudyRecommendationEntity
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Adaptive Study Engine for personalized learning.
 * Adjusts content difficulty, pacing, and recommendations based on user performance.
 * Specially optimized for slow learners with gentle progression.
 */
@Singleton
class AdaptiveStudyEngine @Inject constructor(
    private val studyAnalyticsDao: StudyAnalyticsDao,
    private val studyRecommendationDao: StudyRecommendationDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val _currentDifficultyLevel = MutableStateFlow(DifficultyLevel.NORMAL)
    val currentDifficultyLevel: StateFlow<DifficultyLevel> = _currentDifficultyLevel.asStateFlow()
    
    private val _learningPace = MutableStateFlow(LearningPace.NORMAL)
    val learningPace: StateFlow<LearningPace> = _learningPace.asStateFlow()
    
    private val _studySession = MutableStateFlow<StudySession?>(null)
    val studySession: StateFlow<StudySession?> = _studySession.asStateFlow()
    
    private var userProfile: AccessibilityProfile? = null
    
    // Performance tracking
    private var correctAnswers = 0
    private var totalAnswers = 0
    private var consecutiveCorrect = 0
    private var consecutiveWrong = 0
    private var sessionStartTime = 0L
    
    /**
     * Initialize engine with user profile.
     */
    fun initialize(profile: AccessibilityProfile) {
        userProfile = profile
        
        // Set initial pace based on accessibility mode
        _learningPace.value = when (profile.accessibilityMode) {
            AccessibilityModeType.SLOW_LEARNER -> LearningPace.SLOW
            AccessibilityModeType.BLIND -> LearningPace.SLOW // More time needed for audio
            else -> LearningPace.NORMAL
        }
        
        // Set initial difficulty
        _currentDifficultyLevel.value = when (profile.accessibilityMode) {
            AccessibilityModeType.SLOW_LEARNER -> DifficultyLevel.EASY
            else -> DifficultyLevel.NORMAL
        }
    }
    
    /**
     * Start a new study session.
     */
    suspend fun startSession(
        userId: String,
        subjectId: String,
        topicId: String
    ): StudySession = withContext(ioDispatcher) {
        correctAnswers = 0
        totalAnswers = 0
        consecutiveCorrect = 0
        consecutiveWrong = 0
        sessionStartTime = System.currentTimeMillis()
        
        val session = StudySession(
            userId = userId,
            subjectId = subjectId,
            topicId = topicId,
            startTime = sessionStartTime,
            difficultyLevel = _currentDifficultyLevel.value,
            learningPace = _learningPace.value
        )
        
        _studySession.value = session
        session
    }
    
    /**
     * Record an answer and adapt difficulty.
     */
    suspend fun recordAnswer(
        questionId: String,
        isCorrect: Boolean,
        timeSpentMs: Long,
        mistakeType: MistakeType? = null
    ): AdaptationResult = withContext(ioDispatcher) {
        totalAnswers++
        
        if (isCorrect) {
            correctAnswers++
            consecutiveCorrect++
            consecutiveWrong = 0
        } else {
            consecutiveWrong++
            consecutiveCorrect = 0
            
            // Track mistake for analytics
            if (mistakeType != null) {
                trackMistake(questionId, mistakeType)
            }
        }
        
        // Calculate adaptation
        val adaptation = calculateAdaptation(isCorrect, timeSpentMs)
        
        // Apply adaptation
        applyAdaptation(adaptation)
        
        // Save analytics
        saveAnalytics()
        
        adaptation
    }
    
    /**
     * Calculate how to adapt based on performance.
     */
    private fun calculateAdaptation(isCorrect: Boolean, timeSpentMs: Long): AdaptationResult {
        val currentAccuracy = if (totalAnswers > 0) {
            correctAnswers.toFloat() / totalAnswers
        } else 0f
        
        val isSlowLearner = userProfile?.accessibilityMode == AccessibilityModeType.SLOW_LEARNER
        
        // Slow learners get gentler adaptation
        val consecutiveThresholdUp = if (isSlowLearner) 4 else 3
        val consecutiveThresholdDown = if (isSlowLearner) 3 else 2
        
        var newDifficulty = _currentDifficultyLevel.value
        var newPace = _learningPace.value
        var feedback = ""
        var showEncouragement = false
        
        // Difficulty adaptation
        when {
            consecutiveCorrect >= consecutiveThresholdUp && currentAccuracy >= 0.8f -> {
                newDifficulty = _currentDifficultyLevel.value.increase()
                feedback = getPositiveFeedback()
                showEncouragement = true
            }
            consecutiveWrong >= consecutiveThresholdDown -> {
                newDifficulty = _currentDifficultyLevel.value.decrease()
                feedback = getEncouragingFeedback()
                showEncouragement = true
            }
        }
        
        // Pace adaptation based on time
        val expectedTimeMs = getExpectedTimeForDifficulty(_currentDifficultyLevel.value)
        when {
            timeSpentMs > expectedTimeMs * 2 -> {
                newPace = _learningPace.value.slower()
                if (feedback.isEmpty()) feedback = "Take your time, no rush!"
            }
            timeSpentMs < expectedTimeMs * 0.5 && currentAccuracy >= 0.9f -> {
                newPace = _learningPace.value.faster()
            }
        }
        
        // For slow learners, never go too fast
        if (isSlowLearner && newPace == LearningPace.FAST) {
            newPace = LearningPace.NORMAL
        }
        
        return AdaptationResult(
            previousDifficulty = _currentDifficultyLevel.value,
            newDifficulty = newDifficulty,
            previousPace = _learningPace.value,
            newPace = newPace,
            currentAccuracy = currentAccuracy,
            feedback = feedback,
            showEncouragement = showEncouragement,
            suggestBreak = shouldSuggestBreak(),
            suggestReview = shouldSuggestReview()
        )
    }
    
    /**
     * Apply adaptation to current session.
     */
    private fun applyAdaptation(result: AdaptationResult) {
        _currentDifficultyLevel.value = result.newDifficulty
        _learningPace.value = result.newPace
        
        _studySession.value = _studySession.value?.copy(
            difficultyLevel = result.newDifficulty,
            learningPace = result.newPace,
            correctAnswers = correctAnswers,
            totalAnswers = totalAnswers
        )
    }
    
    /**
     * Track mistake for future review.
     */
    private suspend fun trackMistake(questionId: String, mistakeType: MistakeType) {
        try {
            val userId = userProfile?.userId ?: return
            val existing = studyAnalyticsDao.getRecentAnalytics(userId, 1).firstOrNull()
            
            // Analytics tracking is handled through the repository
            Timber.d("Tracked mistake: $mistakeType for question: $questionId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track mistake")
        }
    }
    
    /**
     * Save current analytics to database.
     */
    private suspend fun saveAnalytics() {
        try {
            val session = _studySession.value ?: return
            val userId = session.userId
            
            val analytics = StudyAnalyticsEntity(
                userId = userId,
                subjectId = session.subjectId,
                chapterId = session.topicId,
                eventType = "STUDY_SESSION",
                durationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt(),
                score = if (totalAnswers > 0) (correctAnswers.toFloat() / totalAnswers * 100) else null,
                mistakesCount = totalAnswers - correctAnswers,
                accessibilityMode = userProfile?.accessibilityMode?.name ?: "NORMAL",
                contentType = "MIXED",
                interactionCount = totalAnswers,
                timestamp = System.currentTimeMillis()
            )
            
            studyAnalyticsDao.insertAnalytics(analytics)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save analytics")
        }
    }
    
    /**
     * Get expected time for current difficulty.
     */
    private fun getExpectedTimeForDifficulty(difficulty: DifficultyLevel): Long {
        return when (difficulty) {
            DifficultyLevel.VERY_EASY -> 45000L // 45 seconds
            DifficultyLevel.EASY -> 60000L // 1 minute
            DifficultyLevel.NORMAL -> 90000L // 1.5 minutes
            DifficultyLevel.HARD -> 120000L // 2 minutes
            DifficultyLevel.VERY_HARD -> 180000L // 3 minutes
        }
    }
    
    /**
     * Check if we should suggest a break.
     */
    private fun shouldSuggestBreak(): Boolean {
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        val isSlowLearner = userProfile?.accessibilityMode == AccessibilityModeType.SLOW_LEARNER
        
        // Slow learners get break suggestions earlier
        val breakInterval = if (isSlowLearner) 15 * 60 * 1000L else 25 * 60 * 1000L
        
        return sessionDuration >= breakInterval
    }
    
    /**
     * Check if we should suggest review.
     */
    private fun shouldSuggestReview(): Boolean {
        val currentAccuracy = if (totalAnswers > 0) {
            correctAnswers.toFloat() / totalAnswers
        } else 1f
        
        return currentAccuracy < 0.6f && totalAnswers >= 5
    }
    
    /**
     * Get encouraging feedback for slow learners.
     */
    private fun getEncouragingFeedback(): String {
        val messages = listOf(
            "You're doing great! Let's try an easier one.",
            "Keep going! Practice makes perfect.",
            "Don't worry, everyone learns at their own pace.",
            "You're making progress! Let's try another.",
            "Almost there! Let's practice a bit more.",
            "Learning takes time, and you're doing well!",
            "Every mistake is a chance to learn something new!"
        )
        return messages.random()
    }
    
    /**
     * Get positive feedback for correct answers.
     */
    private fun getPositiveFeedback(): String {
        val messages = listOf(
            "Excellent work! You're ready for more!",
            "Amazing! Let's try something a bit harder.",
            "You're on fire! Great job!",
            "Brilliant! You're mastering this!",
            "Superstar! Keep it up!",
            "Wonderful! You're getting better every day!",
            "Fantastic progress! You're doing amazingly well!"
        )
        return messages.random()
    }
    
    /**
     * Generate personalized recommendations.
     */
    suspend fun generateRecommendations(userId: String): List<StudyRecommendation> = withContext(ioDispatcher) {
        val recommendations = mutableListOf<StudyRecommendation>()
        
        try {
            val recentAnalytics = studyAnalyticsDao.getRecentAnalytics(userId, 100)
            
            // Analyze weak areas
            val subjectAccuracy = recentAnalytics.groupBy { it.subjectId }
                .mapValues { (_, analyticsList) ->
                    val totalInteractions = analyticsList.sumOf { it.interactionCount }
                    val averageScore = analyticsList.mapNotNull { it.score }.average()
                    if (averageScore.isNaN()) 1f else (averageScore / 100).toFloat()
                }
            
            // Recommend review for weak subjects
            subjectAccuracy.filter { it.value < 0.7f }.forEach { (subjectId, accuracy) ->
                recommendations.add(
                    StudyRecommendation(
                        type = RecommendationType.REVIEW,
                        subjectId = subjectId,
                        reason = "Your accuracy is ${(accuracy * 100).toInt()}%. Let's review!",
                        priority = Priority.HIGH
                    )
                )
            }
            
            // Check for subjects not studied recently
            val recentSubjects = recentAnalytics
                .filter { it.timestamp > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 }
                .map { it.subjectId }
                .toSet()
            
            // Add recommendation for break if studying too long
            if (shouldSuggestBreak()) {
                recommendations.add(
                    StudyRecommendation(
                        type = RecommendationType.BREAK,
                        reason = "You've been studying hard! Take a short break.",
                        priority = Priority.MEDIUM
                    )
                )
            }
            
            // Save recommendations
            recommendations.forEachIndexed { index, rec ->
                studyRecommendationDao.insertRecommendation(
                    StudyRecommendationEntity(
                        id = "${userId}_${System.currentTimeMillis()}_$index",
                        userId = userId,
                        recommendationType = rec.type.name,
                        subjectId = rec.subjectId,
                        chapterId = rec.topicId,
                        title = rec.type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        description = rec.reason,
                        reason = rec.reason,
                        priority = rec.priority.ordinal,
                        createdAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                        isCompleted = false
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate recommendations")
        }
        
        recommendations
    }
    
    /**
     * End current study session.
     */
    suspend fun endSession(): SessionSummary? = withContext(ioDispatcher) {
        val session = _studySession.value ?: return@withContext null
        
        val duration = System.currentTimeMillis() - sessionStartTime
        val accuracy = if (totalAnswers > 0) correctAnswers.toFloat() / totalAnswers else 0f
        
        // Save final analytics
        saveAnalytics()
        
        val summary = SessionSummary(
            durationMinutes = (duration / 60000).toInt(),
            questionsAnswered = totalAnswers,
            correctAnswers = correctAnswers,
            accuracy = accuracy,
            finalDifficulty = _currentDifficultyLevel.value,
            difficultyProgression = session.difficultyLevel to _currentDifficultyLevel.value,
            encouragement = getSessionEndMessage(accuracy)
        )
        
        _studySession.value = null
        summary
    }
    
    /**
     * Get session end message based on performance.
     */
    private fun getSessionEndMessage(accuracy: Float): String {
        return when {
            accuracy >= 0.9f -> "Outstanding session! You're a superstar! ⭐"
            accuracy >= 0.7f -> "Great work today! Keep it up! 🌟"
            accuracy >= 0.5f -> "Good effort! Practice makes perfect! 💪"
            else -> "Every learning session counts! You're making progress! 🎯"
        }
    }
}

/**
 * Difficulty levels for adaptive learning.
 */
enum class DifficultyLevel {
    VERY_EASY,
    EASY,
    NORMAL,
    HARD,
    VERY_HARD;
    
    fun increase(): DifficultyLevel = when (this) {
        VERY_EASY -> EASY
        EASY -> NORMAL
        NORMAL -> HARD
        HARD -> VERY_HARD
        VERY_HARD -> VERY_HARD
    }
    
    fun decrease(): DifficultyLevel = when (this) {
        VERY_EASY -> VERY_EASY
        EASY -> VERY_EASY
        NORMAL -> EASY
        HARD -> NORMAL
        VERY_HARD -> HARD
    }
}

/**
 * Learning pace settings.
 */
enum class LearningPace {
    VERY_SLOW,
    SLOW,
    NORMAL,
    FAST;
    
    fun slower(): LearningPace = when (this) {
        VERY_SLOW -> VERY_SLOW
        SLOW -> VERY_SLOW
        NORMAL -> SLOW
        FAST -> NORMAL
    }
    
    fun faster(): LearningPace = when (this) {
        VERY_SLOW -> SLOW
        SLOW -> NORMAL
        NORMAL -> FAST
        FAST -> FAST
    }
}

/**
 * Types of mistakes for tracking.
 */
enum class MistakeType {
    CONCEPTUAL,      // Doesn't understand the concept
    CALCULATION,     // Math error
    READING,         // Misread the question
    CARELESS,        // Knew but made mistake
    TIME_PRESSURE,   // Ran out of time
    LANGUAGE,        // Language barrier
    UNKNOWN
}

/**
 * Current study session data.
 */
data class StudySession(
    val userId: String,
    val subjectId: String,
    val topicId: String,
    val startTime: Long,
    val difficultyLevel: DifficultyLevel,
    val learningPace: LearningPace,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0
)

/**
 * Result of difficulty adaptation.
 */
data class AdaptationResult(
    val previousDifficulty: DifficultyLevel,
    val newDifficulty: DifficultyLevel,
    val previousPace: LearningPace,
    val newPace: LearningPace,
    val currentAccuracy: Float,
    val feedback: String,
    val showEncouragement: Boolean,
    val suggestBreak: Boolean,
    val suggestReview: Boolean
) {
    val difficultyChanged: Boolean get() = previousDifficulty != newDifficulty
    val paceChanged: Boolean get() = previousPace != newPace
}

/**
 * Study session summary.
 */
data class SessionSummary(
    val durationMinutes: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val finalDifficulty: DifficultyLevel,
    val difficultyProgression: Pair<DifficultyLevel, DifficultyLevel>,
    val encouragement: String
)

/**
 * Study recommendation.
 */
data class StudyRecommendation(
    val type: RecommendationType,
    val subjectId: String? = null,
    val topicId: String? = null,
    val reason: String,
    val priority: Priority
)

/**
 * Types of recommendations.
 */
enum class RecommendationType {
    REVIEW,
    NEW_TOPIC,
    PRACTICE_QUIZ,
    BREAK,
    ACHIEVEMENT
}

/**
 * Priority levels.
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
