package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisting accessibility profile settings.
 */
@Entity(tableName = "accessibility_profiles")
data class AccessibilityProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "accessibility_mode")
    val accessibilityMode: String = "NORMAL",
    
    @ColumnInfo(name = "content_delivery_mode")
    val contentDeliveryMode: String = "MIXED",
    
    // Screen reader settings
    @ColumnInfo(name = "screen_reader_enabled")
    val screenReaderEnabled: Boolean = false,
    
    @ColumnInfo(name = "talkback_optimized")
    val talkBackOptimized: Boolean = false,
    
    @ColumnInfo(name = "speech_rate")
    val speechRate: String = "NORMAL",
    
    @ColumnInfo(name = "announce_focus_changes")
    val announceFocusChanges: Boolean = true,
    
    // Visual settings
    @ColumnInfo(name = "font_scale")
    val fontScale: Float = 1.0f,
    
    @ColumnInfo(name = "contrast_level")
    val contrastLevel: String = "NORMAL",
    
    @ColumnInfo(name = "reduce_motion")
    val reduceMotion: Boolean = false,
    
    @ColumnInfo(name = "large_text_enabled")
    val largeTextEnabled: Boolean = false,
    
    @ColumnInfo(name = "bold_text_enabled")
    val boldTextEnabled: Boolean = false,
    
    // Audio settings
    @ColumnInfo(name = "subtitles_enabled")
    val subtitlesEnabled: Boolean = false,
    
    @ColumnInfo(name = "sign_language_mode_enabled")
    val signLanguageModeEnabled: Boolean = false,
    
    @ColumnInfo(name = "haptic_feedback_enabled")
    val hapticFeedbackEnabled: Boolean = true,
    
    @ColumnInfo(name = "audio_descriptions_enabled")
    val audioDescriptionsEnabled: Boolean = false,
    
    // Navigation
    @ColumnInfo(name = "voice_navigation_enabled")
    val voiceNavigationEnabled: Boolean = false,
    
    @ColumnInfo(name = "gesture_navigation_enabled")
    val gestureNavigationEnabled: Boolean = true,
    
    @ColumnInfo(name = "simplified_navigation_enabled")
    val simplifiedNavigationEnabled: Boolean = false,
    
    // Learning preferences
    @ColumnInfo(name = "prefer_audio_content")
    val preferAudioContent: Boolean = false,
    
    @ColumnInfo(name = "prefer_text_content")
    val preferTextContent: Boolean = false,
    
    @ColumnInfo(name = "prefer_visual_content")
    val preferVisualContent: Boolean = false,
    
    @ColumnInfo(name = "auto_read_content")
    val autoReadContent: Boolean = false,
    
    @ColumnInfo(name = "extended_time_for_quizzes")
    val extendedTimeForQuizzes: Boolean = false,
    
    @ColumnInfo(name = "quiz_time_multiplier")
    val quizTimeMultiplier: Float = 1.0f,
    
    // Timestamps
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
