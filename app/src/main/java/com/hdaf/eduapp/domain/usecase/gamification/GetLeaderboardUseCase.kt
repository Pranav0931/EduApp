package com.hdaf.eduapp.domain.usecase.gamification

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.repository.GamificationRepository
import com.hdaf.eduapp.domain.repository.LeaderboardEntryData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting leaderboard data.
 */
class GetLeaderboardUseCase @Inject constructor(
    private val gamificationRepository: GamificationRepository
) {
    operator fun invoke(filter: String): Flow<Resource<List<LeaderboardEntryData>>> {
        return gamificationRepository.getLeaderboard(filter)
    }
}
