package com.hdaf.eduapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdaf.eduapp.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Chapter operations.
 */
@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Update
    suspend fun update(chapter: ChapterEntity)

    @Delete
    suspend fun delete(chapter: ChapterEntity)

    /**
     * Get all chapters as Flow.
     */
    @Query("SELECT * FROM chapters WHERE is_active = 1 ORDER BY order_index ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    /**
     * Get chapters by book ID as Flow.
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND is_active = 1 ORDER BY order_index ASC")
    fun getChaptersByBook(bookId: String): Flow<List<ChapterEntity>>

    /**
     * Get chapters by book ID (sync version).
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND is_active = 1 ORDER BY order_index ASC")
    suspend fun getChaptersByBookSync(bookId: String): List<ChapterEntity>

    /**
     * Get a single chapter by ID.
     */
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    /**
     * Get chapter by ID as Flow.
     */
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterByIdFlow(chapterId: String): Flow<ChapterEntity?>

    /**
     * Get downloaded chapters.
     */
    @Query("SELECT * FROM chapters WHERE is_downloaded = 1 ORDER BY updated_at DESC")
    fun getDownloadedChapters(): Flow<List<ChapterEntity>>

    /**
     * Get downloaded chapters count.
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE is_downloaded = 1")
    suspend fun getDownloadedCount(): Int

    /**
     * Mark chapter as downloaded.
     */
    @Query("UPDATE chapters SET is_downloaded = 1, local_audio_path = :audioPath, local_video_path = :videoPath WHERE id = :chapterId")
    suspend fun markAsDownloaded(chapterId: String, audioPath: String?, videoPath: String?)

    /**
     * Clear download for chapter.
     */
    @Query("UPDATE chapters SET is_downloaded = 0, local_audio_path = NULL, local_video_path = NULL WHERE id = :chapterId")
    suspend fun clearDownload(chapterId: String)

    /**
     * Get unsynced chapters.
     */
    @Query("SELECT * FROM chapters WHERE is_synced = 0")
    suspend fun getUnsyncedChapters(): List<ChapterEntity>

    /**
     * Mark chapters as synced.
     */
    @Query("UPDATE chapters SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    /**
     * Delete all chapters.
     */
    @Query("DELETE FROM chapters")
    suspend fun deleteAll()

    /**
     * Get chapter count for a book.
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE book_id = :bookId AND is_active = 1")
    suspend fun getChapterCount(bookId: String): Int

    /**
     * Search chapters by title.
     */
    @Query("SELECT * FROM chapters WHERE title LIKE '%' || :query || '%' AND is_active = 1 ORDER BY title ASC")
    fun searchChapters(query: String): Flow<List<ChapterEntity>>

    /**
     * Clear downloaded chapters for a book.
     */
    @Query("UPDATE chapters SET is_downloaded = 0, local_audio_path = NULL, local_video_path = NULL WHERE book_id = :bookId")
    suspend fun clearDownloadedChapters(bookId: String)

    /**
     * Get downloaded chapters for a specific book.
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND is_downloaded = 1 ORDER BY order_index ASC")
    fun getDownloadedChapters(bookId: String): Flow<List<ChapterEntity>>

    /**
     * Update chapter read progress.
     */
    @Query("UPDATE chapters SET read_progress = :progress, last_read_at = :lastReadAt WHERE id = :chapterId")
    suspend fun updateReadProgress(chapterId: String, progress: Float, lastReadAt: Long)

    /**
     * Get recently read chapters.
     */
    @Query("SELECT * FROM chapters WHERE last_read_at > 0 ORDER BY last_read_at DESC LIMIT :limit")
    fun getRecentlyRead(limit: Int): Flow<List<ChapterEntity>>
}
