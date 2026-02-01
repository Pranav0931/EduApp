package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.core.security.SecurePreferences
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.local.entity.UserBadgeEntity
import com.hdaf.eduapp.data.local.entity.UserProgressEntity
import com.hdaf.eduapp.data.mapper.ProgressMapper
import com.hdaf.eduapp.data.remote.api.ContentApi
import com.hdaf.eduapp.data.remote.dto.XpUpdateDto
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.BadgeCategory
import com.hdaf.eduapp.domain.model.DailyGoal
import com.hdaf.eduapp.domain.model.LeaderboardEntry
import com.hdaf.eduapp.domain.model.UserProgress
import com.hdaf.eduapp.domain.repository.BadgeInfo
import com.hdaf.eduapp.domain.repository.ProgressRepository
import com.hdaf.eduapp.domain.repository.StreakStatus
import com.hdaf.eduapp.domain.repository.UserRank
import com.hdaf.eduapp.domain.repository.XpResult
import com.hdaf.eduapp.domain.repository.XpSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProgressRepository handling all gamification features.
 * 
 * Features:
 * - XP and leveling system
 * - Streak tracking
 * - Badge/achievement system
 * - Daily goals
 * - Leaderboards
 * - Offline-first with sync
 */
@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val contentApi: ContentApi,
    private val userProgressDao: UserProgressDao,
    private val networkMonitor: NetworkMonitor,
    private val securePreferences: SecurePreferences,
    private val progressMapper: ProgressMapper
) : ProgressRepository {

    companion object {
        // XP per level formula: Level * 100 + 50
        private const val BASE_XP_PER_LEVEL = 100
        private const val XP_OFFSET = 50
        
        // XP rewards
        private const val XP_QUIZ_BASE = 10
        private const val XP_CHAPTER_COMPLETE = 50
        private const val XP_BOOK_COMPLETE = 200
        private const val XP_DAILY_GOAL = 25
        private const val XP_STREAK_BONUS_MULTIPLIER = 0.1f
        
        // Streak constants
        private const val STREAK_GRACE_HOURS = 36L // 36 hours before streak breaks
        
        // Daily goal defaults
        private const val DEFAULT_DAILY_QUIZ_GOAL = 3
        private const val DEFAULT_DAILY_XP_GOAL = 100
    }

    // ==================== XP System ====================

    override suspend fun addXp(
        amount: Int,
        source: XpSource,
        description: String
    ): Resource<XpResult> {
        return try {
            val userId = getCurrentUserId()
            var progress = userProgressDao.getProgressForUser(userId) ?: createInitialProgress(userId)
            
            // Calculate streak bonus
            val streakBonus = (amount * progress.currentStreak * XP_STREAK_BONUS_MULTIPLIER).toInt()
            val totalXp = amount + streakBonus
            
            val previousLevel = progress.level
            val newTotalXp = progress.totalXp + totalXp
            val newLevel = calculateLevel(newTotalXp)
            val leveledUp = newLevel > previousLevel
            
            // Update progress
            val updatedProgress = progress.copy(
                totalXp = newTotalXp,
                level = newLevel,
                xpToNextLevel = calculateXpToNextLevel(newLevel, newTotalXp),
                lastActivityDate = Date()
            )
            
            userProgressDao.upsertProgress(updatedProgress)
            
            // Try to sync with server
            if (networkMonitor.isConnectedNow()) {
                try {
                    contentApi.addXp(
                        XpUpdateDto(
                            userId = userId,
                            xpAmount = totalXp,
                            source = source.name,
                            description = description
                        )
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to sync XP to server")
                }
            }
            
            Timber.d("Added $totalXp XP (base: $amount, streak bonus: $streakBonus). Level: $newLevel")
            
            Resource.Success(
                XpResult(
                    xpEarned = totalXp,
                    newTotalXp = newTotalXp,
                    newLevel = newLevel,
                    leveledUp = leveledUp,
                    streakBonus = streakBonus
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error adding XP")
            Resource.Error(e.message ?: "Failed to add XP")
        }
    }

    override fun getUserProgress(): Flow<Resource<UserProgress>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            val progress = userProgressDao.getProgressForUser(userId)
                ?: createInitialProgress(userId)
            
            val badges = userProgressDao.getBadgesForUser(userId)
            
            emit(Resource.Success(progressMapper.entityToDomain(progress, badges)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun observeProgress(): Flow<UserProgress> {
        val userId = getCurrentUserId()
        return userProgressDao.observeProgressForUser(userId)
            .map { progress ->
                val badges = userProgressDao.getBadgesForUser(userId)
                progressMapper.entityToDomain(
                    progress ?: createInitialProgress(userId),
                    badges
                )
            }
    }

    override suspend fun syncProgress(): Resource<Unit> {
        return try {
            if (!networkMonitor.isConnectedNow()) {
                return Resource.Error("No network connection")
            }
            
            val userId = getCurrentUserId()
            val remoteProgress = contentApi.getUserProgress(userId)
            
            val progressEntity = progressMapper.dtoToEntity(remoteProgress)
            userProgressDao.upsertProgress(progressEntity)
            
            // Sync badges
            remoteProgress.badges.forEach { badgeDto ->
                val badgeEntity = progressMapper.badgeDtoToEntity(badgeDto)
                userProgressDao.upsertBadge(badgeEntity)
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing progress")
            Resource.Error(e.message ?: "Sync failed")
        }
    }

    // ==================== Streak System ====================

    override suspend fun updateStreak(): Resource<StreakStatus> {
        return try {
            val userId = getCurrentUserId()
            var progress = userProgressDao.getProgressForUser(userId)
                ?: createInitialProgress(userId)
            
            val now = Date()
            val lastActivity = progress.lastActivityDate
            val hoursSinceLastActivity = if (lastActivity != null) {
                TimeUnit.MILLISECONDS.toHours(now.time - lastActivity.time)
            } else {
                Long.MAX_VALUE
            }
            
            val streakBroken = hoursSinceLastActivity > STREAK_GRACE_HOURS
            val isNewDay = !isSameDay(lastActivity, now)
            
            val updatedProgress = if (streakBroken) {
                // Streak broken - reset
                Timber.d("Streak broken! Hours since last activity: $hoursSinceLastActivity")
                progress.copy(
                    currentStreak = 1,
                    lastActivityDate = now
                )
            } else if (isNewDay) {
                // Continue streak
                val newStreak = progress.currentStreak + 1
                val newMaxStreak = maxOf(progress.maxStreak, newStreak)
                Timber.d("Streak continued! New streak: $newStreak")
                progress.copy(
                    currentStreak = newStreak,
                    maxStreak = newMaxStreak,
                    lastActivityDate = now
                )
            } else {
                // Same day - just update last activity
                progress.copy(lastActivityDate = now)
            }
            
            userProgressDao.upsertProgress(updatedProgress)
            
            Resource.Success(
                StreakStatus(
                    currentStreak = updatedProgress.currentStreak,
                    maxStreak = updatedProgress.maxStreak,
                    streakBroken = streakBroken,
                    hoursUntilStreakLost = (STREAK_GRACE_HOURS - hoursSinceLastActivity).coerceAtLeast(0)
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error updating streak")
            Resource.Error(e.message ?: "Failed to update streak")
        }
    }

    override fun getStreakInfo(): Flow<Resource<StreakStatus>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            val progress = userProgressDao.getProgressForUser(userId)
                ?: createInitialProgress(userId)
            
            val now = Date()
            val lastActivity = progress.lastActivityDate
            val hoursSinceLastActivity = if (lastActivity != null) {
                TimeUnit.MILLISECONDS.toHours(now.time - lastActivity.time)
            } else {
                Long.MAX_VALUE
            }
            
            emit(Resource.Success(
                StreakStatus(
                    currentStreak = progress.currentStreak,
                    maxStreak = progress.maxStreak,
                    streakBroken = false,
                    hoursUntilStreakLost = (STREAK_GRACE_HOURS - hoursSinceLastActivity).coerceAtLeast(0)
                )
            ))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    // ==================== Badge System ====================

    override fun getUserBadges(): Flow<Resource<List<Badge>>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            val badges = userProgressDao.getBadgesForUser(userId)
            
            emit(Resource.Success(badges.map { progressMapper.badgeEntityToDomain(it) }))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getAvailableBadges(): Flow<Resource<List<BadgeInfo>>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            val earnedBadges = userProgressDao.getBadgesForUser(userId).map { it.badgeId }.toSet()
            
            // Define all available badges
            val allBadges = getAllDefinedBadges()
            
            val badgeInfoList = allBadges.map { badge ->
                BadgeInfo(
                    badge = badge,
                    isEarned = badge.id in earnedBadges,
                    progress = calculateBadgeProgress(badge, userId)
                )
            }
            
            emit(Resource.Success(badgeInfoList))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun checkAndAwardBadges(): Resource<List<Badge>> {
        return try {
            val userId = getCurrentUserId()
            val progress = userProgressDao.getProgressForUser(userId)
                ?: return Resource.Success(emptyList())
            
            val earnedBadgeIds = userProgressDao.getBadgesForUser(userId).map { it.badgeId }.toSet()
            val newlyEarnedBadges = mutableListOf<Badge>()
            
            // Check each badge condition
            getAllDefinedBadges().forEach { badge ->
                if (badge.id !in earnedBadgeIds && checkBadgeCondition(badge, progress)) {
                    // Award badge
                    val badgeEntity = UserBadgeEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        badgeId = badge.id,
                        badgeType = badge.category.name,
                        rarity = "COMMON",
                        title = badge.name,
                        description = badge.description,
                        iconUrl = badge.iconUrl,
                        earnedAt = Date(),
                        isSynced = false
                    )
                    
                    userProgressDao.upsertBadge(badgeEntity)
                    newlyEarnedBadges.add(badge)
                    
                    Timber.d("Badge earned: ${badge.name}")
                }
            }
            
            Resource.Success(newlyEarnedBadges)
        } catch (e: Exception) {
            Timber.e(e, "Error checking badges")
            Resource.Error(e.message ?: "Failed to check badges")
        }
    }

    override suspend fun awardBadge(badgeId: String): Resource<Badge> {
        return try {
            val userId = getCurrentUserId()
            val badge = getAllDefinedBadges().find { it.id == badgeId }
                ?: return Resource.Error("Badge not found")
            
            val badgeEntity = UserBadgeEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                badgeId = badge.id,
                badgeType = badge.category.name,
                rarity = "COMMON",
                title = badge.name,
                description = badge.description,
                iconUrl = badge.iconUrl,
                earnedAt = Date(),
                isSynced = false
            )
            
            userProgressDao.upsertBadge(badgeEntity)
            
            Resource.Success(badge)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to award badge")
        }
    }

    // ==================== Daily Goals ====================

    override fun getDailyGoal(): Flow<Resource<DailyGoal>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            val progress = userProgressDao.getProgressForUser(userId)
                ?: createInitialProgress(userId)
            
            val todayStart = getTodayStart()
            val todayQuizzes = userProgressDao.getQuizzesCompletedSince(userId, todayStart)
            val todayXp = userProgressDao.getXpEarnedSince(userId, todayStart)
            
            val quizGoal = securePreferences.getInt("daily_quiz_goal", DEFAULT_DAILY_QUIZ_GOAL)
            val xpGoal = securePreferences.getInt("daily_xp_goal", DEFAULT_DAILY_XP_GOAL)
            
            val dailyGoal = DailyGoal(
                quizzesCompleted = todayQuizzes,
                quizzesGoal = quizGoal,
                xpEarned = todayXp,
                xpGoal = xpGoal,
                isCompleted = todayQuizzes >= quizGoal || todayXp >= xpGoal,
                date = Date()
            )
            
            emit(Resource.Success(dailyGoal))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun setDailyGoal(quizCount: Int, xpTarget: Int): Resource<DailyGoal> {
        return try {
            securePreferences.putInt("daily_quiz_goal", quizCount)
            securePreferences.putInt("daily_xp_goal", xpTarget)
            
            val dailyGoal = DailyGoal(
                quizzesCompleted = 0,
                quizzesGoal = quizCount,
                xpEarned = 0,
                xpGoal = xpTarget,
                isCompleted = false,
                date = Date()
            )
            
            Resource.Success(dailyGoal)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to set daily goal")
        }
    }

    override suspend fun checkDailyGoalCompletion(): Resource<Boolean> {
        return try {
            val userId = getCurrentUserId()
            val todayStart = getTodayStart()
            
            val todayQuizzes = userProgressDao.getQuizzesCompletedSince(userId, todayStart)
            val todayXp = userProgressDao.getXpEarnedSince(userId, todayStart)
            
            val quizGoal = securePreferences.getInt("daily_quiz_goal", DEFAULT_DAILY_QUIZ_GOAL)
            val xpGoal = securePreferences.getInt("daily_xp_goal", DEFAULT_DAILY_XP_GOAL)
            
            val isCompleted = todayQuizzes >= quizGoal || todayXp >= xpGoal
            
            if (isCompleted) {
                // Award daily goal XP bonus
                addXp(XP_DAILY_GOAL, XpSource.DAILY_BONUS, "Daily goal completed!")
            }
            
            Resource.Success(isCompleted)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    // ==================== Leaderboards ====================

    override fun getLeaderboard(
        scope: String,
        limit: Int
    ): Flow<Resource<List<LeaderboardEntry>>> = flow {
        emit(Resource.Loading())
        
        try {
            if (networkMonitor.isConnectedNow()) {
                val entries = contentApi.getLeaderboard(scope, limit)
                val leaderboard = entries.mapIndexed { index, dto ->
                    progressMapper.leaderboardDtoToDomain(dto).copy(rank = index + 1)
                }
                emit(Resource.Success(leaderboard))
            } else {
                // Offline - return cached or error
                emit(Resource.Error("Leaderboard requires internet connection"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load leaderboard"))
        }
    }

    override fun getUserRank(): Flow<Resource<UserRank>> = flow {
        emit(Resource.Loading())
        
        try {
            val userId = getCurrentUserId()
            
            if (networkMonitor.isConnectedNow()) {
                val rankResponse = contentApi.getUserRank(userId)
                emit(Resource.Success(
                    UserRank(
                        globalRank = rankResponse.globalRank,
                        weeklyRank = rankResponse.weeklyRank,
                        monthlyRank = rankResponse.monthlyRank,
                        percentile = rankResponse.percentile,
                        totalUsers = rankResponse.totalUsers
                    )
                ))
            } else {
                // Return cached rank if available
                val progress = userProgressDao.getProgressForUser(userId)
                if (progress != null) {
                    emit(Resource.Success(
                        UserRank(
                            globalRank = progress.globalRank,
                            weeklyRank = progress.weeklyRank,
                            monthlyRank = 0,
                            percentile = 0f,
                            totalUsers = 0
                        )
                    ))
                } else {
                    emit(Resource.Error("Rank data unavailable offline"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get rank"))
        }
    }

    // ==================== Helper Functions ====================

    private fun getCurrentUserId(): String {
        return securePreferences.getString("user_id", "local_user") ?: "local_user"
    }

    private fun createInitialProgress(userId: String): UserProgressEntity {
        val progress = UserProgressEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            totalXp = 0,
            level = 1,
            xpToNextLevel = calculateXpForLevel(2),
            currentStreak = 0,
            maxStreak = 0,
            quizzesCompleted = 0,
            chaptersCompleted = 0,
            booksCompleted = 0,
            totalStudyTimeMinutes = 0,
            globalRank = 0,
            weeklyRank = 0,
            lastActivityDate = null,
            lastSyncDate = null,
            isSynced = false
        )
        return progress
    }

    private fun calculateLevel(totalXp: Int): Int {
        var level = 1
        var xpForNextLevel = calculateXpForLevel(level + 1)
        var accumulatedXp = 0
        
        while (accumulatedXp + xpForNextLevel <= totalXp) {
            accumulatedXp += xpForNextLevel
            level++
            xpForNextLevel = calculateXpForLevel(level + 1)
        }
        
        return level
    }

    private fun calculateXpForLevel(level: Int): Int {
        return (level * BASE_XP_PER_LEVEL) + XP_OFFSET
    }

    private fun calculateXpToNextLevel(currentLevel: Int, totalXp: Int): Int {
        var accumulatedXp = 0
        for (l in 1..currentLevel) {
            accumulatedXp += calculateXpForLevel(l)
        }
        val xpForNextLevel = calculateXpForLevel(currentLevel + 1)
        return xpForNextLevel - (totalXp - accumulatedXp + calculateXpForLevel(currentLevel))
    }

    private fun isSameDay(date1: Date?, date2: Date): Boolean {
        if (date1 == null) return false
        
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getTodayStart(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun getAllDefinedBadges(): List<Badge> {
        return listOf(
            // Streak Badges
            Badge(
                id = "streak_3",
                name = "3 दिन की लकीर",
                description = "लगातार 3 दिन पढ़ाई करें",
                iconUrl = "badge_streak_3",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.STREAK
            ),
            Badge(
                id = "streak_7",
                name = "सप्ताह योद्धा",
                description = "लगातार 7 दिन पढ़ाई करें",
                iconUrl = "badge_streak_7",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.STREAK
            ),
            Badge(
                id = "streak_30",
                name = "महीना मास्टर",
                description = "लगातार 30 दिन पढ़ाई करें",
                iconUrl = "badge_streak_30",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.STREAK
            ),
            
            // Quiz Badges
            Badge(
                id = "quiz_first",
                name = "पहली प्रश्नोत्तरी",
                description = "अपनी पहली प्रश्नोत्तरी पूरी करें",
                iconUrl = "badge_quiz_first",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.QUIZ
            ),
            Badge(
                id = "quiz_10",
                name = "प्रश्नोत्तरी उत्साही",
                description = "10 प्रश्नोत्तरियाँ पूरी करें",
                iconUrl = "badge_quiz_10",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.QUIZ
            ),
            Badge(
                id = "quiz_perfect",
                name = "परफेक्ट स्कोर",
                description = "किसी प्रश्नोत्तरी में 100% अंक प्राप्त करें",
                iconUrl = "badge_quiz_perfect",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.QUIZ
            ),
            
            // Level Badges
            Badge(
                id = "level_5",
                name = "स्तर 5",
                description = "स्तर 5 तक पहुंचें",
                iconUrl = "badge_level_5",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            ),
            Badge(
                id = "level_10",
                name = "स्तर 10",
                description = "स्तर 10 तक पहुंचें",
                iconUrl = "badge_level_10",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            ),
            Badge(
                id = "level_25",
                name = "स्तर 25",
                description = "स्तर 25 तक पहुंचें",
                iconUrl = "badge_level_25",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            ),
            
            // Subject Mastery
            Badge(
                id = "math_master",
                name = "गणित गुरु",
                description = "गणित में 90%+ औसत स्कोर",
                iconUrl = "badge_math_master",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.LEARNING
            ),
            Badge(
                id = "science_master",
                name = "विज्ञान विशेषज्ञ",
                description = "विज्ञान में 90%+ औसत स्कोर",
                iconUrl = "badge_science_master",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.LEARNING
            )
        )
    }

    private fun checkBadgeCondition(badge: Badge, progress: UserProgressEntity): Boolean {
        return when (badge.id) {
            "streak_3" -> progress.currentStreak >= 3
            "streak_7" -> progress.currentStreak >= 7
            "streak_30" -> progress.currentStreak >= 30
            "quiz_first" -> progress.quizzesCompleted >= 1
            "quiz_10" -> progress.quizzesCompleted >= 10
            "level_5" -> progress.level >= 5
            "level_10" -> progress.level >= 10
            "level_25" -> progress.level >= 25
            else -> false
        }
    }

    private suspend fun calculateBadgeProgress(badge: Badge, userId: String): Float {
        val progress = userProgressDao.getProgressForUser(userId) ?: return 0f
        
        return when (badge.id) {
            "streak_3" -> (progress.currentStreak / 3f).coerceAtMost(1f)
            "streak_7" -> (progress.currentStreak / 7f).coerceAtMost(1f)
            "streak_30" -> (progress.currentStreak / 30f).coerceAtMost(1f)
            "quiz_first" -> if (progress.quizzesCompleted >= 1) 1f else 0f
            "quiz_10" -> (progress.quizzesCompleted / 10f).coerceAtMost(1f)
            "level_5" -> (progress.level / 5f).coerceAtMost(1f)
            "level_10" -> (progress.level / 10f).coerceAtMost(1f)
            "level_25" -> (progress.level / 25f).coerceAtMost(1f)
            else -> 0f
        }
    }
}
