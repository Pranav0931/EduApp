package com.hdaf.eduapp.domain.usecase.progress

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * Use case for updating chapter progress.
 */
class UpdateChapterProgressUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(chapterId: String, progress: Float, timeSpentSeconds: Int): Resource<Unit> {
        return contentRepository.updateChapterProgress(chapterId, progress, timeSpentSeconds)
    }
}
