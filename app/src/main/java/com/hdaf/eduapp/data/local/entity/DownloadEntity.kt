package com.hdaf.eduapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for downloaded content metadata.
 */
@Entity(
    tableName = "downloaded_content",
    indices = [
        Index(value = ["chapterId"]),
        Index(value = ["bookId"]),
        Index(value = ["contentType"]),
        Index(value = ["downloadedAt"])
    ]
)
data class DownloadedContentEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val chapterTitle: String,
    val bookId: String,
    val bookTitle: String,
    val contentType: String,
    val sizeBytes: Long,
    val localFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0
)

/**
 * Room entity for download queue.
 */
@Entity(
    tableName = "download_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["startedAt"])
    ]
)
data class DownloadQueueEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val bookId: String,
    val title: String,
    val titleHindi: String,
    val contentType: String,
    val sizeBytes: Long,
    val url: String,
    val thumbnailUrl: String? = null,
    val status: String = "PENDING",
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0,
    val errorMessage: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val localFilePath: String? = null
)
