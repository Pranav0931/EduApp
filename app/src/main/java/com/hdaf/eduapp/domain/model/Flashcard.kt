package com.hdaf.eduapp.domain.model

import java.util.UUID

/**
 * Flashcard for spaced repetition learning.
 * Accessible design with TTS-ready content.
 */
data class Flashcard(
    val id: String = UUID.randomUUID().toString(),
    val front: String,              // Question or term
    val back: String,               // Answer or definition
    val hint: String? = null,       // Optional hint
    val imageUrl: String? = null,   // Optional image
    val audioUrl: String? = null,   // Optional audio pronunciation
    val deckId: String,             // Parent deck ID
    val difficulty: FlashcardDifficulty = FlashcardDifficulty.MEDIUM,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
) {
    /**
     * Get success rate as percentage.
     */
    val successRate: Int
        get() = if (reviewCount > 0) (correctCount * 100 / reviewCount) else 0
    
    /**
     * Get accessibility description for screen readers.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("फ्लैशकार्ड: ")
        append(front)
        append(". उत्तर देखने के लिए डबल टैप करें।")
        if (hint != null) {
            append(" संकेत उपलब्ध है।")
        }
    }
}

/**
 * Flashcard deck/collection.
 */
data class FlashcardDeck(
    val id: String = UUID.randomUUID().toString(),
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
) {
    /**
     * Get mastery percentage.
     */
    val masteryPercent: Int
        get() = if (cardCount > 0) (masteredCount * 100 / cardCount) else 0
    
    /**
     * Get accessibility description.
     */
    fun getAccessibilityDescription(): String = buildString {
        append("फ्लैशकार्ड डेक: ")
        append(name)
        append(", ")
        append(cardCount)
        append(" कार्ड, ")
        append(masteryPercent)
        append(" प्रतिशत पूर्ण।")
    }
}

/**
 * Difficulty levels for flashcards.
 */
enum class FlashcardDifficulty {
    EASY,
    MEDIUM,
    HARD
}

/**
 * Review result for spaced repetition algorithm.
 */
enum class ReviewResult {
    AGAIN,      // Forgot - review soon
    HARD,       // Difficult - review in shorter interval
    GOOD,       // Remembered - normal interval
    EASY        // Very easy - longer interval
}

/**
 * Study session statistics.
 */
data class FlashcardStudySession(
    val deckId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val cardsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val averageResponseTimeMs: Long = 0
) {
    val accuracy: Int
        get() = if (cardsReviewed > 0) (correctAnswers * 100 / cardsReviewed) else 0
}
