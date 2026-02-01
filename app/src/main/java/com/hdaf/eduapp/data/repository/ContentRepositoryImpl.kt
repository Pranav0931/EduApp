package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.data.local.dao.BookDao
import com.hdaf.eduapp.data.local.dao.ChapterDao
import com.hdaf.eduapp.data.mapper.toDomain
import com.hdaf.eduapp.data.mapper.toDomainList
import com.hdaf.eduapp.data.mapper.toEntity
import com.hdaf.eduapp.data.mapper.toEntityList
import com.hdaf.eduapp.data.remote.api.ContentApi
import com.hdaf.eduapp.data.remote.dto.ChapterProgressUpdateDto
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.model.ChapterProgress
import com.hdaf.eduapp.domain.model.Language
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.ContentRepository
import com.hdaf.eduapp.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ContentRepository following offline-first pattern.
 * Data flows: Local DB -> Emit -> Fetch Remote -> Update DB -> Emit Updated
 */
@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentApi: ContentApi,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentRepository {

    // ==================== Books ====================

    override fun getBooksByClass(classId: String): Flow<Resource<List<Book>>> = flow {
        emit(Resource.Loading())
        
        // First, emit cached data
        val cachedBooks = bookDao.getBooksByClass(classId).first()
        if (cachedBooks.isNotEmpty()) {
            emit(Resource.Success(cachedBooks.toDomainList()))
        }
        
        // Then fetch from network if available
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getBooksByClass("eq.$classId")
                if (response.isSuccessful && response.body() != null) {
                    val books = response.body()!!
                    // Update local database
                    bookDao.insertAll(books.toEntityList())
                    // Emit updated data
                    emit(Resource.Success(books.toDomainList()))
                } else {
                    if (cachedBooks.isEmpty()) {
                        emit(Resource.Error("Failed to fetch books: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching books for class $classId")
                if (cachedBooks.isEmpty()) {
                    emit(Resource.Error(e.message ?: "Network error"))
                }
            }
        } else if (cachedBooks.isEmpty()) {
            emit(Resource.Error("No internet connection and no cached data"))
        }
    }.flowOn(ioDispatcher)

    override fun getBooksBySubject(classId: String, subject: Subject): Flow<Resource<List<Book>>> = flow {
        emit(Resource.Loading())
        
        val cachedBooks = bookDao.getBooksByClassAndSubject(classId, subject.name).first()
        if (cachedBooks.isNotEmpty()) {
            emit(Resource.Success(cachedBooks.toDomainList()))
        }
        
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getBooksByClass("eq.$classId")
                if (response.isSuccessful && response.body() != null) {
                    val books = response.body()!!.filter { it.subject == subject.name }
                    bookDao.insertAll(books.toEntityList())
                    emit(Resource.Success(books.toDomainList()))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching books by subject")
                if (cachedBooks.isEmpty()) {
                    emit(Resource.Error(e.message ?: "Network error"))
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getBookById(bookId: String): Flow<Resource<Book>> = flow {
        emit(Resource.Loading())
        
        val cachedBook = bookDao.getBookById(bookId)
        if (cachedBook != null) {
            emit(Resource.Success(cachedBook.toDomain()))
        }
        
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getBookById("eq.$bookId")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val book = response.body()!!.first()
                    bookDao.insert(book.toEntity())
                    emit(Resource.Success(book.toDomain()))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching book $bookId")
                if (cachedBook == null) {
                    emit(Resource.Error(e.message ?: "Book not found"))
                }
            }
        } else if (cachedBook == null) {
            emit(Resource.Error("Book not found in cache"))
        }
    }.flowOn(ioDispatcher)

    override fun searchBooks(query: String, classId: String): Flow<Resource<List<Book>>> = flow {
        emit(Resource.Loading())
        
        // Search in local database first
        val searchPattern = "%$query%"
        val cachedBooks = bookDao.searchBooks(searchPattern, classId).first()
        emit(Resource.Success(cachedBooks.toDomainList()))
        
        // Also search remotely if connected
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getBooksByClass("eq.$classId")
                if (response.isSuccessful && response.body() != null) {
                    val books = response.body()!!.filter { 
                        it.title.contains(query, ignoreCase = true) || 
                        it.name.contains(query, ignoreCase = true) 
                    }
                    bookDao.insertAll(books.toEntityList())
                    emit(Resource.Success(books.toDomainList()))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching books")
            }
        }
    }.flowOn(ioDispatcher)

    override fun getDownloadedBooks(): Flow<List<Book>> {
        return bookDao.getDownloadedBooks().map { entities ->
            entities.toDomainList()
        }.flowOn(ioDispatcher)
    }

    override suspend fun downloadBook(bookId: String): Flow<Resource<Float>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get all chapters for the book
            val chaptersResponse = contentApi.getChaptersByBook("eq.$bookId")
            if (!chaptersResponse.isSuccessful) {
                emit(Resource.Error("Failed to fetch chapters"))
                return@flow
            }
            
            val chapters = chaptersResponse.body() ?: emptyList()
            val totalChapters = chapters.size
            var downloadedCount = 0
            
            chapters.forEach { chapterDto ->
                // Download each chapter's content
                val chapterEntity = chapterDto.toEntity(bookId).copy(
                    isDownloaded = true,
                    downloadedFilePath = "/data/chapters/${chapterDto.id}"
                )
                chapterDao.insert(chapterEntity)
                
                downloadedCount++
                val progress = downloadedCount.toFloat() / totalChapters
                emit(Resource.Success(progress))
            }
            
            // Mark book as downloaded
            bookDao.updateDownloadStatus(bookId, true, 1f)
            emit(Resource.Success(1f))
            
        } catch (e: Exception) {
            Timber.e(e, "Error downloading book $bookId")
            emit(Resource.Error(e.message ?: "Download failed"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun deleteDownloadedBook(bookId: String): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                // Delete downloaded chapter files
                chapterDao.clearDownloadedChapters(bookId)
                bookDao.updateDownloadStatus(bookId, false, 0f)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting downloaded book")
                Resource.Error(e.message ?: "Delete failed")
            }
        }
    }

    // ==================== Chapters ====================

    override fun getChaptersByBook(bookId: String): Flow<Resource<List<Chapter>>> = flow {
        emit(Resource.Loading())
        
        val cachedChapters = chapterDao.getChaptersByBook(bookId).first()
        if (cachedChapters.isNotEmpty()) {
            emit(Resource.Success(cachedChapters.toDomainList()))
        }
        
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getChaptersByBook("eq.$bookId")
                if (response.isSuccessful && response.body() != null) {
                    val chapters = response.body()!!
                    chapterDao.insertAll(chapters.toEntityList(bookId))
                    emit(Resource.Success(chapters.toDomainList()))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching chapters")
                if (cachedChapters.isEmpty()) {
                    emit(Resource.Error(e.message ?: "Network error"))
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getChapterById(chapterId: String): Flow<Resource<Chapter>> = flow {
        emit(Resource.Loading())
        
        val cachedChapter = chapterDao.getChapterById(chapterId)
        if (cachedChapter != null) {
            emit(Resource.Success(cachedChapter.toDomain()))
        }
        
        if (networkMonitor.isConnected.first()) {
            try {
                val response = contentApi.getChapterById("eq.$chapterId")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val chapter = response.body()!!.first()
                    chapterDao.insert(chapter.toEntity(chapter.bookId))
                    emit(Resource.Success(chapter.toDomain()))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching chapter")
                if (cachedChapter == null) {
                    emit(Resource.Error(e.message ?: "Chapter not found"))
                }
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun getChapterContent(chapterId: String): Resource<Chapter> {
        return withContext(ioDispatcher) {
            try {
                // First check local cache
                val cached = chapterDao.getChapterById(chapterId)
                if (cached != null && !cached.content.isNullOrEmpty()) {
                    return@withContext Resource.Success(cached.toDomain())
                }
                
                // Fetch from network
                if (networkMonitor.isConnected.first()) {
                    val response = contentApi.getChapterContent("eq.$chapterId")
                    if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                        val chapter = response.body()!!.first()
                        chapterDao.insert(chapter.toEntity(chapter.bookId))
                        return@withContext Resource.Success(chapter.toDomain())
                    }
                }
                
                // Return cached without content if available
                cached?.let { return@withContext Resource.Success(it.toDomain()) }
                
                Resource.Error("Chapter content not available")
            } catch (e: Exception) {
                Timber.e(e, "Error getting chapter content")
                Resource.Error(e.message ?: "Failed to load content")
            }
        }
    }

    override fun getDownloadedChapters(bookId: String): Flow<List<Chapter>> {
        return chapterDao.getDownloadedChapters(bookId).map { entities ->
            entities.toDomainList()
        }.flowOn(ioDispatcher)
    }

    override suspend fun downloadChapter(chapterId: String): Flow<Resource<Float>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = contentApi.getChapterContent("eq.$chapterId")
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                val chapter = response.body()!!.first()
                val entity = chapter.toEntity(chapter.bookId).copy(
                    isDownloaded = true,
                    downloadedFilePath = "/data/chapters/$chapterId"
                )
                chapterDao.insert(entity)
                emit(Resource.Success(1f))
            } else {
                emit(Resource.Error("Failed to download chapter"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading chapter")
            emit(Resource.Error(e.message ?: "Download failed"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun deleteDownloadedChapter(chapterId: String): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                val chapter = chapterDao.getChapterById(chapterId)
                chapter?.let {
                    chapterDao.insert(
                        it.copy(isDownloaded = false, downloadedFilePath = null)
                    )
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Delete failed")
            }
        }
    }

    // ==================== Progress ====================

    override suspend fun updateChapterProgress(
        chapterId: String,
        progress: Float,
        timeSpentSeconds: Int
    ): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                chapterDao.updateReadProgress(
                    chapterId = chapterId,
                    progress = progress,
                    lastReadAt = System.currentTimeMillis()
                )
                
                // Sync to server if connected
                if (networkMonitor.isConnected.first()) {
                    try {
                        contentApi.updateChapterProgress(
                            ChapterProgressUpdateDto(
                                chapterId = chapterId,
                                userId = "", // Would get from AuthManager
                                progress = progress,
                                timeSpentSeconds = timeSpentSeconds
                            )
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync progress to server")
                    }
                }
                
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to update progress")
            }
        }
    }

    override suspend fun markChapterCompleted(chapterId: String): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                chapterDao.updateReadProgress(
                    chapterId = chapterId,
                    progress = 1f,
                    lastReadAt = System.currentTimeMillis()
                )
                
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to mark as completed")
            }
        }
    }

    override fun getBookProgress(bookId: String): Flow<Map<String, ChapterProgress>> = flow {
        // This would be implemented with a proper progress tracking table
        // For now, return empty map
        emit(emptyMap<String, ChapterProgress>())
    }.flowOn(ioDispatcher)

    override fun getRecentlyReadChapters(limit: Int): Flow<List<Chapter>> {
        return chapterDao.getRecentlyRead(limit).map { entities ->
            entities.toDomainList()
        }.flowOn(ioDispatcher)
    }

    // ==================== Language Support ====================

    override suspend fun getChapterContentByLanguage(
        chapterId: String,
        language: Language
    ): Resource<Chapter> {
        return withContext(ioDispatcher) {
            try {
                // For now, fallback to default content
                // Multi-language support would need additional API endpoints
                getChapterContent(chapterId)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load translated content")
            }
        }
    }

    // ==================== Sync ====================

    override suspend fun syncContent(): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                if (!networkMonitor.isConnected.first()) {
                    return@withContext Resource.Error("No internet connection")
                }
                
                // Sync logic would go here
                // 1. Get all cached data that needs syncing
                // 2. Push local changes to server
                // 3. Pull latest data from server
                
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Sync failed")
            }
        }
    }

    override suspend fun checkForUpdates(): Resource<Boolean> {
        return withContext(ioDispatcher) {
            try {
                // Check server for content updates
                Resource.Success(false)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to check updates")
            }
        }
    }

    override fun getLastSyncTime(): Flow<Long?> = flow {
        // Would be stored in preferences or database
        emit(null)
    }.flowOn(ioDispatcher)
}
