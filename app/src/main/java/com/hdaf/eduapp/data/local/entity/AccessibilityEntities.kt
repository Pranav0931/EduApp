package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing AI assistant chat messages.
 */
@Entity(tableName = "ai_chat_messages")
data class AIChatMessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    
    @ColumnInfo(name = "message")
    val message: String,
    
    @ColumnInfo(name = "is_user_message")
    val isUserMessage: Boolean,
    
    @ColumnInfo(name = "response_mode")
    val responseMode: String = "TEXT", // TEXT, AUDIO, VISUAL
    
    @ColumnInfo(name = "context_subject")
    val contextSubject: String? = null,
    
    @ColumnInfo(name = "context_chapter")
    val contextChapter: String? = null,
    
    @ColumnInfo(name = "audio_url")
    val audioUrl: String? = null,
    
    @ColumnInfo(name = "is_cached")
    val isCached: Boolean = false,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Entity for storing study analytics.
 */
@Entity(tableName = "study_analytics")
data class StudyAnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "subject_id")
    val subjectId: String,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: String? = null,
    
    @ColumnInfo(name = "event_type")
    val eventType: String, // LESSON_START, LESSON_COMPLETE, QUIZ_ATTEMPT, PAUSE, RESUME
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int = 0,
    
    @ColumnInfo(name = "score")
    val score: Float? = null,
    
    @ColumnInfo(name = "mistakes_count")
    val mistakesCount: Int = 0,
    
    @ColumnInfo(name = "accessibility_mode")
    val accessibilityMode: String = "NORMAL",
    
    @ColumnInfo(name = "content_type")
    val contentType: String = "MIXED", // AUDIO, TEXT, VIDEO
    
    @ColumnInfo(name = "interaction_count")
    val interactionCount: Int = 0,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Entity for storing study recommendations.
 */
@Entity(tableName = "study_recommendations")
data class StudyRecommendationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "recommendation_type")
    val recommendationType: String, // CONTENT, QUIZ, REVIEW, BREAK
    
    @ColumnInfo(name = "subject_id")
    val subjectId: String? = null,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: String? = null,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "reason")
    val reason: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long = System.currentTimeMillis() + 86400000 // 24 hours
)

/**
 * Entity for caching OCR results.
 */
@Entity(tableName = "ocr_cache")
data class OCRCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "image_hash")
    val imageHash: String,
    
    @ColumnInfo(name = "extracted_text")
    val extractedText: String,
    
    @ColumnInfo(name = "language")
    val language: String = "en",
    
    @ColumnInfo(name = "confidence")
    val confidence: Float = 0f,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entity for voice command history.
 */
@Entity(tableName = "voice_commands")
data class VoiceCommandEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "command_text")
    val commandText: String,
    
    @ColumnInfo(name = "recognized_action")
    val recognizedAction: String,
    
    @ColumnInfo(name = "was_successful")
    val wasSuccessful: Boolean,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
