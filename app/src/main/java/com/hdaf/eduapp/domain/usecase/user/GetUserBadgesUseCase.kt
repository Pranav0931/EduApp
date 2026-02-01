package com.hdaf.eduapp.domain.usecase.user

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user badges.
 */
class GetUserBadgesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<List<Badge>>> {
        return userRepository.getUserBadges()
    }
}
