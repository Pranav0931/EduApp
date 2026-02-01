package com.hdaf.eduapp.domain.usecase.progress

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
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
import javax.inject.Inject

/**
 * Use case for getting user progress.
 */
class GetUserProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<UserProgress>> {
        return progressRepository.getUserProgress()
    }
}

/**
 * Use case for observing progress changes in real-time.
 */
class ObserveProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<UserProgress> {
        return progressRepository.observeProgress()
    }
}

/**
 * Use case for adding XP.
 */
class AddXpUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        amount: Int,
        source: XpSource,
        description: String = ""
    ): Resource<XpResult> {
        return progressRepository.addXp(amount, source, description)
    }
}

/**
 * Use case for updating streak.
 */
class UpdateStreakUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(): Resource<StreakStatus> {
        return progressRepository.updateStreak()
    }
}

/**
 * Use case for getting streak status.
 */
class GetStreakInfoUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<StreakStatus>> {
        return progressRepository.getStreakInfo()
    }
}

/**
 * Use case for getting user badges.
 */
class GetUserBadgesUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<List<Badge>>> {
        return progressRepository.getUserBadges()
    }
}

/**
 * Use case for getting available badges with progress.
 */
class GetAvailableBadgesUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<List<BadgeInfo>>> {
        return progressRepository.getAvailableBadges()
    }
}

/**
 * Use case for checking and awarding badges.
 */
class CheckAndAwardBadgesUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(): Resource<List<Badge>> {
        return progressRepository.checkAndAwardBadges()
    }
}

/**
 * Use case for awarding a specific badge.
 */
class AwardBadgeUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(badgeId: String): Resource<Badge> {
        return progressRepository.awardBadge(badgeId)
    }
}

/**
 * Use case for getting daily goals.
 */
class GetDailyGoalUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<DailyGoal>> {
        return progressRepository.getDailyGoal()
    }
}

/**
 * Use case for setting daily goals.
 */
class SetDailyGoalUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        quizCount: Int,
        xpTarget: Int
    ): Resource<DailyGoal> {
        return progressRepository.setDailyGoal(quizCount, xpTarget)
    }
}

/**
 * Use case for checking daily goal completion.
 */
class CheckDailyGoalCompletionUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(): Resource<Boolean> {
        return progressRepository.checkDailyGoalCompletion()
    }
}

/**
 * Use case for getting leaderboard.
 */
class GetLeaderboardUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(
        scope: String = "global",
        limit: Int = 100
    ): Flow<Resource<List<LeaderboardEntry>>> {
        return progressRepository.getLeaderboard(scope, limit)
    }
}

/**
 * Use case for getting user's rank.
 */
class GetUserRankUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<UserRank>> {
        return progressRepository.getUserRank()
    }
}

/**
 * Use case for syncing progress.
 */
class SyncProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return progressRepository.syncProgress()
    }
}
