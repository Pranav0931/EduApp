package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdaf.eduapp.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Book operations.
 */
@Dao
interface BookDao {

    /**
     * Insert a single book.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity)

    /**
     * Insert multiple books.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    /**
     * Update a book.
     */
    @Update
    suspend fun update(book: BookEntity)

    /**
     * Delete a book.
     */
    @Delete
    suspend fun delete(book: BookEntity)

    /**
     * Get all books as Flow (reactive).
     */
    @Query("SELECT * FROM books WHERE is_active = 1 ORDER BY order_index ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    /**
     * Get books by class ID as Flow.
     */
    @Query("SELECT * FROM books WHERE class_id = :classId AND is_active = 1 ORDER BY order_index ASC")
    fun getBooksByClass(classId: String): Flow<List<BookEntity>>

    /**
     * Get books by class ID (non-Flow for sync).
     */
    @Query("SELECT * FROM books WHERE class_id = :classId AND is_active = 1 ORDER BY order_index ASC")
    suspend fun getBooksByClassSync(classId: String): List<BookEntity>

    /**
     * Get a single book by ID.
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    /**
     * Get a single book by ID as Flow.
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookByIdFlow(bookId: String): Flow<BookEntity?>

    /**
     * Get unsynced books for cloud sync.
     */
    @Query("SELECT * FROM books WHERE is_synced = 0")
    suspend fun getUnsyncedBooks(): List<BookEntity>

    /**
     * Mark books as synced.
     */
    @Query("UPDATE books SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    /**
     * Delete all books.
     */
    @Query("DELETE FROM books")
    suspend fun deleteAll()

    /**
     * Get book count for a class.
     */
    @Query("SELECT COUNT(*) FROM books WHERE class_id = :classId AND is_active = 1")
    suspend fun getBookCount(classId: String): Int

    /**
     * Get books by class and subject.
     */
    @Query("SELECT * FROM books WHERE class_id = :classId AND subject = :subject AND is_active = 1 ORDER BY order_index ASC")
    fun getBooksByClassAndSubject(classId: String, subject: String): Flow<List<BookEntity>>

    /**
     * Search books by title within a class.
     */
    @Query("SELECT * FROM books WHERE title LIKE :searchPattern AND class_id = :classId AND is_active = 1 ORDER BY title ASC")
    fun searchBooks(searchPattern: String, classId: String): Flow<List<BookEntity>>

    /**
     * Get downloaded books.
     */
    @Query("SELECT * FROM books WHERE is_downloaded = 1 ORDER BY title ASC")
    fun getDownloadedBooks(): Flow<List<BookEntity>>

    /**
     * Update download status for a book.
     */
    @Query("UPDATE books SET is_downloaded = :isDownloaded, download_progress = :progress WHERE id = :bookId")
    suspend fun updateDownloadStatus(bookId: String, isDownloaded: Boolean, progress: Float)
}
