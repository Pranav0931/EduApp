package com.hdaf.eduapp.domain.usecase.content

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting books.
 */
class GetBooksUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(classId: String): Flow<Resource<List<Book>>> {
        return contentRepository.getBooksByClass(classId)
    }
}
