package com.hdaf.eduapp.domain.model

import java.util.UUID

/**
 * Bookmark for saving important content with quick access.
 * Designed for accessibility with TTS-ready descriptions.
 */
data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val contentType: BookmarkType,
    val contentId: String,          // ID of book, chapter, quiz, etc.
    val title: String,              // Display title
    val description: String? = null,
    val position: Int? = null,      // Position in content (page, time, etc.)
    val positionLabel: String? = null, // Human-readable position ("पृष्ठ 42", "2:30")
    val highlightedText: String? = null, // If text is highlighted
    val note: String? = null,       // User's note
    val color: String = "#FFEB3B",  // Highlight color
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get accessibility description for screen readers.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("बुकमार्क: ")
        append(title)
        positionLabel?.let { append(", $it पर") }
        note?.let { append(". नोट: $it") }
        append(". खोलने के लिए डबल टैप करें।")
    }
}

/**
 * Types of bookmarkable content.
 */
enum class BookmarkType {
    BOOK,
    CHAPTER,
    QUIZ,
    VIDEO,
    AUDIO,
    FLASHCARD_DECK
}

/**
 * Text highlight within content.
 */
data class Highlight(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val chapterId: String,
    val text: String,
    val startPosition: Int,
    val endPosition: Int,
    val color: HighlightColor = HighlightColor.YELLOW,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Get accessibility description.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("हाइलाइट: ")
        append(text.take(50))
        if (text.length > 50) append("...")
        note?.let { append(". नोट: $it") }
    }
}

/**
 * Available highlight colors.
 */
enum class HighlightColor(val hex: String, val nameHindi: String) {
    YELLOW("#FFEB3B", "पीला"),
    GREEN("#4CAF50", "हरा"),
    BLUE("#2196F3", "नीला"),
    PINK("#E91E63", "गुलाबी"),
    ORANGE("#FF9800", "नारंगी")
}

/**
 * Homework reminder for students.
 */
data class HomeworkReminder(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String? = null,
    val subject: String,
    val dueDate: Long,              // Timestamp
    val reminderTime: Long,         // When to remind
    val relatedChapterId: String? = null,
    val relatedQuizId: String? = null,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if reminder is overdue.
     */
    val isOverdue: Boolean
        get() = !isCompleted && dueDate < System.currentTimeMillis()
    
    /**
     * Get days until due.
     */
    val daysUntilDue: Int
        get() {
            val diff = dueDate - System.currentTimeMillis()
            return (diff / (24 * 60 * 60 * 1000)).toInt()
        }
    
    /**
     * Get accessibility description.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("होमवर्क: ")
        append(title)
        append(", विषय: ")
        append(subject)
        
        when {
            isCompleted -> append(". पूर्ण।")
            isOverdue -> append(". समय सीमा समाप्त!")
            daysUntilDue == 0 -> append(". आज तक।")
            daysUntilDue == 1 -> append(". कल तक।")
            else -> append(". ${daysUntilDue} दिन बाकी।")
        }
    }
}

/**
 * Priority levels for reminders.
 */
enum class ReminderPriority(val labelHindi: String) {
    LOW("कम"),
    MEDIUM("मध्यम"),
    HIGH("उच्च"),
    URGENT("तत्काल")
}

/**
 * Study note attached to content.
 */
data class StudyNote(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val content: String,
    val chapterId: String? = null,
    val bookId: String? = null,
    val voiceNoteUrl: String? = null,   // For voice input
    val attachments: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get word count.
     */
    val wordCount: Int
        get() = content.split(Regex("\\s+")).size
    
    /**
     * Get accessibility description.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("नोट: ")
        append(title)
        append(", ")
        append(wordCount)
        append(" शब्द।")
        if (voiceNoteUrl != null) append(" वॉइस नोट शामिल।")
    }
}
