package com.hdaf.eduapp.presentation.quiz

import app.cash.turbine.test
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.domain.model.Subject
import com.hdaf.eduapp.domain.repository.XpResult
import com.hdaf.eduapp.domain.usecase.progress.AddXpUseCase
import com.hdaf.eduapp.domain.usecase.progress.CheckAndAwardBadgesUseCase
import com.hdaf.eduapp.domain.usecase.quiz.GenerateAiQuizUseCase
import com.hdaf.eduapp.domain.usecase.quiz.GetQuizByIdUseCase
import com.hdaf.eduapp.domain.usecase.quiz.SubmitQuizAttemptUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.lifecycle.SavedStateHandle

/**
 * Unit tests for QuizViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: QuizViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getQuizByIdUseCase: GetQuizByIdUseCase
    private lateinit var generateAiQuizUseCase: GenerateAiQuizUseCase
    private lateinit var submitQuizAttemptUseCase: SubmitQuizAttemptUseCase
    private lateinit var addXpUseCase: AddXpUseCase
    private lateinit var checkAndAwardBadgesUseCase: CheckAndAwardBadgesUseCase
    private lateinit var accessibilityManager: EduAccessibilityManager

    private val testQuiz = Quiz(
        id = "quiz_1",
        chapterId = "chapter_1",
        title = "गणित प्रश्नोत्तरी",
        subject = Subject.MATH,
        difficulty = QuizDifficulty.MEDIUM,
        totalQuestions = 3,
        timeLimitMinutes = 5,
        isAiGenerated = false,
        questions = listOf(
            QuizQuestion(
                id = "q1",
                quizId = "quiz_1",
                questionText = "2 + 2 = ?",
                options = listOf("3", "4", "5", "6"),
                correctAnswerIndex = 1,
                orderIndex = 0
            ),
            QuizQuestion(
                id = "q2",
                quizId = "quiz_1",
                questionText = "3 x 3 = ?",
                options = listOf("6", "9", "12", "15"),
                correctAnswerIndex = 1,
                orderIndex = 1
            ),
            QuizQuestion(
                id = "q3",
                quizId = "quiz_1",
                questionText = "10 - 5 = ?",
                options = listOf("3", "4", "5", "6"),
                correctAnswerIndex = 2,
                orderIndex = 2
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        savedStateHandle = SavedStateHandle()
        getQuizByIdUseCase = mockk()
        generateAiQuizUseCase = mockk()
        submitQuizAttemptUseCase = mockk()
        addXpUseCase = mockk()
        checkAndAwardBadgesUseCase = mockk()
        accessibilityManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): QuizViewModel {
        return QuizViewModel(
            savedStateHandle = savedStateHandle,
            getQuizByIdUseCase = getQuizByIdUseCase,
            generateAiQuizUseCase = generateAiQuizUseCase,
            submitQuizAttemptUseCase = submitQuizAttemptUseCase,
            addXpUseCase = addXpUseCase,
            checkAndAwardBadgesUseCase = checkAndAwardBadgesUseCase,
            accessibilityManager = accessibilityManager
        )
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        viewModel = createViewModel()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(QuizPhase.LOADING, state.quizPhase)
    }

    @Test
    fun `load quiz updates state with quiz data`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        
        // When
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testQuiz, state.quiz)
        assertEquals(QuizPhase.INSTRUCTIONS, state.quizPhase)
        assertEquals(300, state.timeRemainingSeconds) // 5 minutes
    }

    @Test
    fun `start quiz changes phase to in progress`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.onAction(QuizAction.StartQuiz)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(QuizPhase.IN_PROGRESS, state.quizPhase)
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun `select answer updates selected answers map`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.StartQuiz)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.onAction(QuizAction.SelectAnswer(1))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.selectedAnswers[0])
        assertEquals(1, state.answeredCount)
        
        // Verify haptic feedback was triggered
        verify { accessibilityManager.hapticFeedback(any()) }
    }

    @Test
    fun `next question increments current index`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.StartQuiz)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.onAction(QuizAction.NextQuestion)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(1, viewModel.uiState.value.currentQuestionIndex)
    }

    @Test
    fun `previous question decrements current index`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.StartQuiz)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.NextQuestion)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.onAction(QuizAction.PreviousQuestion)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(0, viewModel.uiState.value.currentQuestionIndex)
    }

    @Test
    fun `progress percentage calculates correctly`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.StartQuiz)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When - Answer 2 of 3 questions
        viewModel.onAction(QuizAction.SelectAnswer(1))
        viewModel.onAction(QuizAction.NextQuestion)
        viewModel.onAction(QuizAction.SelectAnswer(1))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.answeredCount)
        assertEquals(2f/3f, state.progressPercentage, 0.01f)
    }

    @Test
    fun `submit quiz calculates result correctly`() = runTest {
        // Given
        val expectedAttempt = QuizAttempt(
            id = "attempt_1",
            userId = "user_1",
            quizId = "quiz_1",
            subject = Subject.MATH,
            totalQuestions = 3,
            correctAnswers = 3,
            scorePercentage = 100f,
            timeTakenSeconds = 120,
            weakTopics = emptyList(),
            answers = listOf(1, 1, 2)
        )
        
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        coEvery { submitQuizAttemptUseCase(any(), any(), any()) } returns Resource.Success(expectedAttempt)
        coEvery { addXpUseCase(any(), any(), any()) } returns Resource.Success(
            XpResult(xpEarned = 100, newTotalXp = 500, newLevel = 5, leveledUp = false)
        )
        coEvery { checkAndAwardBadgesUseCase() } returns Resource.Success(emptyList<Badge>())
        
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(QuizAction.StartQuiz)
        
        // Answer all questions correctly
        viewModel.onAction(QuizAction.SelectAnswer(1)) // q1: correct is 1
        viewModel.onAction(QuizAction.NextQuestion)
        viewModel.onAction(QuizAction.SelectAnswer(1)) // q2: correct is 1
        viewModel.onAction(QuizAction.NextQuestion)
        viewModel.onAction(QuizAction.SelectAnswer(2)) // q3: correct is 2
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.onAction(QuizAction.ConfirmSubmit)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val resultState = viewModel.resultState.value
        assertEquals(3, resultState.correctAnswers)
        assertEquals(100f, resultState.scorePercentage, 0.01f)
    }

    @Test
    fun `time remaining formats correctly`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Success(testQuiz))
        viewModel = createViewModel()
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - 5 minutes = 300 seconds = "05:00"
        assertEquals("05:00", viewModel.uiState.value.timeRemainingFormatted)
    }

    @Test
    fun `error state is set when quiz loading fails`() = runTest {
        // Given
        every { getQuizByIdUseCase("quiz_1") } returns flowOf(Resource.Error("Network error"))
        viewModel = createViewModel()
        
        // When
        viewModel.onAction(QuizAction.LoadQuiz("quiz_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }
}
