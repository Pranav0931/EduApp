package com.hdaf.eduapp.domain.model

/**
 * Accessibility modes for different user needs.
 */
enum class AccessibilityModeType {
    NORMAL,
    BLIND,
    DEAF,
    LOW_VISION,
    SLOW_LEARNER
}

/**
 * Preferred content delivery format.
 */
enum class ContentDeliveryMode {
    TEXT,
    AUDIO,
    VIDEO,
    SIGN_LANGUAGE,
    MIXED
}

/**
 * Contrast preference levels.
 */
enum class ContrastLevel {
    NORMAL,
    MEDIUM,
    HIGH,
    MAXIMUM,
    INVERTED
}

/**
 * Speech rate for TTS.
 */
enum class SpeechRate {
    VERY_SLOW,
    SLOW,
    NORMAL,
    FAST,
    VERY_FAST;
    
    fun toFloat(): Float = when (this) {
        VERY_SLOW -> 0.5f
        SLOW -> 0.75f
        NORMAL -> 1.0f
        FAST -> 1.25f
        VERY_FAST -> 1.5f
    }
}

/**
 * Complete accessibility profile for a user.
 */
data class AccessibilityProfile(
    val userId: String,
    val accessibilityMode: AccessibilityModeType = AccessibilityModeType.NORMAL,
    val contentDeliveryMode: ContentDeliveryMode = ContentDeliveryMode.MIXED,
    
    // Screen reader settings
    val screenReaderEnabled: Boolean = false,
    val talkBackOptimized: Boolean = false,
    val speechRate: SpeechRate = SpeechRate.NORMAL,
    val announceFocusChanges: Boolean = true,
    
    // Visual settings
    val fontScale: Float = 1.0f,
    val contrastLevel: ContrastLevel = ContrastLevel.NORMAL,
    val reduceMotion: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val boldTextEnabled: Boolean = false,
    
    // Audio settings
    val subtitlesEnabled: Boolean = false,
    val signLanguageModeEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val audioDescriptionsEnabled: Boolean = false,
    
    // Navigation
    val voiceNavigationEnabled: Boolean = false,
    val gestureNavigationEnabled: Boolean = true,
    val simplifiedNavigationEnabled: Boolean = false,
    
    // Learning preferences
    val preferAudioContent: Boolean = false,
    val preferTextContent: Boolean = false,
    val preferVisualContent: Boolean = false,
    val autoReadContent: Boolean = false,
    val extendedTimeForQuizzes: Boolean = false,
    val quizTimeMultiplier: Float = 1.0f,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun forBlindUser(userId: String) = AccessibilityProfile(
            userId = userId,
            accessibilityMode = AccessibilityModeType.BLIND,
            contentDeliveryMode = ContentDeliveryMode.AUDIO,
            screenReaderEnabled = true,
            talkBackOptimized = true,
            voiceNavigationEnabled = true,
            preferAudioContent = true,
            autoReadContent = true,
            hapticFeedbackEnabled = true,
            audioDescriptionsEnabled = true,
            announceFocusChanges = true
        )
        
        fun forDeafUser(userId: String) = AccessibilityProfile(
            userId = userId,
            accessibilityMode = AccessibilityModeType.DEAF,
            contentDeliveryMode = ContentDeliveryMode.TEXT,
            subtitlesEnabled = true,
            signLanguageModeEnabled = true,
            preferTextContent = true,
            preferVisualContent = true,
            hapticFeedbackEnabled = true
        )
        
        fun forLowVisionUser(userId: String) = AccessibilityProfile(
            userId = userId,
            accessibilityMode = AccessibilityModeType.LOW_VISION,
            contentDeliveryMode = ContentDeliveryMode.MIXED,
            fontScale = 1.5f,
            contrastLevel = ContrastLevel.HIGH,
            largeTextEnabled = true,
            boldTextEnabled = true,
            screenReaderEnabled = true
        )
        
        fun forSlowLearner(userId: String) = AccessibilityProfile(
            userId = userId,
            accessibilityMode = AccessibilityModeType.SLOW_LEARNER,
            contentDeliveryMode = ContentDeliveryMode.MIXED,
            simplifiedNavigationEnabled = true,
            extendedTimeForQuizzes = true,
            quizTimeMultiplier = 2.0f,
            speechRate = SpeechRate.SLOW
        )
    }
}
