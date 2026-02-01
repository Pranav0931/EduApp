package com.hdaf.eduapp.domain.model

/**
 * Domain model for Book - represents a subject book for a class.
 */
data class Book(
    val id: String,
    val classId: String,
    val name: String,
    val title: String = "",
    val subject: String = "",
    val description: String? = null,
    val iconUrl: String? = null,
    val coverUrl: String? = null,
    val orderIndex: Int = 0,
    val isActive: Boolean = true,
    // Fields for offline support (not in entity but useful for domain)
    val completedChapters: Int = 0,
    val totalChapters: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f
) {
    // Convenience properties for UI
    val displayTitle: String get() = title.ifEmpty { name }
    val classLevel: Int get() = classId.filter { it.isDigit() }.toIntOrNull() ?: 1
    val progress: Int get() = if (totalChapters > 0) {
        ((completedChapters.toFloat() / totalChapters) * 100).toInt()
    } else 0
}

enum class Subject(val displayName: String) {
    HINDI("हिंदी"),
    ENGLISH("English"),
    MARATHI("मराठी"),
    MATH("गणित"),
    SCIENCE("विज्ञान"),
    SOCIAL_STUDIES("सामाजिक विज्ञान"),
    SANSKRIT("संस्कृत"),
    GENERAL_KNOWLEDGE("सामान्य ज्ञान"),
    COMPUTER("कंप्यूटर"),
    ENVIRONMENTAL_STUDIES("पर्यावरण अध्ययन");
    
    companion object {
        fun fromString(value: String): Subject {
            return when (value.lowercase().replace(" ", "_")) {
                "hindi" -> HINDI
                "english" -> ENGLISH
                "marathi" -> MARATHI
                "math", "mathematics" -> MATH
                "science" -> SCIENCE
                "social_studies", "social", "socialstudies" -> SOCIAL_STUDIES
                "sanskrit" -> SANSKRIT
                "general_knowledge", "gk", "generalknowledge" -> GENERAL_KNOWLEDGE
                "computer", "computers" -> COMPUTER
                "environmental_studies", "evs", "environment" -> ENVIRONMENTAL_STUDIES
                else -> HINDI
            }
        }
    }
}

enum class Language(val displayName: String, val code: String) {
    HINDI("हिंदी", "hi"),
    ENGLISH("English", "en"),
    MARATHI("मराठी", "mr");
    
    companion object {
        fun fromString(value: String): Language {
            return when (value.lowercase()) {
                "hindi", "hi" -> HINDI
                "english", "en" -> ENGLISH
                "marathi", "mr" -> MARATHI
                else -> HINDI
            }
        }
    }
}
