package com.hdaf.eduapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity for Book/Subject.
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["class_id"]),
        Index(value = ["subject"]),
        Index(value = ["is_synced"])
    ]
)
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "class_id")
    val classId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "title")
    val title: String = "",
    
    @ColumnInfo(name = "subject")
    val subject: String = "",
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,
    
    @ColumnInfo(name = "cover_url")
    val coverUrl: String? = null,
    
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0,
    
    @ColumnInfo(name = "total_chapters")
    val totalChapters: Int = 0,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,
    
    @ColumnInfo(name = "download_progress")
    val downloadProgress: Float = 0f,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)
