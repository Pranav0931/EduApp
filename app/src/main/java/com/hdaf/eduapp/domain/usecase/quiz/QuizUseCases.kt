package com.hdaf.eduapp.domain.usecase.quiz

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAnalytics
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting quizzes by chapter.
 */
class GetQuizzesByChapterUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(chapterId: String): Flow<Resource<List<Quiz>>> {
        return quizRepository.getQuizzesByChapter(chapterId)
    }
}

/**
 * Use case for getting quizzes by subject.
 */
class GetQuizzesBySubjectUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(subject: Subject): Flow<Resource<List<Quiz>>> {
        return quizRepository.getQuizzesBySubject(subject)
    }
}

/**
 * Use case for getting a specific quiz with questions.
 */
class GetQuizByIdUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(quizId: String): Flow<Resource<Quiz>> {
        return quizRepository.getQuizById(quizId)
    }
}

/**
 * Use case for generating AI-powered quiz.
 */
class GenerateAiQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(
        chapterId: String,
        numberOfQuestions: Int = 10,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    ): Resource<Quiz> {
        return quizRepository.generateAiQuiz(chapterId, numberOfQuestions, difficulty)
    }
}

/**
 * Use case for generating practice quiz based on weak topics.
 */
class GeneratePracticeQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(
        subject: Subject,
        weakTopics: List<String>,
        numberOfQuestions: Int = 10
    ): Resource<Quiz> {
        return quizRepository.generatePracticeQuiz(subject, weakTopics, numberOfQuestions)
    }
}

/**
 * Use case for generating adaptive quiz based on user performance.
 */
class GenerateAdaptiveQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(subject: Subject, userId: String): Resource<Quiz> {
        return quizRepository.generateAdaptiveQuiz(subject, userId)
    }
}

/**
 * Use case for submitting a quiz attempt.
 */
class SubmitQuizAttemptUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(
        quizId: String,
        answers: List<Int>,
        timeTakenSeconds: Int
    ): Resource<QuizAttempt> {
        return quizRepository.submitQuizAttempt(quizId, answers, timeTakenSeconds)
    }
}

/**
 * Use case for getting quiz attempts.
 */
class GetQuizAttemptsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<QuizAttempt>>> {
        return quizRepository.getQuizAttempts(userId)
    }
}

/**
 * Use case for getting recent quiz attempts.
 */
class GetRecentAttemptsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<QuizAttempt>> {
        return quizRepository.getRecentAttempts(limit)
    }
}

/**
 * Use case for getting quiz analytics by subject.
 */
class GetSubjectAnalyticsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(subject: Subject): Resource<QuizAnalytics> {
        return quizRepository.getSubjectAnalytics(subject)
    }
}

/**
 * Use case for getting overall quiz analytics.
 */
class GetOverallAnalyticsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(): Resource<List<QuizAnalytics>> {
        return quizRepository.getOverallAnalytics()
    }
}

/**
 * Use case for getting weak topics across subjects.
 */
class GetWeakTopicsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(): Resource<Map<Subject, List<String>>> {
        return quizRepository.getWeakTopics()
    }
}

/**
 * Use case for caching quiz for offline.
 */
class CacheQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(quizId: String): Resource<Unit> {
        return quizRepository.cacheQuiz(quizId)
    }
}

/**
 * Use case for syncing offline attempts.
 */
class SyncOfflineAttemptsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(): Resource<Int> {
        return quizRepository.syncOfflineAttempts()
    }
}
