package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.data.mapper.AIChatMessage
import com.hdaf.eduapp.data.mapper.StudyAnalytics
import com.hdaf.eduapp.data.mapper.StudyRecommendation
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.ContrastLevel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing accessibility profiles and settings.
 */
interface AccessibilityRepository {
    
    /**
     * Get the accessibility profile for a user.
     */
    suspend fun getProfile(userId: String): Resource<AccessibilityProfile>
    
    /**
     * Observe profile changes in real-time.
     */
    fun observeProfile(userId: String): Flow<AccessibilityProfile?>
    
    /**
     * Create or update an accessibility profile.
     */
    suspend fun saveProfile(profile: AccessibilityProfile): Resource<Unit>
    
    /**
     * Update just the accessibility mode.
     */
    suspend fun updateAccessibilityMode(userId: String, mode: AccessibilityModeType): Resource<Unit>
    
    /**
     * Update font scale.
     */
    suspend fun updateFontScale(userId: String, scale: Float): Resource<Unit>
    
    /**
     * Update contrast level.
     */
    suspend fun updateContrastLevel(userId: String, level: ContrastLevel): Resource<Unit>
    
    /**
     * Delete user's accessibility profile.
     */
    suspend fun deleteProfile(userId: String): Resource<Unit>
    
    /**
     * Get recommended profile for a specific mode.
     */
    fun getRecommendedProfile(userId: String, mode: AccessibilityModeType): AccessibilityProfile
}

/**
 * Repository for AI chat functionality.
 */
interface AIChatRepository {
    
    /**
     * Send a message and get AI response.
     */
    suspend fun sendMessage(
        userId: String,
        sessionId: String,
        message: String,
        contextSubject: String? = null,
        contextChapter: String? = null
    ): Resource<AIChatMessage>
    
    /**
     * Get recent chat messages.
     */
    suspend fun getRecentMessages(userId: String, limit: Int = 50): Resource<List<AIChatMessage>>
    
    /**
     * Observe messages for a session.
     */
    fun observeSessionMessages(sessionId: String): Flow<List<AIChatMessage>>
    
    /**
     * Search cached responses.
     */
    suspend fun searchCachedResponses(query: String): List<AIChatMessage>
    
    /**
     * Clear a chat session.
     */
    suspend fun clearSession(userId: String, sessionId: String): Resource<Unit>
    
    /**
     * Generate spoken summary for content.
     */
    suspend fun generateSpokenSummary(contentId: String, text: String): Resource<String>
}

/**
 * Repository for study analytics and recommendations.
 */
interface StudyAnalyticsRepository {
    
    /**
     * Log a study event.
     */
    suspend fun logEvent(analytics: StudyAnalytics): Resource<Long>
    
    /**
     * Get analytics for a user.
     */
    suspend fun getAnalytics(userId: String, limit: Int = 100): Resource<List<StudyAnalytics>>
    
    /**
     * Get analytics by subject.
     */
    suspend fun getAnalyticsBySubject(userId: String, subjectId: String): Resource<List<StudyAnalytics>>
    
    /**
     * Get average score for a subject.
     */
    suspend fun getAverageScore(userId: String, subjectId: String): Float?
    
    /**
     * Get total study time since a timestamp.
     */
    suspend fun getTotalStudyTime(userId: String, sinceTimestamp: Long): Int?
    
    /**
     * Get user's preferred content type based on usage.
     */
    suspend fun getPreferredContentType(userId: String): String?
    
    /**
     * Generate study recommendations.
     */
    suspend fun generateRecommendations(userId: String): Resource<List<StudyRecommendation>>
    
    /**
     * Get active recommendations.
     */
    suspend fun getActiveRecommendations(userId: String): Resource<List<StudyRecommendation>>
    
    /**
     * Observe recommendations.
     */
    fun observeRecommendations(userId: String): Flow<List<StudyRecommendation>>
    
    /**
     * Mark a recommendation as completed.
     */
    suspend fun markRecommendationCompleted(recommendationId: String): Resource<Unit>
}

/**
 * Repository for OCR operations.
 */
interface OCRRepository {
    
    /**
     * Process an image and extract text.
     */
    suspend fun processImage(imageBytes: ByteArray): Resource<String>
    
    /**
     * Get cached OCR result.
     */
    suspend fun getCachedResult(imageHash: String): String?
    
    /**
     * Clear old cache entries.
     */
    suspend fun clearOldCache(beforeTimestamp: Long): Resource<Unit>
}

/**
 * Repository for voice commands.
 */
interface VoiceCommandRepository {
    
    /**
     * Log a voice command.
     */
    suspend fun logCommand(
        userId: String,
        commandText: String,
        recognizedAction: String,
        wasSuccessful: Boolean
    ): Resource<Unit>
    
    /**
     * Get most used commands for suggestions.
     */
    suspend fun getMostUsedCommands(userId: String): List<String>
    
    /**
     * Get recent command history.
     */
    suspend fun getRecentCommands(userId: String, limit: Int = 20): Resource<List<String>>
}
