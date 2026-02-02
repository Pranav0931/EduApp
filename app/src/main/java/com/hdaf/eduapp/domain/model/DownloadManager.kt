package com.hdaf.eduapp.domain.model

/**
 * Downloadable content item for offline access.
 * Uses ContentType from Chapter.kt for content classification.
 */
data class DownloadableContent(
    val id: String,
    val chapterId: String,
    val bookId: String,
    val title: String,
    val titleHindi: String,
    val contentType: ContentType,
    val sizeBytes: Long,
    val url: String,
    val thumbnailUrl: String? = null
)

/**
 * Represents a download task.
 */
data class DownloadTask(
    val id: String,
    val content: DownloadableContent,
    val status: DownloadStatus,
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val errorMessage: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val localFilePath: String? = null
)

/**
 * Download status.
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

// Note: ContentType is already defined in Chapter.kt
// We reuse that definition for consistency.

/**
 * Offline content storage info.
 */
data class OfflineStorage(
    val totalSpaceBytes: Long,
    val usedSpaceBytes: Long,
    val availableSpaceBytes: Long,
    val downloadedItems: Int,
    val downloadedContentList: List<DownloadedContent>
)

/**
 * Downloaded content available offline.
 */
data class DownloadedContent(
    val id: String,
    val chapterId: String,
    val chapterTitle: String,
    val bookId: String,
    val bookTitle: String,
    val contentType: ContentType,
    val sizeBytes: Long,
    val localFilePath: String,
    val downloadedAt: Long,
    val lastAccessedAt: Long,
    val accessCount: Int = 0
)
