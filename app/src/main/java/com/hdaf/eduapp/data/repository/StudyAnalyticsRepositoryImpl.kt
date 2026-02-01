package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.StudyAnalyticsDao
import com.hdaf.eduapp.data.local.dao.StudyRecommendationDao
import com.hdaf.eduapp.data.mapper.RecommendationType
import com.hdaf.eduapp.data.mapper.StudyAnalytics
import com.hdaf.eduapp.data.mapper.StudyRecommendation
import com.hdaf.eduapp.data.mapper.toDomain
import com.hdaf.eduapp.data.mapper.toEntity
import com.hdaf.eduapp.domain.repository.AccessibilityRepository
import com.hdaf.eduapp.domain.repository.StudyAnalyticsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StudyAnalyticsRepository.
 * Tracks study patterns and generates adaptive recommendations.
 */
@Singleton
class StudyAnalyticsRepositoryImpl @Inject constructor(
    private val studyAnalyticsDao: StudyAnalyticsDao,
    private val studyRecommendationDao: StudyRecommendationDao,
    private val accessibilityRepository: AccessibilityRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StudyAnalyticsRepository {

    override suspend fun logEvent(analytics: StudyAnalytics): Resource<Long> = withContext(ioDispatcher) {
        try {
            val id = studyAnalyticsDao.insertAnalytics(analytics.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error logging study event")
            Resource.Error("Failed to log event: ${e.message}")
        }
    }

    override suspend fun getAnalytics(userId: String, limit: Int): Resource<List<StudyAnalytics>> = 
        withContext(ioDispatcher) {
            try {
                val analytics = studyAnalyticsDao.getRecentAnalytics(userId, limit)
                Resource.Success(analytics.map { it.toDomain() })
            } catch (e: Exception) {
                Timber.e(e, "Error getting analytics")
                Resource.Error("Failed to get analytics: ${e.message}")
            }
        }

    override suspend fun getAnalyticsBySubject(
        userId: String,
        subjectId: String
    ): Resource<List<StudyAnalytics>> = withContext(ioDispatcher) {
        try {
            val analytics = studyAnalyticsDao.getAnalyticsBySubject(userId, subjectId)
            Resource.Success(analytics.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Error getting analytics by subject")
            Resource.Error("Failed to get analytics: ${e.message}")
        }
    }

    override suspend fun getAverageScore(userId: String, subjectId: String): Float? = 
        withContext(ioDispatcher) {
            try {
                studyAnalyticsDao.getAverageScore(userId, subjectId)
            } catch (e: Exception) {
                Timber.e(e, "Error getting average score")
                null
            }
        }

    override suspend fun getTotalStudyTime(userId: String, sinceTimestamp: Long): Int? = 
        withContext(ioDispatcher) {
            try {
                studyAnalyticsDao.getTotalStudyTime(userId, sinceTimestamp)
            } catch (e: Exception) {
                Timber.e(e, "Error getting total study time")
                null
            }
        }

    override suspend fun getPreferredContentType(userId: String): String? = withContext(ioDispatcher) {
        try {
            studyAnalyticsDao.getPreferredContentType(userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting preferred content type")
            null
        }
    }

    override suspend fun generateRecommendations(userId: String): Resource<List<StudyRecommendation>> = 
        withContext(ioDispatcher) {
            try {
                // Clear expired recommendations first
                studyRecommendationDao.deleteExpiredRecommendations()

                // Get user's profile and analytics
                val profileResult = accessibilityRepository.getProfile(userId)
                val recentAnalytics = studyAnalyticsDao.getRecentAnalytics(userId, 50)
                
                val recommendations = mutableListOf<StudyRecommendation>()
                
                // Analyze patterns and generate recommendations
                val subjectScores = mutableMapOf<String, MutableList<Float>>()
                val subjectMistakes = mutableMapOf<String, Int>()
                var totalStudyTime = 0
                
                recentAnalytics.forEach { analytics ->
                    analytics.score?.let { score ->
                        subjectScores.getOrPut(analytics.subjectId) { mutableListOf() }.add(score)
                    }
                    subjectMistakes[analytics.subjectId] = 
                        (subjectMistakes[analytics.subjectId] ?: 0) + analytics.mistakesCount
                    totalStudyTime += analytics.durationSeconds
                }
                
                // Generate recommendations based on weak areas
                subjectScores.forEach { (subjectId, scores) ->
                    val avgScore = scores.average().toFloat()
                    if (avgScore < 0.7f) {
                        recommendations.add(
                            StudyRecommendation(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                recommendationType = RecommendationType.REVIEW,
                                subjectId = subjectId,
                                title = "Review Needed",
                                description = "Your average score in this subject is ${(avgScore * 100).toInt()}%. Consider reviewing the material.",
                                priority = 2,
                                reason = "Low quiz scores detected"
                            )
                        )
                    }
                }
                
                // Suggest breaks if studying too long
                if (totalStudyTime > 3600) { // More than 1 hour
                    recommendations.add(
                        StudyRecommendation(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            recommendationType = RecommendationType.BREAK,
                            title = "Take a Break",
                            description = "You've been studying for a while. A short break can help you retain information better.",
                            priority = 1,
                            reason = "Extended study session detected"
                        )
                    )
                }
                
                // Suggest practice quizzes for subjects with many mistakes
                subjectMistakes.filter { it.value > 5 }.forEach { (subjectId, mistakes) ->
                    recommendations.add(
                        StudyRecommendation(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            recommendationType = RecommendationType.QUIZ,
                            subjectId = subjectId,
                            title = "Practice Quiz",
                            description = "Practice makes perfect! Try a quiz to reinforce your learning.",
                            priority = 2,
                            reason = "$mistakes mistakes in recent quizzes"
                        )
                    )
                }
                
                // Add content recommendations based on accessibility mode
                if (profileResult is Resource.Success) {
                    val profile = profileResult.data
                    when {
                        profile.preferAudioContent -> {
                            recommendations.add(
                                StudyRecommendation(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    recommendationType = RecommendationType.CONTENT,
                                    title = "Audio Learning",
                                    description = "Try our audio lessons for a hands-free learning experience.",
                                    priority = 1,
                                    reason = "Based on your audio content preference"
                                )
                            )
                        }
                        profile.preferVisualContent -> {
                            recommendations.add(
                                StudyRecommendation(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    recommendationType = RecommendationType.CONTENT,
                                    title = "Visual Learning",
                                    description = "Check out our video lessons with subtitles and visual aids.",
                                    priority = 1,
                                    reason = "Based on your visual content preference"
                                )
                            )
                        }
                    }
                }
                
                // Save recommendations
                if (recommendations.isNotEmpty()) {
                    studyRecommendationDao.insertRecommendations(recommendations.map { it.toEntity() })
                }
                
                Resource.Success(recommendations.sortedByDescending { it.priority })
                
            } catch (e: Exception) {
                Timber.e(e, "Error generating recommendations")
                Resource.Error("Failed to generate recommendations: ${e.message}")
            }
        }

    override suspend fun getActiveRecommendations(userId: String): Resource<List<StudyRecommendation>> = 
        withContext(ioDispatcher) {
            try {
                val recommendations = studyRecommendationDao.getActiveRecommendations(userId)
                Resource.Success(recommendations.map { it.toDomain() })
            } catch (e: Exception) {
                Timber.e(e, "Error getting active recommendations")
                Resource.Error("Failed to get recommendations: ${e.message}")
            }
        }

    override fun observeRecommendations(userId: String): Flow<List<StudyRecommendation>> {
        return studyRecommendationDao.observeActiveRecommendations(userId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun markRecommendationCompleted(recommendationId: String): Resource<Unit> = 
        withContext(ioDispatcher) {
            try {
                studyRecommendationDao.markCompleted(recommendationId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error marking recommendation completed")
                Resource.Error("Failed to mark completed: ${e.message}")
            }
        }
}
