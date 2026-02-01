package com.hdaf.eduapp.presentation.quiz

import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizGrade
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.presentation.base.UiEvent
import com.hdaf.eduapp.presentation.base.UiState

/**
 * UI State for Quiz screens.
 */
data class QuizUiState(
    val isLoading: Boolean = true,
    val quiz: Quiz? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val timeRemainingSeconds: Int = 0,
    val isTimerPaused: Boolean = false,
    val quizPhase: QuizPhase = QuizPhase.LOADING,
    val error: String? = null
) : UiState {
    val currentQuestion: QuizQuestion?
        get() = quiz?.questions?.getOrNull(currentQuestionIndex)
    
    val totalQuestions: Int
        get() = quiz?.totalQuestions ?: 0
    
    val answeredCount: Int
        get() = selectedAnswers.size
    
    val progressPercentage: Float
        get() = if (totalQuestions > 0) answeredCount.toFloat() / totalQuestions else 0f
    
    val isLastQuestion: Boolean
        get() = currentQuestionIndex >= (totalQuestions - 1)
    
    val canSubmit: Boolean
        get() = answeredCount == totalQuestions
    
    val timeRemainingFormatted: String
        get() {
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

enum class QuizPhase {
    LOADING,
    INSTRUCTIONS,
    IN_PROGRESS,
    REVIEW,
    SUBMITTED,
    RESULT
}

/**
 * Result state after quiz completion.
 */
data class QuizResultState(
    val attempt: QuizAttempt? = null,
    val quiz: Quiz? = null,
    val isLoading: Boolean = true,
    val grade: QuizGrade = QuizGrade.AVERAGE,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val scorePercentage: Float = 0f,
    val xpEarned: Int = 0,
    val newBadges: List<String> = emptyList(),
    val weakTopics: List<String> = emptyList(),
    val timeTaken: String = "",
    val showCelebration: Boolean = false,
    val error: String? = null
)

/**
 * Events from Quiz ViewModel.
 */
sealed class QuizUiEvent : UiEvent {
    data class ShowSnackbar(val message: String) : QuizUiEvent()
    data class SpeakQuestion(val question: String, val options: List<String>) : QuizUiEvent()
    data class SpeakMessage(val message: String) : QuizUiEvent()
    data object NavigateToResult : QuizUiEvent()
    data object NavigateBack : QuizUiEvent()
    data object ShowTimeWarning : QuizUiEvent()
    data object TimeUp : QuizUiEvent()
    data class ShowBadgeEarned(val badgeName: String) : QuizUiEvent()
    data object PlayCorrectSound : QuizUiEvent()
    data object PlayIncorrectSound : QuizUiEvent()
    data object HapticSuccess : QuizUiEvent()
    data object HapticError : QuizUiEvent()
}

/**
 * Actions from Quiz UI.
 */
sealed class QuizAction {
    // Quiz Loading
    data class LoadQuiz(val quizId: String) : QuizAction()
    data class GenerateAiQuiz(val chapterId: String) : QuizAction()
    
    // Quiz Flow
    data object StartQuiz : QuizAction()
    data class SelectAnswer(val optionIndex: Int) : QuizAction()
    data object NextQuestion : QuizAction()
    data object PreviousQuestion : QuizAction()
    data class JumpToQuestion(val index: Int) : QuizAction()
    
    // Timer
    data object PauseTimer : QuizAction()
    data object ResumeTimer : QuizAction()
    
    // Submission
    data object ReviewAnswers : QuizAction()
    data object SubmitQuiz : QuizAction()
    data object ConfirmSubmit : QuizAction()
    
    // Accessibility
    data object ReadCurrentQuestion : QuizAction()
    data object RepeatOptions : QuizAction()
    
    // Navigation
    data object ExitQuiz : QuizAction()
    data object RetryQuiz : QuizAction()
    data object GoToNextChapter : QuizAction()
    data object ShareResult : QuizAction()
    data object DismissError : QuizAction()
}
