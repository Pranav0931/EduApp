package com.hdaf.eduapp.domain.usecase.user

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.UserStats
import com.hdaf.eduapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user statistics.
 */
class GetUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<UserStats>> {
        return userRepository.getUserStats()
    }
}
