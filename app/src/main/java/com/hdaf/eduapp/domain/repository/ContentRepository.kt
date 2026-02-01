package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.model.ChapterProgress
import com.hdaf.eduapp.domain.model.Language
import com.hdaf.eduapp.domain.model.Subject
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for content (books and chapters) operations.
 * Implements offline-first pattern with sync support.
 */
interface ContentRepository {

    // ==================== Books ====================

    /**
     * Get all books for a specific class.
     * Returns cached data first, then fetches from network.
     */
    fun getBooksByClass(classId: String): Flow<Resource<List<Book>>>

    /**
     * Get books filtered by subject.
     */
    fun getBooksBySubject(classId: String, subject: Subject): Flow<Resource<List<Book>>>

    /**
     * Get a specific book by ID.
     */
    fun getBookById(bookId: String): Flow<Resource<Book>>

    /**
     * Search books by title or subject.
     */
    fun searchBooks(query: String, classId: String): Flow<Resource<List<Book>>>

    /**
     * Get downloaded books available offline.
     */
    fun getDownloadedBooks(): Flow<List<Book>>

    /**
     * Download a book for offline use.
     */
    suspend fun downloadBook(bookId: String): Flow<Resource<Float>> // Progress 0-1

    /**
     * Delete downloaded book to free space.
     */
    suspend fun deleteDownloadedBook(bookId: String): Resource<Unit>

    // ==================== Chapters ====================

    /**
     * Get all chapters for a book.
     */
    fun getChaptersByBook(bookId: String): Flow<Resource<List<Chapter>>>

    /**
     * Get a specific chapter by ID.
     */
    fun getChapterById(chapterId: String): Flow<Resource<Chapter>>

    /**
     * Get chapter content (text, audio URL, video URL).
     */
    suspend fun getChapterContent(chapterId: String): Resource<Chapter>

    /**
     * Get downloaded chapters.
     */
    fun getDownloadedChapters(bookId: String): Flow<List<Chapter>>

    /**
     * Download a chapter for offline reading.
     */
    suspend fun downloadChapter(chapterId: String): Flow<Resource<Float>>

    /**
     * Delete downloaded chapter.
     */
    suspend fun deleteDownloadedChapter(chapterId: String): Resource<Unit>

    // ==================== Progress ====================

    /**
     * Update reading progress for a chapter.
     */
    suspend fun updateChapterProgress(
        chapterId: String,
        progress: Float,
        timeSpentSeconds: Int
    ): Resource<Unit>

    /**
     * Mark chapter as completed.
     */
    suspend fun markChapterCompleted(chapterId: String): Resource<Unit>

    /**
     * Get reading progress for all chapters in a book.
     */
    fun getBookProgress(bookId: String): Flow<Map<String, ChapterProgress>>

    /**
     * Get recently read chapters.
     */
    fun getRecentlyReadChapters(limit: Int = 10): Flow<List<Chapter>>

    // ==================== Content by Language ====================

    /**
     * Get chapter content in a specific language.
     */
    suspend fun getChapterContentByLanguage(
        chapterId: String,
        language: Language
    ): Resource<Chapter>

    // ==================== Sync ====================

    /**
     * Sync all content with server.
     */
    suspend fun syncContent(): Resource<Unit>

    /**
     * Check for content updates.
     */
    suspend fun checkForUpdates(): Resource<Boolean>

    /**
     * Get last sync timestamp.
     */
    fun getLastSyncTime(): Flow<Long?>
}
