package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.local.entity.AccessibilityProfileEntity
import com.hdaf.eduapp.data.local.entity.AIChatMessageEntity
import com.hdaf.eduapp.data.local.entity.StudyAnalyticsEntity
import com.hdaf.eduapp.data.local.entity.StudyRecommendationEntity
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.ContentDeliveryMode
import com.hdaf.eduapp.domain.model.ContrastLevel
import com.hdaf.eduapp.domain.model.SpeechRate

// Extension functions for mapping AccessibilityProfile

fun AccessibilityProfileEntity.toDomain(): AccessibilityProfile = AccessibilityProfile(
    userId = userId,
    accessibilityMode = AccessibilityModeType.valueOf(accessibilityMode),
    contentDeliveryMode = ContentDeliveryMode.valueOf(contentDeliveryMode),
    screenReaderEnabled = screenReaderEnabled,
    talkBackOptimized = talkBackOptimized,
    speechRate = SpeechRate.valueOf(speechRate),
    announceFocusChanges = announceFocusChanges,
    fontScale = fontScale,
    contrastLevel = ContrastLevel.valueOf(contrastLevel),
    reduceMotion = reduceMotion,
    largeTextEnabled = largeTextEnabled,
    boldTextEnabled = boldTextEnabled,
    subtitlesEnabled = subtitlesEnabled,
    signLanguageModeEnabled = signLanguageModeEnabled,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    audioDescriptionsEnabled = audioDescriptionsEnabled,
    voiceNavigationEnabled = voiceNavigationEnabled,
    gestureNavigationEnabled = gestureNavigationEnabled,
    simplifiedNavigationEnabled = simplifiedNavigationEnabled,
    preferAudioContent = preferAudioContent,
    preferTextContent = preferTextContent,
    preferVisualContent = preferVisualContent,
    autoReadContent = autoReadContent,
    extendedTimeForQuizzes = extendedTimeForQuizzes,
    quizTimeMultiplier = quizTimeMultiplier,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AccessibilityProfile.toEntity(): AccessibilityProfileEntity = AccessibilityProfileEntity(
    userId = userId,
    accessibilityMode = accessibilityMode.name,
    contentDeliveryMode = contentDeliveryMode.name,
    screenReaderEnabled = screenReaderEnabled,
    talkBackOptimized = talkBackOptimized,
    speechRate = speechRate.name,
    announceFocusChanges = announceFocusChanges,
    fontScale = fontScale,
    contrastLevel = contrastLevel.name,
    reduceMotion = reduceMotion,
    largeTextEnabled = largeTextEnabled,
    boldTextEnabled = boldTextEnabled,
    subtitlesEnabled = subtitlesEnabled,
    signLanguageModeEnabled = signLanguageModeEnabled,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    audioDescriptionsEnabled = audioDescriptionsEnabled,
    voiceNavigationEnabled = voiceNavigationEnabled,
    gestureNavigationEnabled = gestureNavigationEnabled,
    simplifiedNavigationEnabled = simplifiedNavigationEnabled,
    preferAudioContent = preferAudioContent,
    preferTextContent = preferTextContent,
    preferVisualContent = preferVisualContent,
    autoReadContent = autoReadContent,
    extendedTimeForQuizzes = extendedTimeForQuizzes,
    quizTimeMultiplier = quizTimeMultiplier,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// AI Chat Message mapping
data class AIChatMessage(
    val id: String,
    val userId: String,
    val sessionId: String,
    val message: String,
    val isUserMessage: Boolean,
    val responseMode: ResponseMode = ResponseMode.TEXT,
    val contextSubject: String? = null,
    val contextChapter: String? = null,
    val audioUrl: String? = null,
    val isCached: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ResponseMode {
    TEXT, AUDIO, VISUAL
}

fun AIChatMessageEntity.toDomain(): AIChatMessage = AIChatMessage(
    id = id,
    userId = userId,
    sessionId = sessionId,
    message = message,
    isUserMessage = isUserMessage,
    responseMode = ResponseMode.valueOf(responseMode),
    contextSubject = contextSubject,
    contextChapter = contextChapter,
    audioUrl = audioUrl,
    isCached = isCached,
    timestamp = timestamp
)

fun AIChatMessage.toEntity(): AIChatMessageEntity = AIChatMessageEntity(
    id = id,
    userId = userId,
    sessionId = sessionId,
    message = message,
    isUserMessage = isUserMessage,
    responseMode = responseMode.name,
    contextSubject = contextSubject,
    contextChapter = contextChapter,
    audioUrl = audioUrl,
    isCached = isCached,
    timestamp = timestamp
)

// Study Analytics mapping
data class StudyAnalytics(
    val id: Long = 0,
    val userId: String,
    val subjectId: String,
    val chapterId: String? = null,
    val eventType: StudyEventType,
    val durationSeconds: Int = 0,
    val score: Float? = null,
    val mistakesCount: Int = 0,
    val accessibilityMode: AccessibilityModeType = AccessibilityModeType.NORMAL,
    val contentType: ContentDeliveryMode = ContentDeliveryMode.MIXED,
    val interactionCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StudyEventType {
    LESSON_START, LESSON_COMPLETE, QUIZ_ATTEMPT, PAUSE, RESUME
}

fun StudyAnalyticsEntity.toDomain(): StudyAnalytics = StudyAnalytics(
    id = id,
    userId = userId,
    subjectId = subjectId,
    chapterId = chapterId,
    eventType = StudyEventType.valueOf(eventType),
    durationSeconds = durationSeconds,
    score = score,
    mistakesCount = mistakesCount,
    accessibilityMode = AccessibilityModeType.valueOf(accessibilityMode),
    contentType = ContentDeliveryMode.valueOf(contentType),
    interactionCount = interactionCount,
    timestamp = timestamp
)

fun StudyAnalytics.toEntity(): StudyAnalyticsEntity = StudyAnalyticsEntity(
    id = id,
    userId = userId,
    subjectId = subjectId,
    chapterId = chapterId,
    eventType = eventType.name,
    durationSeconds = durationSeconds,
    score = score,
    mistakesCount = mistakesCount,
    accessibilityMode = accessibilityMode.name,
    contentType = contentType.name,
    interactionCount = interactionCount,
    timestamp = timestamp
)

// Study Recommendation mapping
data class StudyRecommendation(
    val id: String,
    val userId: String,
    val recommendationType: RecommendationType,
    val subjectId: String? = null,
    val chapterId: String? = null,
    val title: String,
    val description: String,
    val priority: Int = 0,
    val reason: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000
)

enum class RecommendationType {
    CONTENT, QUIZ, REVIEW, BREAK
}

fun StudyRecommendationEntity.toDomain(): StudyRecommendation = StudyRecommendation(
    id = id,
    userId = userId,
    recommendationType = RecommendationType.valueOf(recommendationType),
    subjectId = subjectId,
    chapterId = chapterId,
    title = title,
    description = description,
    priority = priority,
    reason = reason,
    isCompleted = isCompleted,
    createdAt = createdAt,
    expiresAt = expiresAt
)

fun StudyRecommendation.toEntity(): StudyRecommendationEntity = StudyRecommendationEntity(
    id = id,
    userId = userId,
    recommendationType = recommendationType.name,
    subjectId = subjectId,
    chapterId = chapterId,
    title = title,
    description = description,
    priority = priority,
    reason = reason,
    isCompleted = isCompleted,
    createdAt = createdAt,
    expiresAt = expiresAt
)
