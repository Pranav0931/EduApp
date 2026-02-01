package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.security.SecurePreferences
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.remote.api.GamificationApi
import com.hdaf.eduapp.data.mapper.BadgeMapper
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.User
import com.hdaf.eduapp.domain.model.UserProfile
import com.hdaf.eduapp.domain.model.UserStats
import com.hdaf.eduapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val userProgressDao: UserProgressDao,
    private val badgeDao: BadgeDao,
    private val gamificationApi: GamificationApi,
    private val badgeMapper: BadgeMapper
) : UserRepository {

    override fun getUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = securePreferences.getUserId()
            
            if (userId.isNullOrEmpty()) {
                emit(Resource.Error("User not logged in"))
                return@flow
            }
            
            // Try to get from local first
            val progress = userProgressDao.getProgressByUser(userId)
            val classLevel = securePreferences.getInt(KEY_CLASS_LEVEL, 1)
            val medium = securePreferences.getString(KEY_MEDIUM, "hi") ?: "hi"
            
            val profile = UserProfile(
                id = userId,
                name = securePreferences.getString(KEY_USER_NAME, "User") ?: "User",
                phone = securePreferences.getString(KEY_PHONE, "") ?: "",
                avatarUrl = null,
                classLevel = classLevel,
                medium = medium,
                xp = progress?.totalXp ?: 0,
                level = progress?.level ?: 1,
                xpToNextLevel = calculateXpToNextLevel(progress?.level ?: 1),
                streakDays = progress?.currentStreak ?: 0,
                leaderboardRank = 0 // Would need API call
            )
            
            emit(Resource.Success(profile))
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load profile"))
        }
    }

    override suspend fun updateProfile(user: User): Resource<Unit> {
        return try {
            securePreferences.putString(KEY_USER_NAME, user.name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile")
        }
    }

    override fun getUserStats(): Flow<Resource<UserStats>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = securePreferences.getUserId()
            
            if (userId.isNullOrEmpty()) {
                emit(Resource.Error("User not logged in"))
                return@flow
            }
            
            val progress = userProgressDao.getProgressByUser(userId)
            
            val stats = UserStats(
                currentStreak = progress?.currentStreak ?: 0,
                longestStreak = progress?.maxStreak ?: 0,
                booksCompleted = progress?.booksCompleted ?: 0,
                chaptersCompleted = progress?.chaptersCompleted ?: 0,
                quizzesCompleted = progress?.quizzesCompleted ?: 0,
                totalMinutesLearned = progress?.totalStudyTimeMinutes ?: 0,
                leaderboardRank = progress?.globalRank ?: 0,
                totalXpEarned = progress?.totalXp ?: 0,
                averageQuizScore = 0 // TODO: Calculate from quiz attempts if needed
            )
            
            emit(Resource.Success(stats))
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load stats"))
        }
    }

    override fun getUserBadges(): Flow<Resource<List<Badge>>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = securePreferences.getUserId()
            
            if (userId.isNullOrEmpty()) {
                emit(Resource.Error("User not logged in"))
                return@flow
            }
            
            // Get from local DB
            badgeDao.getUnlockedBadges().collect { badgeEntities ->
                val badges = badgeEntities.map { badgeMapper.toDomain(it) }
                emit(Resource.Success(badges))
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to load badges"))
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val userId = securePreferences.getUserId()
            
            if (!userId.isNullOrEmpty()) {
                userProgressDao.deleteProgress(userId)
                badgeDao.deleteAllBadges()
            }
            
            securePreferences.clearAll()
            Resource.Success(Unit)
            
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete account")
        }
    }

    override suspend fun updateUserClass(classLevel: Int): Resource<Unit> {
        return try {
            securePreferences.putInt(KEY_CLASS_LEVEL, classLevel)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update class level")
        }
    }

    private fun calculateXpToNextLevel(currentLevel: Int): Int {
        return currentLevel * 100 + 100
    }
    
    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_CLASS_LEVEL = "class_level"
        private const val KEY_MEDIUM = "medium"
    }
}
