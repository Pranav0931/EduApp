package com.hdaf.eduapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.local.dao.BookDao
import com.hdaf.eduapp.data.local.dao.ChapterDao
import com.hdaf.eduapp.data.local.dao.QuizDao
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.local.entity.BadgeEntity
import com.hdaf.eduapp.data.local.entity.BookEntity
import com.hdaf.eduapp.data.local.entity.ChapterEntity
import com.hdaf.eduapp.data.local.entity.QuizAttemptEntity
import com.hdaf.eduapp.data.local.entity.QuizEntity
import com.hdaf.eduapp.data.local.entity.QuizQuestionEntity
import com.hdaf.eduapp.data.local.entity.UserBadgeEntity
import com.hdaf.eduapp.data.local.entity.UserProgressEntity
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
 * 
 * @version 1 - Initial schema
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
        BadgeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class EduAppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun quizDao(): QuizDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        const val DATABASE_NAME = "eduapp_database"
    }
}
