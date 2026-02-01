package com.hdaf.eduapp.domain.model

/**
 * Domain model for Chapter - represents a chapter within a book.
 */
data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val description: String? = null,
    val contentText: String? = null,
    val audioUrl: String? = null,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val durationMinutes: Int = 0,
    val orderIndex: Int = 0,
    val isActive: Boolean = true,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val localVideoPath: String? = null,
    // Additional UI properties
    val bookTitle: String? = null,
    val isCompleted: Boolean = false,
    val readProgress: Float = 0f // 0 to 1
) {
    val isAvailableOffline: Boolean
        get() = isDownloaded && (!localAudioPath.isNullOrEmpty() || !localVideoPath.isNullOrEmpty())
    
    val progressPercentage: Int
        get() = (readProgress * 100).toInt()
    
    val progress: Int
        get() = progressPercentage
        
    val hasAudio: Boolean
        get() = !audioUrl.isNullOrEmpty() || !localAudioPath.isNullOrEmpty()
        
    val hasVideo: Boolean
        get() = !videoUrl.isNullOrEmpty() || !localVideoPath.isNullOrEmpty()
        
    val hasText: Boolean
        get() = !contentText.isNullOrEmpty()
}

enum class ContentType(val mimeType: String) {
    TEXT("text/plain"),
    AUDIO("audio/mpeg"),
    VIDEO("video/mp4"),
    SIGN_LANGUAGE_VIDEO("video/mp4"),
    PDF("application/pdf"),
    INTERACTIVE("application/interactive");
    
    companion object {
        fun fromString(value: String): ContentType {
            return when (value.lowercase()) {
                "text" -> TEXT
                "audio" -> AUDIO
                "video" -> VIDEO
                "sign_language_video", "signlanguage", "sign" -> SIGN_LANGUAGE_VIDEO
                "pdf" -> PDF
                "interactive" -> INTERACTIVE
                else -> TEXT
            }
        }
    }
}

/**
 * Reading progress for a chapter.
 */
data class ChapterProgress(
    val chapterId: String,
    val userId: String,
    val progress: Float,
    val isCompleted: Boolean,
    val timeSpentSeconds: Int,
    val lastReadAt: Long
)
