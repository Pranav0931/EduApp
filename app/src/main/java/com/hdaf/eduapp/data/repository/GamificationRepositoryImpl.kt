package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.data.local.dao.BadgeDao
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.remote.api.GamificationApi
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.BadgeCategory
import com.hdaf.eduapp.domain.model.UserStats
import com.hdaf.eduapp.domain.repository.GamificationRepository
import com.hdaf.eduapp.domain.repository.LeaderboardEntryData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val badgeDao: BadgeDao,
    private val gamificationApi: GamificationApi
) : GamificationRepository {

    override fun getUserStats(): Flow<Resource<UserStats>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get from local cache first
            val progress = userProgressDao.getProgressByUser("current_user")
            if (progress != null) {
                emit(Resource.Success(
                    UserStats(
                        currentStreak = progress.currentStreak,
                        longestStreak = progress.maxStreak,
                        booksCompleted = progress.booksCompleted,
                        chaptersCompleted = progress.chaptersCompleted,
                        quizzesCompleted = progress.quizzesCompleted,
                        totalMinutesLearned = progress.totalStudyTimeMinutes,
                        leaderboardRank = progress.globalRank,
                        totalXpEarned = progress.totalXp,
                        averageQuizScore = 0
                    )
                ))
            }
            
            // Fetch from remote and update
            val response = gamificationApi.getUserStats()
            if (response.isSuccessful && response.body() != null) {
                val statsDto = response.body()!!
                val stats = UserStats(
                    currentStreak = statsDto.currentStreak,
                    longestStreak = statsDto.maxStreak,
                    booksCompleted = statsDto.booksCompleted,
                    chaptersCompleted = statsDto.chaptersCompleted,
                    quizzesCompleted = statsDto.quizzesCompleted,
                    totalMinutesLearned = statsDto.minutesLearned,
                    leaderboardRank = statsDto.leaderboardRank,
                    totalXpEarned = statsDto.totalXp,
                    averageQuizScore = 0
                )
                emit(Resource.Success(stats))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get user stats"))
        }
    }

    override suspend fun updateStats(
        minutesLearned: Int,
        chapterCompleted: Boolean,
        quizCompleted: Boolean
    ): Resource<UserStats> {
        return try {
            val response = gamificationApi.updateStats(
                mapOf(
                    "minutesLearned" to minutesLearned,
                    "chapterCompleted" to chapterCompleted,
                    "quizCompleted" to quizCompleted
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Resource.Success(
                    UserStats(
                        currentStreak = dto.currentStreak,
                        longestStreak = dto.maxStreak,
                        booksCompleted = dto.booksCompleted,
                        chaptersCompleted = dto.chaptersCompleted,
                        quizzesCompleted = dto.quizzesCompleted,
                        totalMinutesLearned = dto.minutesLearned,
                        leaderboardRank = dto.leaderboardRank,
                        totalXpEarned = dto.totalXp,
                        averageQuizScore = 0
                    )
                )
            } else {
                Resource.Error("Failed to update stats")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update stats")
        }
    }

    override fun getUserBadges(): Flow<Resource<List<Badge>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get from local cache
            badgeDao.getUserBadges().collect { badgeEntities ->
                val badges = badgeEntities.map { entity ->
                    Badge(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        iconUrl = entity.iconUrl,
                        isUnlocked = entity.isUnlocked,
                        unlockedAt = entity.unlockedAt,
                        category = BadgeCategory.valueOf(entity.category)
                    )
                }
                emit(Resource.Success(badges))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get badges"))
        }
    }

    override fun getBadgesByCategory(category: String): Flow<Resource<List<Badge>>> = flow {
        emit(Resource.Loading())
        
        try {
            badgeDao.getBadgesByCategory(category).collect { badgeEntities ->
                val badges = badgeEntities.map { entity ->
                    Badge(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        iconUrl = entity.iconUrl,
                        isUnlocked = entity.isUnlocked,
                        unlockedAt = entity.unlockedAt,
                        category = BadgeCategory.valueOf(entity.category)
                    )
                }
                emit(Resource.Success(badges))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get badges by category"))
        }
    }

    override suspend fun checkBadgeEligibility(): Resource<List<Badge>> {
        return try {
            val response = gamificationApi.checkBadgeEligibility()
            if (response.isSuccessful && response.body() != null) {
                val badges = response.body()!!.map { dto ->
                    Badge(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        iconUrl = dto.iconUrl,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis(),
                        category = BadgeCategory.valueOf(dto.category.uppercase())
                    )
                }
                Resource.Success(badges)
            } else {
                Resource.Success(emptyList())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check badge eligibility")
        }
    }

    override suspend fun unlockBadge(badgeId: String): Resource<Badge> {
        return try {
            val response = gamificationApi.unlockBadge(badgeId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Resource.Success(
                    Badge(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        iconUrl = dto.iconUrl,
                        isUnlocked = true,
                        unlockedAt = dto.unlockedAt ?: System.currentTimeMillis(),
                        category = BadgeCategory.valueOf(dto.category.uppercase())
                    )
                )
            } else {
                Resource.Error("Failed to unlock badge")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlock badge")
        }
    }

    override fun getLeaderboard(filter: String): Flow<Resource<List<LeaderboardEntryData>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = gamificationApi.getLeaderboard(filter)
            if (response.isSuccessful && response.body() != null) {
                val entries = response.body()!!.entries.mapIndexed { index, dto ->
                    LeaderboardEntryData(
                        rank = index + 1,
                        userId = dto.userId,
                        userName = dto.displayName,
                        avatarUrl = dto.avatarUrl,
                        xp = dto.totalXp,
                        level = dto.level,
                        isCurrentUser = dto.isCurrentUser ?: false
                    )
                }
                emit(Resource.Success(entries))
            } else {
                emit(Resource.Error("Failed to fetch leaderboard"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch leaderboard"))
        }
    }

    override fun getCurrentUserRank(filter: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = gamificationApi.getUserRank(filter)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.globalRank))
            } else {
                emit(Resource.Error("Failed to get user rank"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get user rank"))
        }
    }

    override suspend fun refreshLeaderboard(): Resource<Unit> {
        return try {
            gamificationApi.refreshLeaderboard()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh leaderboard")
        }
    }
}
