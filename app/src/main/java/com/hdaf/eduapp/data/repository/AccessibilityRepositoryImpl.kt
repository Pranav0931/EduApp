package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.AccessibilityProfileDao
import com.hdaf.eduapp.data.mapper.toDomain
import com.hdaf.eduapp.data.mapper.toEntity
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.ContrastLevel
import com.hdaf.eduapp.domain.repository.AccessibilityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AccessibilityRepository.
 * Manages user accessibility profiles with offline-first approach.
 */
@Singleton
class AccessibilityRepositoryImpl @Inject constructor(
    private val accessibilityProfileDao: AccessibilityProfileDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AccessibilityRepository {

    override suspend fun getProfile(userId: String): Resource<AccessibilityProfile> = withContext(ioDispatcher) {
        try {
            val entity = accessibilityProfileDao.getProfile(userId)
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                // Return default profile if none exists
                val defaultProfile = AccessibilityProfile(userId = userId)
                accessibilityProfileDao.insertProfile(defaultProfile.toEntity())
                Resource.Success(defaultProfile)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting accessibility profile for user: $userId")
            Resource.Error("Failed to get accessibility profile: ${e.message}")
        }
    }

    override fun observeProfile(userId: String): Flow<AccessibilityProfile?> {
        return accessibilityProfileDao.observeProfile(userId)
            .map { entity -> entity?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override suspend fun saveProfile(profile: AccessibilityProfile): Resource<Unit> = withContext(ioDispatcher) {
        try {
            val updatedProfile = profile.copy(updatedAt = System.currentTimeMillis())
            accessibilityProfileDao.insertProfile(updatedProfile.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error saving accessibility profile")
            Resource.Error("Failed to save accessibility profile: ${e.message}")
        }
    }

    override suspend fun updateAccessibilityMode(
        userId: String,
        mode: AccessibilityModeType
    ): Resource<Unit> = withContext(ioDispatcher) {
        try {
            // First ensure profile exists
            ensureProfileExists(userId)
            accessibilityProfileDao.updateAccessibilityMode(userId, mode.name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating accessibility mode")
            Resource.Error("Failed to update accessibility mode: ${e.message}")
        }
    }

    override suspend fun updateFontScale(userId: String, scale: Float): Resource<Unit> = withContext(ioDispatcher) {
        try {
            ensureProfileExists(userId)
            accessibilityProfileDao.updateFontScale(userId, scale.coerceIn(0.5f, 3.0f))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating font scale")
            Resource.Error("Failed to update font scale: ${e.message}")
        }
    }

    override suspend fun updateContrastLevel(
        userId: String,
        level: ContrastLevel
    ): Resource<Unit> = withContext(ioDispatcher) {
        try {
            ensureProfileExists(userId)
            accessibilityProfileDao.updateContrastLevel(userId, level.name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating contrast level")
            Resource.Error("Failed to update contrast level: ${e.message}")
        }
    }

    override suspend fun deleteProfile(userId: String): Resource<Unit> = withContext(ioDispatcher) {
        try {
            accessibilityProfileDao.deleteProfile(userId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting accessibility profile")
            Resource.Error("Failed to delete accessibility profile: ${e.message}")
        }
    }

    override fun getRecommendedProfile(userId: String, mode: AccessibilityModeType): AccessibilityProfile {
        return when (mode) {
            AccessibilityModeType.BLIND -> AccessibilityProfile.forBlindUser(userId)
            AccessibilityModeType.DEAF -> AccessibilityProfile.forDeafUser(userId)
            AccessibilityModeType.LOW_VISION -> AccessibilityProfile.forLowVisionUser(userId)
            AccessibilityModeType.SLOW_LEARNER -> AccessibilityProfile.forSlowLearner(userId)
            AccessibilityModeType.NORMAL -> AccessibilityProfile(userId = userId)
        }
    }

    private suspend fun ensureProfileExists(userId: String) {
        val existing = accessibilityProfileDao.getProfile(userId)
        if (existing == null) {
            val defaultProfile = AccessibilityProfile(userId = userId)
            accessibilityProfileDao.insertProfile(defaultProfile.toEntity())
        }
    }
}
