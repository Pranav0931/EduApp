package com.hdaf.eduapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hdaf.eduapp.data.local.dao.AccessibilityProfileDao
import com.hdaf.eduapp.data.local.dao.AIChatDao
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.local.dao.BookDao
import com.hdaf.eduapp.data.local.dao.ChapterDao
import com.hdaf.eduapp.data.local.dao.ChapterAudioProgressDao
import com.hdaf.eduapp.data.local.dao.OCRCacheDao
import com.hdaf.eduapp.data.local.dao.QuizDao
import com.hdaf.eduapp.data.local.dao.QuizProgressDao
import com.hdaf.eduapp.data.local.dao.StudyAnalyticsDao
import com.hdaf.eduapp.data.local.dao.StudyRecommendationDao
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.local.dao.VoiceCommandDao
import com.hdaf.eduapp.data.local.entity.AccessibilityProfileEntity
import com.hdaf.eduapp.data.local.entity.AIChatMessageEntity
import com.hdaf.eduapp.data.local.entity.BadgeEntity
import com.hdaf.eduapp.data.local.entity.BookEntity
import com.hdaf.eduapp.data.local.entity.ChapterAudioProgressEntity
import com.hdaf.eduapp.data.local.entity.ChapterEntity
import com.hdaf.eduapp.data.local.entity.OCRCacheEntity
import com.hdaf.eduapp.data.local.entity.QuizAttemptEntity
import com.hdaf.eduapp.data.local.entity.QuizEntity
import com.hdaf.eduapp.data.local.entity.QuizProgressEntity
import com.hdaf.eduapp.data.local.entity.QuizQuestionEntity
import com.hdaf.eduapp.data.local.entity.StudyAnalyticsEntity
import com.hdaf.eduapp.data.local.entity.StudyRecommendationEntity
import com.hdaf.eduapp.data.local.entity.UserBadgeEntity
import com.hdaf.eduapp.data.local.entity.UserProgressEntity
import com.hdaf.eduapp.data.local.entity.VoiceCommandEntity
import com.hdaf.eduapp.data.local.converter.DateConverter
import com.hdaf.eduapp.data.local.converter.ListConverter

/**
 * Room Database for EduApp.
 * 
 * This is the single source of truth for local data storage.
 * All data flows through this database.
 * 
 * Schema:
 * - Books: Educational books/subjects
 * - Chapters: Chapters within books
 * - Quizzes: Quiz definitions
 * - QuizQuestions: Individual questions
 * - QuizAttempts: User quiz attempts
 * - UserProgress: Gamification progress
 * - UserBadges: Earned badges
 * - ChapterAudioProgress: Per-chapter audio playback state
 * - QuizProgress: In-progress quiz state for resumption
 * 
 * @version 5 - Added QuizProgressEntity for quiz resumption
 */
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizAttemptEntity::class,
        UserProgressEntity::class,
        UserBadgeEntity::class,
        BadgeEntity::class,
        // Accessibility entities
        AccessibilityProfileEntity::class,
        AIChatMessageEntity::class,
        StudyAnalyticsEntity::class,
        StudyRecommendationEntity::class,
        OCRCacheEntity::class,
        VoiceCommandEntity::class,
        // Audio progress entity
        ChapterAudioProgressEntity::class,
        // Quiz progress entity
        QuizProgressEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class EduAppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun quizDao(): QuizDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userProgressDao(): UserProgressDao
    
    // Accessibility DAOs
    abstract fun accessibilityProfileDao(): AccessibilityProfileDao
    abstract fun aiChatDao(): AIChatDao
    abstract fun studyAnalyticsDao(): StudyAnalyticsDao
    abstract fun studyRecommendationDao(): StudyRecommendationDao
    abstract fun ocrCacheDao(): OCRCacheDao
    abstract fun voiceCommandDao(): VoiceCommandDao
    
    // Audio progress DAO
    abstract fun chapterAudioProgressDao(): ChapterAudioProgressDao
    
    // Quiz progress DAO (for resumption)
    abstract fun quizProgressDao(): QuizProgressDao

    companion object {
        const val DATABASE_NAME = "eduapp_database"
    }
}
