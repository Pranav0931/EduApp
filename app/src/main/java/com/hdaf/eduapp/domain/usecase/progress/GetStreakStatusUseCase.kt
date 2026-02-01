package com.hdaf.eduapp.domain.usecase.progress

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.repository.ProgressRepository
import com.hdaf.eduapp.domain.repository.StreakStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting streak status.
 */
class GetStreakStatusUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<StreakStatus>> {
        return progressRepository.getStreakInfo()
    }
}
