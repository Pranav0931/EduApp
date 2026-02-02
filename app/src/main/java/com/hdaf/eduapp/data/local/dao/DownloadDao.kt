package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.DownloadedContentEntity
import com.hdaf.eduapp.data.local.entity.DownloadQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for downloaded content operations.
 */
@Dao
interface DownloadedContentDao {
    
    @Query("SELECT * FROM downloaded_content ORDER BY downloadedAt DESC")
    fun getAllDownloadedContent(): Flow<List<DownloadedContentEntity>>
    
    @Query("SELECT * FROM downloaded_content WHERE chapterId = :chapterId")
    fun getDownloadedContentByChapter(chapterId: String): Flow<List<DownloadedContentEntity>>
    
    @Query("SELECT * FROM downloaded_content WHERE bookId = :bookId")
    fun getDownloadedContentByBook(bookId: String): Flow<List<DownloadedContentEntity>>
    
    @Query("SELECT * FROM downloaded_content WHERE contentType = :contentType ORDER BY downloadedAt DESC")
    fun getDownloadedContentByType(contentType: String): Flow<List<DownloadedContentEntity>>
    
    @Query("SELECT * FROM downloaded_content WHERE id = :contentId")
    suspend fun getDownloadedContentById(contentId: String): DownloadedContentEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_content WHERE chapterId = :chapterId AND contentType = :contentType)")
    suspend fun isContentDownloaded(chapterId: String, contentType: String): Boolean
    
    @Query("SELECT SUM(sizeBytes) FROM downloaded_content")
    suspend fun getTotalDownloadedSize(): Long?
    
    @Query("SELECT COUNT(*) FROM downloaded_content")
    suspend fun getDownloadedContentCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedContent(content: DownloadedContentEntity)
    
    @Update
    suspend fun updateDownloadedContent(content: DownloadedContentEntity)
    
    @Query("UPDATE downloaded_content SET lastAccessedAt = :accessTime, accessCount = accessCount + 1 WHERE id = :contentId")
    suspend fun updateAccessInfo(contentId: String, accessTime: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteDownloadedContent(content: DownloadedContentEntity)
    
    @Query("DELETE FROM downloaded_content WHERE id = :contentId")
    suspend fun deleteDownloadedContentById(contentId: String)
    
    @Query("DELETE FROM downloaded_content WHERE chapterId = :chapterId")
    suspend fun deleteDownloadedContentByChapter(chapterId: String)
    
    @Query("DELETE FROM downloaded_content WHERE bookId = :bookId")
    suspend fun deleteDownloadedContentByBook(bookId: String)
    
    @Query("DELETE FROM downloaded_content")
    suspend fun clearAllDownloadedContent()
    
    // Get oldest accessed content for cleanup
    @Query("SELECT * FROM downloaded_content ORDER BY lastAccessedAt ASC LIMIT :limit")
    suspend fun getOldestAccessedContent(limit: Int): List<DownloadedContentEntity>
}

/**
 * DAO for download queue operations.
 */
@Dao
interface DownloadQueueDao {
    
    @Query("SELECT * FROM download_queue ORDER BY startedAt ASC")
    fun getAllDownloads(): Flow<List<DownloadQueueEntity>>
    
    @Query("SELECT * FROM download_queue WHERE status = :status ORDER BY startedAt ASC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadQueueEntity>>
    
    @Query("SELECT * FROM download_queue WHERE status IN ('PENDING', 'DOWNLOADING', 'PAUSED') ORDER BY startedAt ASC")
    fun getActiveDownloads(): Flow<List<DownloadQueueEntity>>
    
    @Query("SELECT * FROM download_queue WHERE id = :downloadId")
    suspend fun getDownloadById(downloadId: String): DownloadQueueEntity?
    
    @Query("SELECT * FROM download_queue WHERE status = 'PENDING' ORDER BY startedAt ASC LIMIT 1")
    suspend fun getNextPendingDownload(): DownloadQueueEntity?
    
    @Query("SELECT COUNT(*) FROM download_queue WHERE status = 'DOWNLOADING'")
    suspend fun getActiveDownloadCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadQueueEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(downloads: List<DownloadQueueEntity>)
    
    @Update
    suspend fun updateDownload(download: DownloadQueueEntity)
    
    @Query("UPDATE download_queue SET status = :status, progressPercent = :progress, downloadedBytes = :downloadedBytes WHERE id = :downloadId")
    suspend fun updateDownloadProgress(downloadId: String, status: String, progress: Int, downloadedBytes: Long)
    
    @Query("UPDATE download_queue SET status = 'COMPLETED', completedAt = :completedAt, localFilePath = :localPath, progressPercent = 100 WHERE id = :downloadId")
    suspend fun markAsCompleted(downloadId: String, completedAt: Long = System.currentTimeMillis(), localPath: String)
    
    @Query("UPDATE download_queue SET status = 'FAILED', errorMessage = :errorMessage WHERE id = :downloadId")
    suspend fun markAsFailed(downloadId: String, errorMessage: String)
    
    @Query("UPDATE download_queue SET status = 'PAUSED' WHERE id = :downloadId")
    suspend fun pauseDownload(downloadId: String)
    
    @Query("UPDATE download_queue SET status = 'PENDING' WHERE id = :downloadId")
    suspend fun resumeDownload(downloadId: String)
    
    @Query("UPDATE download_queue SET status = 'CANCELLED' WHERE id = :downloadId")
    suspend fun cancelDownload(downloadId: String)
    
    @Delete
    suspend fun deleteDownload(download: DownloadQueueEntity)
    
    @Query("DELETE FROM download_queue WHERE id = :downloadId")
    suspend fun deleteDownloadById(downloadId: String)
    
    @Query("DELETE FROM download_queue WHERE status = 'COMPLETED'")
    suspend fun clearCompletedDownloads()
    
    @Query("DELETE FROM download_queue WHERE status = 'FAILED'")
    suspend fun clearFailedDownloads()
    
    @Query("DELETE FROM download_queue")
    suspend fun clearAllDownloads()
}
