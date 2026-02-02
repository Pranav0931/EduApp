package com.hdaf.eduapp.core.di

import android.content.Context
import androidx.room.Room
import com.hdaf.eduapp.data.local.EduAppDatabase
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.local.dao.BookDao
import com.hdaf.eduapp.data.local.dao.ChapterAudioProgressDao
import com.hdaf.eduapp.data.local.dao.ChapterDao
import com.hdaf.eduapp.data.local.dao.DailyStudySummaryDao
import com.hdaf.eduapp.data.local.dao.DownloadedContentDao
import com.hdaf.eduapp.data.local.dao.DownloadQueueDao
import com.hdaf.eduapp.data.local.dao.NotesDao
import com.hdaf.eduapp.data.local.dao.QuizDao
import com.hdaf.eduapp.data.local.dao.QuizProgressDao
import com.hdaf.eduapp.data.local.dao.StudyGoalDao
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
            .fallbackToDestructiveMigration() // TODO: Add proper migrations for production
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
}
