package com.hdaf.eduapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hdaf.eduapp.data.local.EduAppDatabase
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.local.dao.BookDao
import com.hdaf.eduapp.data.local.dao.BookmarkDao
import com.hdaf.eduapp.data.local.dao.ChapterAudioProgressDao
import com.hdaf.eduapp.data.local.dao.ChapterDao
import com.hdaf.eduapp.data.local.dao.DailyStudySummaryDao
import com.hdaf.eduapp.data.local.dao.DownloadedContentDao
import com.hdaf.eduapp.data.local.dao.DownloadQueueDao
import com.hdaf.eduapp.data.local.dao.FlashcardDao
import com.hdaf.eduapp.data.local.dao.FlashcardDeckDao
import com.hdaf.eduapp.data.local.dao.HighlightDao
import com.hdaf.eduapp.data.local.dao.HomeworkReminderDao
import com.hdaf.eduapp.data.local.dao.NotesDao
import com.hdaf.eduapp.data.local.dao.QuizDao
import com.hdaf.eduapp.data.local.dao.QuizProgressDao
import com.hdaf.eduapp.data.local.dao.StudyGoalDao
import com.hdaf.eduapp.data.local.dao.StudyNoteDao
import com.hdaf.eduapp.data.local.dao.StudyPlanDao
import com.hdaf.eduapp.data.local.dao.StudySessionDao
import com.hdaf.eduapp.data.local.dao.TextHighlightsDao
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module.
 * 
 * Provides:
 * - Room database instance
 * - DAO instances for data access
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "eduapp_database"

    /**
     * Migration from version 6 to 7.
     * Adds study tools tables: flashcards, flashcard_decks, bookmarks,
     * highlights, homework_reminders, study_notes.
     */
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Flashcards table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `flashcards` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `front` TEXT NOT NULL,
                    `back` TEXT NOT NULL,
                    `hint` TEXT,
                    `imageUrl` TEXT,
                    `audioUrl` TEXT,
                    `deckId` TEXT NOT NULL,
                    `difficulty` TEXT NOT NULL DEFAULT 'MEDIUM',
                    `lastReviewedAt` INTEGER,
                    `nextReviewAt` INTEGER,
                    `reviewCount` INTEGER NOT NULL DEFAULT 0,
                    `correctCount` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    `tags` TEXT NOT NULL DEFAULT ''
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_deckId` ON `flashcards` (`deckId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_nextReviewAt` ON `flashcards` (`nextReviewAt`)")

            // Flashcard Decks table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `flashcard_decks` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `description` TEXT,
                    `subject` TEXT NOT NULL,
                    `classId` TEXT NOT NULL,
                    `chapterId` TEXT,
                    `cardCount` INTEGER NOT NULL DEFAULT 0,
                    `masteredCount` INTEGER NOT NULL DEFAULT 0,
                    `coverImageUrl` TEXT,
                    `isPublic` INTEGER NOT NULL DEFAULT 0,
                    `createdBy` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcard_decks_subject` ON `flashcard_decks` (`subject`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcard_decks_classId` ON `flashcard_decks` (`classId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcard_decks_chapterId` ON `flashcard_decks` (`chapterId`)")

            // Bookmarks table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `bookmarks` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `userId` TEXT NOT NULL,
                    `contentType` TEXT NOT NULL,
                    `contentId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `position` INTEGER,
                    `positionLabel` TEXT,
                    `highlightedText` TEXT,
                    `note` TEXT,
                    `color` TEXT NOT NULL DEFAULT '#FFEB3B',
                    `tags` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_userId` ON `bookmarks` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_contentType` ON `bookmarks` (`contentType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_createdAt` ON `bookmarks` (`createdAt`)")

            // Highlights table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `highlights` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `userId` TEXT NOT NULL,
                    `chapterId` TEXT NOT NULL,
                    `text` TEXT NOT NULL,
                    `startPosition` INTEGER NOT NULL,
                    `endPosition` INTEGER NOT NULL,
                    `color` TEXT NOT NULL DEFAULT 'YELLOW',
                    `note` TEXT,
                    `createdAt` INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_highlights_userId` ON `highlights` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_highlights_chapterId` ON `highlights` (`chapterId`)")

            // Homework Reminders table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `homework_reminders` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `userId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `subject` TEXT NOT NULL,
                    `dueDate` INTEGER NOT NULL,
                    `reminderTime` INTEGER NOT NULL,
                    `relatedChapterId` TEXT,
                    `relatedQuizId` TEXT,
                    `priority` TEXT NOT NULL DEFAULT 'MEDIUM',
                    `isCompleted` INTEGER NOT NULL DEFAULT 0,
                    `completedAt` INTEGER,
                    `createdAt` INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_reminders_userId` ON `homework_reminders` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_reminders_dueDate` ON `homework_reminders` (`dueDate`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_reminders_isCompleted` ON `homework_reminders` (`isCompleted`)")

            // Study Notes table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `study_notes` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `userId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `chapterId` TEXT,
                    `bookId` TEXT,
                    `voiceNoteUrl` TEXT,
                    `attachments` TEXT NOT NULL DEFAULT '',
                    `tags` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_notes_userId` ON `study_notes` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_notes_chapterId` ON `study_notes` (`chapterId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_notes_bookId` ON `study_notes` (`bookId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_notes_createdAt` ON `study_notes` (`createdAt`)")
        }
    }

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EduAppDatabase {
        return Room.databaseBuilder(
            context,
            EduAppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_6_7)
            .fallbackToDestructiveMigration() // Fallback only for older pre-release versions
            .build()
    }

    /**
     * Provides BookDao for book-related database operations.
     */
    @Provides
    @Singleton
    fun provideBookDao(database: EduAppDatabase): BookDao {
        return database.bookDao()
    }

    /**
     * Provides ChapterDao for chapter-related database operations.
     */
    @Provides
    @Singleton
    fun provideChapterDao(database: EduAppDatabase): ChapterDao {
        return database.chapterDao()
    }

    /**
     * Provides QuizDao for quiz-related database operations.
     */
    @Provides
    @Singleton
    fun provideQuizDao(database: EduAppDatabase): QuizDao {
        return database.quizDao()
    }

    /**
     * Provides UserProgressDao for user progress tracking.
     */
    @Provides
    @Singleton
    fun provideUserProgressDao(database: EduAppDatabase): UserProgressDao {
        return database.userProgressDao()
    }

    /**
     * Provides BadgeDao for badge-related database operations.
     */
    @Provides
    @Singleton
    fun provideBadgeDao(database: EduAppDatabase): BadgeDao {
        return database.badgeDao()
    }
    
    /**
     * Provides ChapterAudioProgressDao for chapter-wise audio progress tracking.
     */
    @Provides
    @Singleton
    fun provideChapterAudioProgressDao(database: EduAppDatabase): ChapterAudioProgressDao {
        return database.chapterAudioProgressDao()
    }
    
    /**
     * Provides QuizProgressDao for quiz resumption functionality.
     */
    @Provides
    @Singleton
    fun provideQuizProgressDao(database: EduAppDatabase): QuizProgressDao {
        return database.quizProgressDao()
    }
    
    // ========== Notes DAOs ==========
    
    /**
     * Provides NotesDao for user notes management.
     */
    @Provides
    @Singleton
    fun provideNotesDao(database: EduAppDatabase): NotesDao {
        return database.notesDao()
    }
    
    // Note: BookmarkDao is already provided by existing DI setup in StudyToolsDao
    
    /**
     * Provides TextHighlightsDao for text highlighting functionality.
     */
    @Provides
    @Singleton
    fun provideTextHighlightsDao(database: EduAppDatabase): TextHighlightsDao {
        return database.textHighlightsDao()
    }
    
    // ========== Download Manager DAOs ==========
    
    /**
     * Provides DownloadedContentDao for managing downloaded content.
     */
    @Provides
    @Singleton
    fun provideDownloadedContentDao(database: EduAppDatabase): DownloadedContentDao {
        return database.downloadedContentDao()
    }
    
    /**
     * Provides DownloadQueueDao for managing download queue.
     */
    @Provides
    @Singleton
    fun provideDownloadQueueDao(database: EduAppDatabase): DownloadQueueDao {
        return database.downloadQueueDao()
    }
    
    // ========== Study Planner DAOs ==========
    
    /**
     * Provides StudySessionDao for managing study sessions.
     */
    @Provides
    @Singleton
    fun provideStudySessionDao(database: EduAppDatabase): StudySessionDao {
        return database.studySessionDao()
    }
    
    /**
     * Provides StudyPlanDao for managing study plans.
     */
    @Provides
    @Singleton
    fun provideStudyPlanDao(database: EduAppDatabase): StudyPlanDao {
        return database.studyPlanDao()
    }
    
    /**
     * Provides StudyGoalDao for managing study goals.
     */
    @Provides
    @Singleton
    fun provideStudyGoalDao(database: EduAppDatabase): StudyGoalDao {
        return database.studyGoalDao()
    }
    
    /**
     * Provides DailyStudySummaryDao for daily study statistics.
     */
    @Provides
    @Singleton
    fun provideDailyStudySummaryDao(database: EduAppDatabase): DailyStudySummaryDao {
        return database.dailyStudySummaryDao()
    }
    
    // ========== Study Tools DAOs ==========
    
    @Provides
    @Singleton
    fun provideFlashcardDao(database: EduAppDatabase): FlashcardDao {
        return database.flashcardDao()
    }
    
    @Provides
    @Singleton
    fun provideFlashcardDeckDao(database: EduAppDatabase): FlashcardDeckDao {
        return database.flashcardDeckDao()
    }
    
    @Provides
    @Singleton
    fun provideBookmarkDao(database: EduAppDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
    
    @Provides
    @Singleton
    fun provideHighlightDao(database: EduAppDatabase): HighlightDao {
        return database.highlightDao()
    }
    
    @Provides
    @Singleton
    fun provideHomeworkReminderDao(database: EduAppDatabase): HomeworkReminderDao {
        return database.homeworkReminderDao()
    }
    
    @Provides
    @Singleton
    fun provideStudyNoteDao(database: EduAppDatabase): StudyNoteDao {
        return database.studyNoteDao()
    }
}
