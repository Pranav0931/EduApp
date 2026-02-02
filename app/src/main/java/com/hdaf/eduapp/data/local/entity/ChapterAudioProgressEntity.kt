package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing chapter-specific audio playback progress.
 * Enables per-chapter audio isolation with resume functionality.
 * 
 * This solves the critical issue where all chapters shared a single audio state.
 */
@Entity(tableName = "chapter_audio_progress")
data class ChapterAudioProgressEntity(
    @PrimaryKey
    val chapterId: String,
    
    /** Current playback position in milliseconds */
    val positionMs: Long = 0L,
    
    /** Total duration of the audio in milliseconds */
    val durationMs: Long = 0L,
    
    /** Playback speed (0.5f to 2.0f) */
    val playbackSpeed: Float = 1.0f,
    
    /** Timestamp when this chapter was last played */
    val lastPlayedAt: Long = System.currentTimeMillis(),
    
    /** Whether the chapter audio has been completed (100%) */
    val isCompleted: Boolean = false,
    
    /** Number of times this chapter was listened to */
    val playCount: Int = 0,
    
    /** Total time spent listening to this chapter in seconds */
    val totalListenTimeSeconds: Long = 0L,
    
    /** The chapter title for display purposes */
    val chapterTitle: String = "",
    
    /** The book ID this chapter belongs to */
    val bookId: String = ""
)
