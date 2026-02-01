package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.local.entity.BookEntity
import com.hdaf.eduapp.data.local.entity.ChapterEntity
import com.hdaf.eduapp.data.remote.dto.BookDto
import com.hdaf.eduapp.data.remote.dto.ChapterDto
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.model.ContentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mappers for Book and Chapter conversions between layers.
 */

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

private fun parseDate(dateString: String?): Date {
    return dateString?.let {
        try {
            dateFormat.parse(it)
        } catch (e: Exception) {
            Date()
        }
    } ?: Date()
}

// ==================== Book Mappers ====================

fun BookDto.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        classId = classId,
        name = name,
        title = title,
        subject = subject,
        description = description,
        iconUrl = iconUrl,
        coverUrl = coverUrl,
        orderIndex = orderIndex,
        totalChapters = totalChapters,
        isActive = isActive,
        isDownloaded = false,
        downloadProgress = 0f,
        isSynced = true,
        createdAt = parseDate(createdAt),
        updatedAt = parseDate(updatedAt)
    )
}

fun BookDto.toDomain(): Book {
    return Book(
        id = id,
        classId = classId,
        name = name,
        title = title,
        subject = subject,
        description = description,
        iconUrl = iconUrl,
        coverUrl = coverUrl,
        orderIndex = orderIndex,
        totalChapters = totalChapters,
        isActive = isActive,
        isDownloaded = false,
        downloadProgress = 0f
    )
}

fun BookEntity.toDomain(): Book {
    return Book(
        id = id,
        classId = classId,
        name = name,
        title = title,
        subject = subject,
        description = description,
        iconUrl = iconUrl,
        coverUrl = coverUrl,
        orderIndex = orderIndex,
        totalChapters = totalChapters,
        isActive = isActive,
        isDownloaded = isDownloaded,
        downloadProgress = downloadProgress
    )
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        classId = classId,
        name = name,
        title = title,
        subject = subject,
        description = description,
        iconUrl = iconUrl,
        coverUrl = coverUrl,
        orderIndex = orderIndex,
        totalChapters = totalChapters,
        isActive = isActive,
        isDownloaded = isDownloaded,
        downloadProgress = downloadProgress,
        isSynced = true,
        createdAt = Date(),
        updatedAt = Date()
    )
}

// ==================== Chapter Mappers ====================

fun ChapterDto.toEntity(): ChapterEntity {
    return ChapterEntity(
        id = id,
        bookId = bookId,
        title = title,
        description = description,
        content = content,
        contentText = contentText,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        isActive = isActive,
        isSynced = true,
        isDownloaded = false,
        downloadedFilePath = null,
        localAudioPath = null,
        localVideoPath = null,
        readProgress = 0f,
        lastReadAt = 0L,
        createdAt = parseDate(createdAt),
        updatedAt = parseDate(updatedAt)
    )
}

fun ChapterDto.toDomain(): Chapter {
    return Chapter(
        id = id,
        bookId = bookId,
        title = title,
        description = description,
        contentText = contentText,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        isActive = isActive,
        isDownloaded = false,
        localAudioPath = null,
        localVideoPath = null
    )
}

fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        id = id,
        bookId = bookId,
        title = title,
        description = description,
        contentText = contentText,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        isActive = isActive,
        isDownloaded = isDownloaded,
        localAudioPath = localAudioPath,
        localVideoPath = localVideoPath
    )
}

fun Chapter.toEntity(): ChapterEntity {
    return ChapterEntity(
        id = id,
        bookId = bookId,
        title = title,
        description = description,
        contentText = contentText,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        isActive = isActive,
        isSynced = true,
        isDownloaded = isDownloaded,
        localAudioPath = localAudioPath,
        localVideoPath = localVideoPath,
        createdAt = Date(),
        updatedAt = Date()
    )
}

// Overload with explicit bookId parameter
fun ChapterDto.toEntity(overrideBookId: String): ChapterEntity {
    return ChapterEntity(
        id = id,
        bookId = overrideBookId,
        title = title,
        description = description,
        content = content,
        contentText = contentText,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationMinutes = durationMinutes,
        orderIndex = orderIndex,
        isActive = isActive,
        isSynced = true,
        isDownloaded = false,
        downloadedFilePath = null,
        localAudioPath = null,
        localVideoPath = null,
        readProgress = 0f,
        lastReadAt = 0L,
        createdAt = parseDate(createdAt),
        updatedAt = parseDate(updatedAt)
    )
}

// ==================== List Extensions ====================

@JvmName("bookDtoListToEntity")
fun List<BookDto>.toEntityList(): List<BookEntity> = map { it.toEntity() }
@JvmName("bookDtoListToDomain")
fun List<BookDto>.toDomainList(): List<Book> = map { it.toDomain() }
@JvmName("bookEntityListToDomain")
fun List<BookEntity>.toDomainList(): List<Book> = map { it.toDomain() }

@JvmName("chapterDtoListToEntity")
fun List<ChapterDto>.toEntityList(): List<ChapterEntity> = map { it.toEntity() }
@JvmName("chapterDtoListToEntityWithBookId")
fun List<ChapterDto>.toEntityList(bookId: String): List<ChapterEntity> = map { it.toEntity(bookId) }
@JvmName("chapterDtoListToDomain")
fun List<ChapterDto>.toDomainList(): List<Chapter> = map { it.toDomain() }
@JvmName("chapterEntityListToDomain")
fun List<ChapterEntity>.toDomainList(): List<Chapter> = map { it.toDomain() }
