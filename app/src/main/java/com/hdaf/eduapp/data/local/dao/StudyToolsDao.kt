package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Flashcard operations.
 */
@Dao
interface FlashcardDao {
    
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdAt ASC")
    fun getFlashcardsByDeck(deckId: String): Flow<List<FlashcardEntity>>
    
    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: String): FlashcardEntity?
    
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getCardsForReview(deckId: String, currentTime: Long, limit: Int = 20): List<FlashcardEntity>
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    suspend fun getCardCountForDeck(deckId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)
    
    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)
    
    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)
    
    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteFlashcardsByDeck(deckId: String)
}

/**
 * DAO for Flashcard Deck operations.
 */
@Dao
interface FlashcardDeckDao {
    
    @Query("SELECT * FROM flashcard_decks WHERE classId = :classId ORDER BY updatedAt DESC")
    fun getDecksByClass(classId: String): Flow<List<FlashcardDeckEntity>>
    
    @Query("SELECT * FROM flashcard_decks WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getDecksBySubject(subject: String): Flow<List<FlashcardDeckEntity>>
    
    @Query("SELECT * FROM flashcard_decks WHERE chapterId = :chapterId")
    fun getDecksByChapter(chapterId: String): Flow<List<FlashcardDeckEntity>>
    
    @Query("SELECT * FROM flashcard_decks WHERE id = :id")
    suspend fun getDeckById(id: String): FlashcardDeckEntity?
    
    @Query("SELECT * FROM flashcard_decks ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentDecks(limit: Int = 10): Flow<List<FlashcardDeckEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity)
    
    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)
    
    @Delete
    suspend fun deleteDeck(deck: FlashcardDeckEntity)
}

/**
 * DAO for Bookmark operations.
 */
@Dao
interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBookmarksByUser(userId: String): Flow<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE userId = :userId AND contentType = :contentType ORDER BY createdAt DESC")
    fun getBookmarksByType(userId: String, contentType: String): Flow<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE userId = :userId AND contentId = :contentId")
    suspend fun getBookmarkForContent(userId: String, contentId: String): BookmarkEntity?
    
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: String): BookmarkEntity?
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE userId = :userId")
    suspend fun getBookmarkCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
    
    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    @Query("DELETE FROM bookmarks WHERE userId = :userId AND contentId = :contentId")
    suspend fun deleteBookmarkForContent(userId: String, contentId: String)
}

/**
 * DAO for Highlight operations.
 */
@Dao
interface HighlightDao {
    
    @Query("SELECT * FROM highlights WHERE userId = :userId AND chapterId = :chapterId ORDER BY startPosition ASC")
    fun getHighlightsByChapter(userId: String, chapterId: String): Flow<List<HighlightEntity>>
    
    @Query("SELECT * FROM highlights WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHighlights(userId: String): Flow<List<HighlightEntity>>
    
    @Query("SELECT * FROM highlights WHERE id = :id")
    suspend fun getHighlightById(id: String): HighlightEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)
    
    @Update
    suspend fun updateHighlight(highlight: HighlightEntity)
    
    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)
    
    @Query("DELETE FROM highlights WHERE chapterId = :chapterId")
    suspend fun deleteHighlightsByChapter(chapterId: String)
}

/**
 * DAO for Homework Reminder operations.
 */
@Dao
interface HomeworkReminderDao {
    
    @Query("SELECT * FROM homework_reminders WHERE userId = :userId AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingReminders(userId: String): Flow<List<HomeworkReminderEntity>>
    
    @Query("SELECT * FROM homework_reminders WHERE userId = :userId ORDER BY dueDate DESC")
    fun getAllReminders(userId: String): Flow<List<HomeworkReminderEntity>>
    
    @Query("SELECT * FROM homework_reminders WHERE userId = :userId AND dueDate <= :endOfDay AND isCompleted = 0")
    suspend fun getTodayReminders(userId: String, endOfDay: Long): List<HomeworkReminderEntity>
    
    @Query("SELECT * FROM homework_reminders WHERE userId = :userId AND dueDate < :now AND isCompleted = 0")
    suspend fun getOverdueReminders(userId: String, now: Long): List<HomeworkReminderEntity>
    
    @Query("SELECT * FROM homework_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): HomeworkReminderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: HomeworkReminderEntity)
    
    @Update
    suspend fun updateReminder(reminder: HomeworkReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: HomeworkReminderEntity)
    
    @Query("UPDATE homework_reminders SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompleted(id: String, completedAt: Long)
}

/**
 * DAO for Study Note operations.
 */
@Dao
interface StudyNoteDao {
    
    @Query("SELECT * FROM study_notes WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getNotesByUser(userId: String): Flow<List<StudyNoteEntity>>
    
    @Query("SELECT * FROM study_notes WHERE userId = :userId AND chapterId = :chapterId ORDER BY createdAt DESC")
    fun getNotesByChapter(userId: String, chapterId: String): Flow<List<StudyNoteEntity>>
    
    @Query("SELECT * FROM study_notes WHERE userId = :userId AND bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesByBook(userId: String, bookId: String): Flow<List<StudyNoteEntity>>
    
    @Query("SELECT * FROM study_notes WHERE id = :id")
    suspend fun getNoteById(id: String): StudyNoteEntity?
    
    @Query("SELECT * FROM study_notes WHERE userId = :userId ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentNotes(userId: String, limit: Int = 10): Flow<List<StudyNoteEntity>>
    
    @Query("SELECT * FROM study_notes WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(userId: String, query: String): Flow<List<StudyNoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity)
    
    @Update
    suspend fun updateNote(note: StudyNoteEntity)
    
    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)
}
