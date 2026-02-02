package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.data.local.dao.QuizDao
import com.hdaf.eduapp.data.mapper.QuizMapper
import com.hdaf.eduapp.data.remote.api.QuizApi
import com.hdaf.eduapp.domain.ai.EduAIService
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAnalytics
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.domain.model.ImprovementTrend
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of QuizRepository following offline-first pattern.
 * 
 * Features:
 * - Offline quiz access
 * - AI quiz generation via Gemini API
 * - Local caching of attempts
 * - Background sync with Supabase
 * - Analytics aggregation
 */
@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizApi: QuizApi,
    private val quizDao: QuizDao,
    private val networkMonitor: NetworkMonitor,
    private val quizMapper: QuizMapper,
    private val eduAIService: EduAIService
) : QuizRepository {

    // ==================== Quiz Retrieval ====================

    override fun getAllQuizzes(): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        try {
            val quizzes = quizDao.getAllQuizzes()
            if (quizzes.isNotEmpty()) {
                val domainQuizzes = quizzes.map { quiz ->
                    val questions = quizDao.getQuestionsByQuizId(quiz.id)
                    quizMapper.entityToDomain(quiz, questions)
                }
                emit(Resource.Success(domainQuizzes))
            } else {
                // Return sample quizzes for development
                emit(Resource.Success(listOf(
                    createSampleQuiz("sample_math"),
                    createSampleQuiz("sample_science"),
                    createSampleQuiz("sample_hindi")
                )))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching all quizzes")
            emit(Resource.Error(e.message ?: "Failed to load quizzes"))
        }
    }

    override fun getQuizById(quizId: String): Flow<Resource<Quiz>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get from local database
            val localQuiz = quizDao.getQuizById(quizId)
            val localQuestions = quizDao.getQuestionsByQuizId(quizId)
            
            if (localQuiz != null && localQuestions.isNotEmpty()) {
                emit(Resource.Success(quizMapper.entityToDomain(localQuiz, localQuestions)))
            } else {
                // Return sample quiz for development
                emit(Resource.Success(createSampleQuiz(quizId)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quiz: $quizId")
            emit(Resource.Error(e.message ?: "Failed to load quiz"))
        }
    }

    override fun getQuizzesByChapter(chapterId: String): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        try {
            val quizzes = quizDao.getQuizzesByChapterId(chapterId)
            if (quizzes.isNotEmpty()) {
                val domainQuizzes = quizzes.map { quiz ->
                    val questions = quizDao.getQuestionsByQuizId(quiz.id)
                    quizMapper.entityToDomain(quiz, questions)
                }
                emit(Resource.Success(domainQuizzes))
            } else {
                // Return sample quiz for development
                emit(Resource.Success(listOf(createSampleQuiz(chapterId))))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quizzes for chapter: $chapterId")
            emit(Resource.Error(e.message ?: "Failed to load quizzes"))
        }
    }

    override fun getQuizzesBySubject(subject: Subject): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        try {
            val quizzes = quizDao.getQuizzesBySubject(subject.name)
            if (quizzes.isNotEmpty()) {
                val domainQuizzes = quizzes.map { quiz ->
                    val questions = quizDao.getQuestionsByQuizId(quiz.id)
                    quizMapper.entityToDomain(quiz, questions)
                }
                emit(Resource.Success(domainQuizzes))
            } else {
                emit(Resource.Success(listOf(createSampleQuiz(UUID.randomUUID().toString()))))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quizzes for subject: $subject")
            emit(Resource.Error(e.message ?: "Failed to load quizzes"))
        }
    }

    override fun searchQuizzes(query: String): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        try {
            val quizzes = quizDao.searchQuizzes("%$query%")
            val domainQuizzes = quizzes.map { quiz ->
                val questions = quizDao.getQuestionsByQuizId(quiz.id)
                quizMapper.entityToDomain(quiz, questions)
            }
            emit(Resource.Success(domainQuizzes))
        } catch (e: Exception) {
            Timber.e(e, "Error searching quizzes: $query")
            emit(Resource.Error(e.message ?: "Failed to search quizzes"))
        }
    }

    // ==================== AI Quiz Generation ====================

    override suspend fun generateAiQuiz(
        chapterId: String,
        numberOfQuestions: Int,
        difficulty: QuizDifficulty
    ): Resource<Quiz> {
        return try {
            Timber.d("Generating AI quiz for chapter: $chapterId, questions: $numberOfQuestions, difficulty: $difficulty")
            
            // Check network connectivity
            if (!networkMonitor.isConnectedNow()) {
                Timber.w("No network, returning sample quiz")
                return Resource.Success(createSampleQuiz(chapterId, numberOfQuestions))
            }
            
            // Get chapter content for AI generation
            val chapterContent = getChapterContentForQuiz(chapterId)
            val subject = detectSubjectFromChapterId(chapterId)
            
            // Generate quiz using Gemini AI
            val result = eduAIService.generateQuiz(
                chapterContent = chapterContent,
                subject = subject,
                numberOfQuestions = numberOfQuestions,
                difficulty = difficulty
            )
            
            when (result) {
                is Resource.Success -> {
                    Timber.d("AI quiz generated successfully with ${result.data.questions.size} questions")
                    Resource.Success(result.data)
                }
                is Resource.Error -> {
                    Timber.w("AI generation failed, falling back to sample: ${result.message}")
                    Resource.Success(createSampleQuiz(chapterId, numberOfQuestions))
                }
                is Resource.Loading -> Resource.Loading()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error generating AI quiz, falling back to sample")
            Resource.Success(createSampleQuiz(chapterId, numberOfQuestions))
        }
    }
    
    /**
     * Get chapter content for AI quiz generation.
     * In production, this would fetch from database or API.
     */
    private suspend fun getChapterContentForQuiz(chapterId: String): String {
        // TODO: Fetch actual chapter content from ChapterDao/API
        // For now, return a generic content based on chapter ID
        return """
            अध्याय: ${chapterId.replace("_", " ").replaceFirstChar { it.uppercase() }}
            
            यह अध्याय विभिन्न महत्वपूर्ण अवधारणाओं को कवर करता है।
            छात्रों को इस अध्याय में मुख्य बिंदुओं को समझना होगा।
            
            मुख्य विषय:
            1. बुनियादी अवधारणाएं और परिभाषाएं
            2. महत्वपूर्ण सूत्र और नियम
            3. व्यावहारिक उदाहरण
            4. अभ्यास प्रश्न
        """.trimIndent()
    }
    
    /**
     * Detect subject from chapter ID.
     */
    private fun detectSubjectFromChapterId(chapterId: String): Subject {
        val lowerCaseId = chapterId.lowercase()
        return when {
            lowerCaseId.contains("math") || lowerCaseId.contains("गणित") -> Subject.MATH
            lowerCaseId.contains("science") || lowerCaseId.contains("विज्ञान") -> Subject.SCIENCE
            lowerCaseId.contains("english") || lowerCaseId.contains("अंग्रेज़ी") -> Subject.ENGLISH
            lowerCaseId.contains("hindi") || lowerCaseId.contains("हिंदी") -> Subject.HINDI
            lowerCaseId.contains("social") || lowerCaseId.contains("सामाजिक") -> Subject.SOCIAL_STUDIES
            lowerCaseId.contains("marathi") || lowerCaseId.contains("मराठी") -> Subject.MARATHI
            else -> Subject.MATH // Default
        }
    }

    override suspend fun generatePracticeQuiz(
        subject: Subject,
        weakTopics: List<String>,
        numberOfQuestions: Int
    ): Resource<Quiz> {
        return try {
            Timber.d("Generating practice quiz for subject: $subject, weak topics: $weakTopics")
            
            if (!networkMonitor.isConnectedNow()) {
                return Resource.Success(createSampleQuiz(subject.name, numberOfQuestions))
            }
            
            // Use AI service for adaptive quiz
            val result = eduAIService.generateAdaptiveQuiz(
                subject = subject,
                weakTopics = weakTopics,
                numberOfQuestions = numberOfQuestions
            )
            
            when (result) {
                is Resource.Success -> Resource.Success(result.data)
                is Resource.Error -> Resource.Success(createSampleQuiz(subject.name, numberOfQuestions))
                is Resource.Loading -> Resource.Loading()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error generating practice quiz")
            Resource.Success(createSampleQuiz(subject.name, numberOfQuestions))
        }
    }

    override suspend fun generateAdaptiveQuiz(
        subject: Subject,
        userId: String
    ): Resource<Quiz> {
        return try {
            // TODO: Implement adaptive quiz generation based on user performance
            Timber.d("Generating adaptive quiz for subject: $subject, user: $userId")
            Resource.Success(createSampleQuiz(subject.name))
        } catch (e: Exception) {
            Timber.e(e, "Error generating adaptive quiz")
            Resource.Error(e.message ?: "Failed to generate adaptive quiz")
        }
    }

    // ==================== Quiz Attempts ====================

    override suspend fun submitQuizAttempt(
        quizId: String,
        answers: List<Int>,
        timeTakenSeconds: Int
    ): Resource<QuizAttempt> {
        return try {
            // Calculate score based on answers
            val questions = quizDao.getQuestionsByQuizId(quizId)
            
            var correctCount = 0
            questions.forEachIndexed { index, question ->
                if (index < answers.size && answers[index] == question.correctAnswerIndex) {
                    correctCount++
                }
            }
            
            val scorePercentage = if (questions.isNotEmpty()) {
                correctCount.toFloat() / questions.size * 100f
            } else 0f
            
            // Get the quiz to determine subject
            val quiz = quizDao.getQuizById(quizId)
            val subject = quiz?.let { Subject.valueOf(it.subject) } ?: Subject.MATH
            
            val attempt = QuizAttempt(
                id = UUID.randomUUID().toString(),
                userId = "current_user", // TODO: Get from auth
                quizId = quizId,
                subject = subject,
                totalQuestions = questions.size,
                correctAnswers = correctCount,
                scorePercentage = scorePercentage,
                timeTakenSeconds = timeTakenSeconds,
                answers = answers
            )
            
            // Save to local database
            quizDao.insertAttempt(quizMapper.attemptToEntity(attempt))
            
            Resource.Success(attempt)
        } catch (e: Exception) {
            Timber.e(e, "Error submitting quiz attempt")
            Resource.Error(e.message ?: "Failed to submit quiz attempt")
        }
    }

    override fun getQuizAttempts(userId: String): Flow<Resource<List<QuizAttempt>>> = flow {
        emit(Resource.Loading())
        
        try {
            val attempts = quizDao.getAttemptsByUserId(userId)
            val domainAttempts = attempts.map { quizMapper.attemptEntityToDomain(it) }
            emit(Resource.Success(domainAttempts))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quiz attempts")
            emit(Resource.Error(e.message ?: "Failed to load quiz attempts"))
        }
    }

    override fun getQuizAttemptsByQuiz(quizId: String): Flow<Resource<List<QuizAttempt>>> = flow {
        emit(Resource.Loading())
        
        try {
            val attempts = quizDao.getAttemptsByQuizId(quizId)
            val domainAttempts = attempts.map { quizMapper.attemptEntityToDomain(it) }
            emit(Resource.Success(domainAttempts))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quiz attempts for quiz: $quizId")
            emit(Resource.Error(e.message ?: "Failed to load quiz attempts"))
        }
    }

    override fun getRecentAttempts(limit: Int): Flow<List<QuizAttempt>> = flow {
        try {
            val attempts = quizDao.getRecentAttempts(limit)
            emit(attempts.map { quizMapper.attemptEntityToDomain(it) })
        } catch (e: Exception) {
            Timber.e(e, "Error fetching recent attempts")
            emit(emptyList())
        }
    }

    override suspend fun getBestAttempt(quizId: String): Resource<QuizAttempt?> {
        return try {
            val attempt = quizDao.getBestAttemptForQuiz(quizId)
            Resource.Success(attempt?.let { quizMapper.attemptEntityToDomain(it) })
        } catch (e: Exception) {
            Timber.e(e, "Error fetching best attempt for quiz: $quizId")
            Resource.Error(e.message ?: "Failed to load best attempt")
        }
    }

    // ==================== Analytics ====================

    override suspend fun getSubjectAnalytics(subject: Subject): Resource<QuizAnalytics> {
        return try {
            val attempts = quizDao.getAttemptsBySubject(subject.name)
            
            if (attempts.isEmpty()) {
                return Resource.Success(createEmptyAnalytics(subject))
            }
            
            val totalAttempts = attempts.size
            val averageScore = attempts.map { it.scorePercentage.toDouble() }.average().toFloat()
            val bestScore = attempts.maxOfOrNull { it.scorePercentage } ?: 0f
            val totalTimeMinutes = attempts.sumOf { it.timeTakenSeconds } / 60
            
            val analytics = QuizAnalytics(
                subject = subject,
                totalAttempts = totalAttempts,
                averageScore = averageScore,
                bestScore = bestScore,
                totalTimeSpentMinutes = totalTimeMinutes,
                weakTopics = emptyList(), // TODO: Calculate from wrong answers
                strongTopics = emptyList(), // TODO: Calculate from correct answers
                improvementTrend = ImprovementTrend.STABLE
            )
            
            Resource.Success(analytics)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching subject analytics")
            Resource.Error(e.message ?: "Failed to load analytics")
        }
    }

    override suspend fun getOverallAnalytics(): Resource<List<QuizAnalytics>> {
        return try {
            val analyticsList = Subject.values().mapNotNull { subject ->
                val result = getSubjectAnalytics(subject)
                if (result is Resource.Success) result.data else null
            }
            Resource.Success(analyticsList)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching overall analytics")
            Resource.Error(e.message ?: "Failed to load analytics")
        }
    }

    override suspend fun getWeakTopics(): Resource<Map<Subject, List<String>>> {
        return try {
            // TODO: Implement weak topics analysis
            Resource.Success(emptyMap())
        } catch (e: Exception) {
            Timber.e(e, "Error fetching weak topics")
            Resource.Error(e.message ?: "Failed to load weak topics")
        }
    }

    override fun getAverageScoreBySubject(): Flow<Map<Subject, Float>> = flow {
        try {
            val scoreMap = Subject.values().associateWith { subject ->
                val attempts = quizDao.getAttemptsBySubject(subject.name)
                if (attempts.isNotEmpty()) {
                    attempts.map { it.scorePercentage }.average().toFloat()
                } else {
                    0f
                }
            }
            emit(scoreMap)
        } catch (e: Exception) {
            Timber.e(e, "Error calculating average scores")
            emit(emptyMap())
        }
    }

    // ==================== Offline Support ====================

    override suspend fun cacheQuiz(quizId: String): Resource<Unit> {
        return try {
            // Quiz is already cached when fetched
            Timber.d("Caching quiz: $quizId")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error caching quiz")
            Resource.Error(e.message ?: "Failed to cache quiz")
        }
    }

    override fun getCachedQuizzes(): Flow<List<Quiz>> = flow {
        try {
            val quizzes = quizDao.getAllQuizzes()
            val domainQuizzes = quizzes.map { quiz ->
                val questions = quizDao.getQuestionsByQuizId(quiz.id)
                quizMapper.entityToDomain(quiz, questions)
            }
            emit(domainQuizzes)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching cached quizzes")
            emit(emptyList())
        }
    }

    override suspend fun syncOfflineAttempts(): Resource<Int> {
        return try {
            val unsyncedAttempts = quizDao.getUnsyncedAttempts()
            
            if (unsyncedAttempts.isEmpty()) {
                return Resource.Success(0)
            }
            
            if (!networkMonitor.isCurrentlyConnected()) {
                return Resource.Error("No network connection")
            }
            
            // TODO: Sync with backend when API is ready
            Timber.d("Would sync ${unsyncedAttempts.size} attempts to server")
            
            Resource.Success(unsyncedAttempts.size)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing offline attempts")
            Resource.Error(e.message ?: "Failed to sync attempts")
        }
    }

    // ==================== Helper Functions ====================

    private fun createSampleQuiz(id: String, questionCount: Int = 5): Quiz {
        val questions = (1..questionCount).map { index ->
            QuizQuestion(
                id = "${id}_q$index",
                quizId = id,
                questionText = "Sample question $index for quiz",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                correctAnswerIndex = 0,
                explanation = "This is the explanation for question $index",
                orderIndex = index
            )
        }
        
        return Quiz(
            id = id,
            title = "Sample Quiz",
            chapterId = id,
            subject = Subject.MATH,
            difficulty = QuizDifficulty.MEDIUM,
            questions = questions,
            totalQuestions = questions.size,
            timeLimitMinutes = 15,
            isAiGenerated = false
        )
    }

    private fun createEmptyAnalytics(subject: Subject): QuizAnalytics {
        return QuizAnalytics(
            subject = subject,
            totalAttempts = 0,
            averageScore = 0f,
            bestScore = 0f,
            totalTimeSpentMinutes = 0,
            weakTopics = emptyList(),
            strongTopics = emptyList(),
            improvementTrend = ImprovementTrend.STABLE
        )
    }

    private fun calculateXpEarned(score: Int): Int {
        return when {
            score >= 90 -> 100
            score >= 80 -> 75
            score >= 70 -> 50
            score >= 60 -> 25
            else -> 10
        }
    }
}
