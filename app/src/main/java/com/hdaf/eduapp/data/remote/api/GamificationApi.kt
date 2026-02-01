package com.hdaf.eduapp.data.remote.api

import com.hdaf.eduapp.data.remote.dto.BadgeDto
import com.hdaf.eduapp.data.remote.dto.LeaderboardResponseDto
import com.hdaf.eduapp.data.remote.dto.UserRankDto
import com.hdaf.eduapp.data.remote.dto.UserStatsDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Gamification API endpoints.
 */
interface GamificationApi {

    @GET("gamification/stats")
    suspend fun getUserStats(): Response<UserStatsDto>

    @POST("gamification/stats")
    suspend fun updateStats(
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): Response<UserStatsDto>

    @GET("gamification/badges")
    suspend fun getUserBadges(): Response<List<BadgeDto>>

    @POST("gamification/badges/check")
    suspend fun checkBadgeEligibility(): Response<List<BadgeDto>>

    @POST("gamification/badges/{badgeId}/unlock")
    suspend fun unlockBadge(
        @Path("badgeId") badgeId: String
    ): Response<BadgeDto>

    @GET("gamification/leaderboard")
    suspend fun getLeaderboard(
        @Query("filter") filter: String
    ): Response<LeaderboardResponseDto>

    @GET("gamification/rank")
    suspend fun getUserRank(
        @Query("filter") filter: String
    ): Response<UserRankDto>

    @POST("gamification/leaderboard/refresh")
    suspend fun refreshLeaderboard(): Response<Unit>
}
