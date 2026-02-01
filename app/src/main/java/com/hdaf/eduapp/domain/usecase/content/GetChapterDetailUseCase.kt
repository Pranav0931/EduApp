package com.hdaf.eduapp.domain.usecase.content

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting chapter details for playback.
 */
class GetChapterDetailUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(chapterId: String): Flow<Resource<Chapter>> {
        return contentRepository.getChapterById(chapterId)
    }
}
