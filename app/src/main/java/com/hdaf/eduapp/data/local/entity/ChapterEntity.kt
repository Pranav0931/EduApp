package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity for Chapter.
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["is_synced"]),
        Index(value = ["is_downloaded"])
    ]
)
data class ChapterEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "content")
    val content: String? = null,
    
    @ColumnInfo(name = "content_text")
    val contentText: String? = null,
    
    @ColumnInfo(name = "audio_url")
    val audioUrl: String? = null,
    
    @ColumnInfo(name = "video_url")
    val videoUrl: String? = null,
    
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 0,
    
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,
    
    @ColumnInfo(name = "downloaded_file_path")
    val downloadedFilePath: String? = null,
    
    @ColumnInfo(name = "local_audio_path")
    val localAudioPath: String? = null,
    
    @ColumnInfo(name = "local_video_path")
    val localVideoPath: String? = null,
    
    @ColumnInfo(name = "read_progress")
    val readProgress: Float = 0f,
    
    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long = 0L,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)
