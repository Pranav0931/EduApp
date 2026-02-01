package com.hdaf.eduapp.domain.usecase.content

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting books by class.
 */
class GetBooksByClassUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(classId: String): Flow<Resource<List<Book>>> {
        return contentRepository.getBooksByClass(classId)
    }
}

/**
 * Use case for getting books by subject.
 */
class GetBooksBySubjectUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(classId: String, subject: Subject): Flow<Resource<List<Book>>> {
        return contentRepository.getBooksBySubject(classId, subject)
    }
}

/**
 * Use case for getting a specific book.
 */
class GetBookByIdUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(bookId: String): Flow<Resource<Book>> {
        return contentRepository.getBookById(bookId)
    }
}

/**
 * Use case for searching books.
 */
class SearchBooksUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(query: String, classId: String): Flow<Resource<List<Book>>> {
        return contentRepository.searchBooks(query, classId)
    }
}

/**
 * Use case for getting chapters of a book.
 */
class GetChaptersByBookUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(bookId: String): Flow<Resource<List<Chapter>>> {
        return contentRepository.getChaptersByBook(bookId)
    }
}

/**
 * Use case for getting chapter content.
 */
class GetChapterContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(chapterId: String): Resource<Chapter> {
        return contentRepository.getChapterContent(chapterId)
    }
}

/**
 * Use case for updating reading progress.
 */
class UpdateChapterProgressUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(
        chapterId: String,
        progress: Float,
        timeSpentSeconds: Int
    ): Resource<Unit> {
        return contentRepository.updateChapterProgress(chapterId, progress, timeSpentSeconds)
    }
}

/**
 * Use case for marking chapter as completed.
 */
class MarkChapterCompletedUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(chapterId: String): Resource<Unit> {
        return contentRepository.markChapterCompleted(chapterId)
    }
}

/**
 * Use case for downloading a book for offline use.
 */
class DownloadBookUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(bookId: String): Flow<Resource<Float>> {
        return contentRepository.downloadBook(bookId)
    }
}

/**
 * Use case for downloading a chapter.
 */
class DownloadChapterUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(chapterId: String): Flow<Resource<Float>> {
        return contentRepository.downloadChapter(chapterId)
    }
}

/**
 * Use case for getting recently read chapters.
 */
class GetRecentlyReadChaptersUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<Chapter>> {
        return contentRepository.getRecentlyReadChapters(limit)
    }
}

/**
 * Use case for syncing content.
 */
class SyncContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return contentRepository.syncContent()
    }
}

/**
 * Use case for getting downloaded books.
 */
class GetDownloadedBooksUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(): Flow<List<Book>> {
        return contentRepository.getDownloadedBooks()
    }
}
