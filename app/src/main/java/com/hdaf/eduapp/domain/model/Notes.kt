package com.hdaf.eduapp.domain.model

/**
 * Domain model for user notes on chapters.
 * This extends the existing study tools with chapter-specific notes.
 */
data class Note(
    val id: String,
    val chapterId: String,
    val chapterTitle: String,
    val bookId: String,
    val content: String,
    val highlightColor: HighlightColor = HighlightColor.YELLOW,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

// Note: Bookmark and HighlightColor are already defined in StudyTools.kt
// We reuse those definitions instead of creating duplicates.

/**
 * Text highlight within content - extension data for persistence.
 * Note: This is separate from Highlight in StudyTools.kt which is the domain model.
 * This adds persistence-specific fields.
 */
data class TextHighlight(
    val id: String,
    val chapterId: String,
    val startOffset: Int,
    val endOffset: Int,
    val highlightedText: String,
    val color: HighlightColor,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
