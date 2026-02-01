package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.User
import com.hdaf.eduapp.domain.model.UserProfile
import com.hdaf.eduapp.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations.
 */
interface UserRepository {

    /**
     * Get user profile.
     */
    fun getUserProfile(): Flow<Resource<UserProfile>>

    /**
     * Update user profile.
     */
    suspend fun updateProfile(user: User): Resource<Unit>

    /**
     * Get user stats.
     */
    fun getUserStats(): Flow<Resource<UserStats>>

    /**
     * Get user badges.
     */
    fun getUserBadges(): Flow<Resource<List<Badge>>>

    /**
     * Delete user account.
     */
    suspend fun deleteAccount(): Resource<Unit>

    /**
     * Update user class level.
     */
    suspend fun updateUserClass(classLevel: Int): Resource<Unit>
}
