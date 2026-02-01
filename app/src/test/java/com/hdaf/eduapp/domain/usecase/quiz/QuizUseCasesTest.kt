package com.hdaf.eduapp.domain.usecase.quiz

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.QuizRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Quiz Use Cases.
 */
class QuizUseCasesTest {

    private lateinit var quizRepository: QuizRepository
    private lateinit var getQuizByIdUseCase: GetQuizByIdUseCase
    private lateinit var submitQuizAttemptUseCase: SubmitQuizAttemptUseCase
    private lateinit var generateAiQuizUseCase: GenerateAiQuizUseCase

    private val testQuiz = Quiz(
        id = "quiz_1",
        chapterId = "chapter_1",
        title = "Test Quiz",
        subject = Subject.MATH,
        difficulty = QuizDifficulty.MEDIUM,
        totalQuestions = 5,
        timeLimitMinutes = 10,
        isAiGenerated = false,
        questions = listOf(
            QuizQuestion(
                id = "q1",
                quizId = "quiz_1",
                questionText = "What is 2 + 2?",
                options = listOf("3", "4", "5", "6"),
                correctAnswerIndex = 1,
                explanation = "2 + 2 = 4",
                orderIndex = 0
            ),
            QuizQuestion(
                id = "q2",
                quizId = "quiz_1",
                questionText = "What is 3 x 3?",
                options = listOf("6", "9", "12", "15"),
                correctAnswerIndex = 1,
                explanation = "3 x 3 = 9",
                orderIndex = 1
            )
        )
    )

    @Before
    fun setup() {
        quizRepository = mockk()
        getQuizByIdUseCase = GetQuizByIdUseCase(quizRepository)
        submitQuizAttemptUseCase = SubmitQuizAttemptUseCase(quizRepository)
        generateAiQuizUseCase = GenerateAiQuizUseCase(quizRepository)
    }

    @Test
    fun `getQuizById returns quiz successfully`() = runTest {
        // Given
        coEvery { quizRepository.getQuizById("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        
        // When
        val result = getQuizByIdUseCase("quiz_1").first()
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals("quiz_1", (result as Resource.Success).data.id)
        assertEquals("Test Quiz", result.data.title)
    }

    @Test
    fun `getQuizById returns error when quiz not found`() = runTest {
        // Given
        coEvery { quizRepository.getQuizById("invalid_id") } returns 
            flowOf(Resource.Error("Quiz not found"))
        
        // When
        val result = getQuizByIdUseCase("invalid_id").first()
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Quiz not found", (result as Resource.Error).message)
    }

    @Test
    fun `submitQuizAttempt calculates score correctly`() = runTest {
        // Given
        val expectedAttempt = QuizAttempt(
            id = "attempt_1",
            userId = "user_1",
            quizId = "quiz_1",
            subject = Subject.MATH,
            totalQuestions = 5,
            correctAnswers = 4,
            scorePercentage = 80f,
            timeTakenSeconds = 300,
            weakTopics = emptyList(),
            answers = listOf(1, 1, 0, 1, 1),
            isCompleted = true
        )
        
        coEvery { 
            quizRepository.submitQuizAttempt("quiz_1", any(), 300) 
        } returns Resource.Success(expectedAttempt)
        
        // When
        val answers = listOf(1, 1, 0, 1, 1)
        val result = submitQuizAttemptUseCase("quiz_1", answers, 300)
        
        // Then
        assertTrue(result is Resource.Success)
        val attempt = (result as Resource.Success).data
        assertEquals(4, attempt.correctAnswers)
        assertEquals(80f, attempt.scorePercentage)
        assertTrue(attempt.isPassing)
    }

    @Test
    fun `generateAiQuiz creates quiz with correct parameters`() = runTest {
        // Given
        val aiQuiz = testQuiz.copy(id = "ai_quiz_1", isAiGenerated = true)
        coEvery { 
            quizRepository.generateAiQuiz("chapter_1", 10, QuizDifficulty.MEDIUM) 
        } returns Resource.Success(aiQuiz)
        
        // When
        val result = generateAiQuizUseCase("chapter_1", 10, QuizDifficulty.MEDIUM)
        
        // Then
        assertTrue(result is Resource.Success)
        val quiz = (result as Resource.Success).data
        assertTrue(quiz.isAiGenerated)
        
        coVerify { quizRepository.generateAiQuiz("chapter_1", 10, QuizDifficulty.MEDIUM) }
    }

    @Test
    fun `quiz attempt grade is calculated correctly`() {
        // Given
        val excellentAttempt = createAttempt(scorePercentage = 95f)
        val goodAttempt = createAttempt(scorePercentage = 78f)
        val averageAttempt = createAttempt(scorePercentage = 65f)
        val needsImprovementAttempt = createAttempt(scorePercentage = 45f)
        val poorAttempt = createAttempt(scorePercentage = 30f)
        
        // Then
        assertEquals(com.hdaf.eduapp.domain.model.QuizGrade.EXCELLENT, excellentAttempt.grade)
        assertEquals(com.hdaf.eduapp.domain.model.QuizGrade.GOOD, goodAttempt.grade)
        assertEquals(com.hdaf.eduapp.domain.model.QuizGrade.AVERAGE, averageAttempt.grade)
        assertEquals(com.hdaf.eduapp.domain.model.QuizGrade.NEEDS_IMPROVEMENT, needsImprovementAttempt.grade)
        assertEquals(com.hdaf.eduapp.domain.model.QuizGrade.POOR, poorAttempt.grade)
    }

    @Test
    fun `quiz question isCorrect returns true for correct answer`() {
        // Given
        val question = testQuiz.questions[0]
        
        // Then
        assertTrue(question.isCorrect(1))
        assertTrue(!question.isCorrect(0))
        assertTrue(!question.isCorrect(2))
    }

    private fun createAttempt(scorePercentage: Float) = QuizAttempt(
        id = "test",
        userId = "user",
        quizId = "quiz",
        subject = Subject.MATH,
        totalQuestions = 10,
        correctAnswers = (scorePercentage / 10).toInt(),
        scorePercentage = scorePercentage,
        timeTakenSeconds = 300,
        weakTopics = emptyList(),
        answers = emptyList()
    )
}
