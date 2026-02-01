package com.hdaf.eduapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs for Content API (Books, Chapters).
 */

data class BookDto(
    @SerializedName("id") val id: String,
    @SerializedName("class_id") val classId: String,
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String = "",
    @SerializedName("subject") val subject: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("total_chapters") val totalChapters: Int = 0,
    @SerializedName("order_index") val orderIndex: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ChapterDto(
    @SerializedName("id") val id: String,
    @SerializedName("book_id") val bookId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("content_text") val contentText: String? = null,
    @SerializedName("audio_url") val audioUrl: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int = 0,
    @SerializedName("order_index") val orderIndex: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ChapterProgressUpdateDto(
    @SerializedName("chapter_id") val chapterId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("progress") val progress: Float,
    @SerializedName("time_spent_seconds") val timeSpentSeconds: Int
)
