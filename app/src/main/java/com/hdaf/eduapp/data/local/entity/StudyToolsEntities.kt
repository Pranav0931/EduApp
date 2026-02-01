package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Database entity for Flashcard.
 */
@Entity(
    tableName = "flashcards",
    indices = [
        Index("deckId"),
        Index("nextReviewAt")
    ]
)
data class FlashcardEntity(
    @PrimaryKey
    val id: String,
    val front: String,
    val back: String,
    val hint: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val deckId: String,
    val difficulty: String = "MEDIUM",
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: String = ""   // Comma-separated
)

/**
 * Database entity for Flashcard Deck.
 */
@Entity(
    tableName = "flashcard_decks",
    indices = [
        Index("subject"),
        Index("classId"),
        Index("chapterId")
    ]
)
data class FlashcardDeckEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val subject: String,
    val classId: String,
    val chapterId: String? = null,
    val cardCount: Int = 0,
    val masteredCount: Int = 0,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = false,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Database entity for Bookmark.
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index("userId"),
        Index("contentType"),
        Index("createdAt")
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentType: String,    // BookmarkType enum name
    val contentId: String,
    val title: String,
    val description: String? = null,
    val position: Int? = null,
    val positionLabel: String? = null,
    val highlightedText: String? = null,
    val note: String? = null,
    val color: String = "#FFEB3B",
    val tags: String = "",      // Comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Database entity for Highlight.
 */
@Entity(
    tableName = "highlights",
    indices = [
        Index("userId"),
        Index("chapterId")
    ]
)
data class HighlightEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val chapterId: String,
    val text: String,
    val startPosition: Int,
    val endPosition: Int,
    val color: String = "YELLOW",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Database entity for Homework Reminder.
 */
@Entity(
    tableName = "homework_reminders",
    indices = [
        Index("userId"),
        Index("dueDate"),
        Index("isCompleted")
    ]
)
data class HomeworkReminderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val subject: String,
    val dueDate: Long,
    val reminderTime: Long,
    val relatedChapterId: String? = null,
    val relatedQuizId: String? = null,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Database entity for Study Note.
 */
@Entity(
    tableName = "study_notes",
    indices = [
        Index("userId"),
        Index("chapterId"),
        Index("bookId"),
        Index("createdAt")
    ]
)
data class StudyNoteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val chapterId: String? = null,
    val bookId: String? = null,
    val voiceNoteUrl: String? = null,
    val attachments: String = "",   // Comma-separated URLs
    val tags: String = "",          // Comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
