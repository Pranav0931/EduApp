package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for user notes.
 */
@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["chapterId"]),
        Index(value = ["bookId"]),
        Index(value = ["createdAt"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val chapterTitle: String,
    val bookId: String,
    val content: String,
    val highlightColor: String = "YELLOW",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

// Note: BookmarkEntity is already defined in StudyToolsEntities.kt
// We reuse that entity for bookmarks. See BookmarkEntity in StudyToolsEntities.kt

/**
 * Room entity for text highlights.
 * This is a more specific highlight entity for chapter reading position.
 */
@Entity(
    tableName = "text_highlights",
    indices = [
        Index(value = ["chapterId"])
    ]
)
data class TextHighlightEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val startOffset: Int,
    val endOffset: Int,
    val highlightedText: String,
    val color: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
