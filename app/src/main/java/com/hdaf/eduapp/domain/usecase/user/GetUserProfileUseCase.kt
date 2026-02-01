package com.hdaf.eduapp.domain.usecase.user

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.UserProfile
import com.hdaf.eduapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user profile.
 */
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<UserProfile>> {
        return userRepository.getUserProfile()
    }
}
