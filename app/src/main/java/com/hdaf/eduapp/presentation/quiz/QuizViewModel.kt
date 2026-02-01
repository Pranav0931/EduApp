package com.hdaf.eduapp.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizGrade
import com.hdaf.eduapp.domain.usecase.progress.AddXpUseCase
import com.hdaf.eduapp.domain.usecase.progress.CheckAndAwardBadgesUseCase
import com.hdaf.eduapp.domain.usecase.quiz.GenerateAiQuizUseCase
import com.hdaf.eduapp.domain.usecase.quiz.GetQuizByIdUseCase
import com.hdaf.eduapp.domain.usecase.quiz.SubmitQuizAttemptUseCase
import com.hdaf.eduapp.domain.repository.XpSource
import com.hdaf.eduapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Quiz feature.
 * Handles quiz loading, answering, timing, and submission.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getQuizByIdUseCase: GetQuizByIdUseCase,
    private val generateAiQuizUseCase: GenerateAiQuizUseCase,
    private val submitQuizAttemptUseCase: SubmitQuizAttemptUseCase,
    private val addXpUseCase: AddXpUseCase,
    private val checkAndAwardBadgesUseCase: CheckAndAwardBadgesUseCase,
    private val accessibilityManager: EduAccessibilityManager
) : BaseViewModel<QuizUiState, QuizUiEvent>(QuizUiState()) {

    private val _uiState = MutableStateFlow(QuizUiState())
    val quizUiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _resultState = MutableStateFlow(QuizResultState())
    val resultState: StateFlow<QuizResultState> = _resultState.asStateFlow()

    private var timerJob: Job? = null
    private var quizStartTime: Long = 0

    init {
        // Check if quiz ID was passed
        savedStateHandle.get<String>("quizId")?.let { quizId ->
            loadQuiz(quizId)
        }
        
        savedStateHandle.get<String>("chapterId")?.let { chapterId ->
            generateAiQuiz(chapterId)
        }
    }

    fun onAction(action: QuizAction) {
        when (action) {
            is QuizAction.LoadQuiz -> loadQuiz(action.quizId)
            is QuizAction.GenerateAiQuiz -> generateAiQuiz(action.chapterId)
            is QuizAction.StartQuiz -> startQuiz()
            is QuizAction.SelectAnswer -> selectOption(action.optionIndex)
            is QuizAction.NextQuestion -> nextQuestion()
            is QuizAction.PreviousQuestion -> previousQuestion()
            is QuizAction.JumpToQuestion -> jumpToQuestion(action.index)
            is QuizAction.PauseTimer -> pauseTimer()
            is QuizAction.ResumeTimer -> resumeTimer()
            is QuizAction.ReviewAnswers -> reviewAnswers()
            is QuizAction.SubmitQuiz -> showSubmitConfirmation()
            is QuizAction.ConfirmSubmit -> submitQuiz()
            is QuizAction.ReadCurrentQuestion -> readCurrentQuestion()
            is QuizAction.RepeatOptions -> repeatOptions()
            is QuizAction.ExitQuiz -> exitQuiz()
            is QuizAction.RetryQuiz -> retryQuiz()
            is QuizAction.GoToNextChapter -> goToNextChapter()
            is QuizAction.ShareResult -> shareResult()
            is QuizAction.DismissError -> dismissError()
        }
    }

    // ==================== Loading ====================

    private fun loadQuiz(quizId: String) {
        getQuizByIdUseCase(quizId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val quiz = resource.data
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                quiz = quiz,
                                timeRemainingSeconds = quiz.timeLimitMinutes * 60,
                                quizPhase = QuizPhase.INSTRUCTIONS
                            )
                        }
                        announceQuizLoaded(quiz)
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "प्रश्नोत्तरी लोड करने में विफल"
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun generateAiQuiz(chapterId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = generateAiQuizUseCase(chapterId, 10, QuizDifficulty.MEDIUM)) {
                is Resource.Success -> {
                    val quiz = result.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            quiz = quiz,
                            timeRemainingSeconds = quiz.timeLimitMinutes * 60,
                            quizPhase = QuizPhase.INSTRUCTIONS
                        )
                    }
                    announceQuizLoaded(quiz)
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "AI प्रश्नोत्तरी बनाने में विफल"
                        )
                    }
                }
                is Resource.Loading -> { }
            }
        }
    }

    private fun announceQuizLoaded(quiz: Quiz) {
        val message = """
            ${quiz.title} लोड हो गई।
            कुल ${quiz.totalQuestions} प्रश्न हैं।
            समय सीमा ${quiz.timeLimitMinutes} मिनट है।
            शुरू करने के लिए स्टार्ट बटन दबाएं।
        """.trimIndent()
        sendEvent(QuizUiEvent.SpeakMessage(message))
    }

    // ==================== Quiz Flow ====================

    /**
     * Start a quiz for a specific chapter (generates AI quiz)
     */
    fun startQuiz(chapterId: String) {
        generateAiQuiz(chapterId)
    }

    /**
     * Start the loaded quiz
     */
    fun startQuiz() {
        quizStartTime = System.currentTimeMillis()
        _uiState.update { 
            it.copy(
                quizPhase = QuizPhase.IN_PROGRESS,
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap()
            )
        }
        startTimer()
        readCurrentQuestion()
    }

    fun selectOption(optionIndex: Int) {
        val currentIndex = _uiState.value.currentQuestionIndex
        val currentAnswers = _uiState.value.selectedAnswers.toMutableMap()
        currentAnswers[currentIndex] = optionIndex
        
        _uiState.update { it.copy(selectedAnswers = currentAnswers) }
        
        // Haptic feedback
        accessibilityManager.hapticFeedback(HapticType.CLICK)
        
        // Announce selection
        val question = _uiState.value.currentQuestion
        question?.let {
            val selectedOption = it.options.getOrNull(optionIndex) ?: ""
            sendEvent(QuizUiEvent.SpeakMessage("विकल्प ${optionIndex + 1}: $selectedOption चुना गया"))
        }
    }

    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val totalQuestions = _uiState.value.totalQuestions
        
        if (currentIndex < totalQuestions - 1) {
            _uiState.update { it.copy(currentQuestionIndex = currentIndex + 1) }
            readCurrentQuestion()
        } else {
            // Last question - show review option
            sendEvent(QuizUiEvent.SpeakMessage("यह अंतिम प्रश्न है। जमा करने के लिए सबमिट बटन दबाएं।"))
        }
    }

    fun previousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        
        if (currentIndex > 0) {
            _uiState.update { it.copy(currentQuestionIndex = currentIndex - 1) }
            readCurrentQuestion()
        } else {
            sendEvent(QuizUiEvent.SpeakMessage("यह पहला प्रश्न है।"))
        }
    }

    private fun jumpToQuestion(index: Int) {
        val totalQuestions = _uiState.value.totalQuestions
        if (index in 0 until totalQuestions) {
            _uiState.update { it.copy(currentQuestionIndex = index) }
            readCurrentQuestion()
        }
    }

    // ==================== Timer ====================

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && 
                   _uiState.value.quizPhase == QuizPhase.IN_PROGRESS) {
                
                if (!_uiState.value.isTimerPaused) {
                    val remaining = _uiState.value.timeRemainingSeconds - 1
                    _uiState.update { it.copy(timeRemainingSeconds = remaining) }
                    
                    // Warning at 1 minute
                    if (remaining == 60) {
                        sendEvent(QuizUiEvent.ShowTimeWarning)
                        sendEvent(QuizUiEvent.SpeakMessage("केवल 1 मिनट बचा है!"))
                    }
                    
                    // Time up
                    if (remaining <= 0) {
                        sendEvent(QuizUiEvent.TimeUp)
                        sendEvent(QuizUiEvent.SpeakMessage("समय समाप्त! प्रश्नोत्तरी जमा की जा रही है।"))
                        submitQuiz()
                        return@launch
                    }
                }
                delay(1000)
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerPaused = true) }
        sendEvent(QuizUiEvent.SpeakMessage("टाइमर रोका गया"))
    }

    private fun resumeTimer() {
        _uiState.update { it.copy(isTimerPaused = false) }
        sendEvent(QuizUiEvent.SpeakMessage("टाइमर शुरू"))
    }

    // ==================== Submission ====================

    private fun reviewAnswers() {
        _uiState.update { it.copy(quizPhase = QuizPhase.REVIEW) }
        
        val answered = _uiState.value.answeredCount
        val total = _uiState.value.totalQuestions
        val unanswered = total - answered
        
        val message = if (unanswered > 0) {
            "$answered प्रश्नों के उत्तर दिए। $unanswered प्रश्न बाकी हैं।"
        } else {
            "सभी प्रश्नों के उत्तर दिए गए। जमा करने के लिए तैयार।"
        }
        sendEvent(QuizUiEvent.SpeakMessage(message))
    }

    private fun showSubmitConfirmation() {
        val unanswered = _uiState.value.totalQuestions - _uiState.value.answeredCount
        if (unanswered > 0) {
            sendEvent(QuizUiEvent.SpeakMessage("$unanswered प्रश्न अनुत्तरित हैं। क्या आप सुनिश्चित हैं?"))
        }
        // Show confirmation dialog in UI
    }

    fun submitQuiz() {
        timerJob?.cancel()
        
        val quiz = _uiState.value.quiz ?: return
        val answers = _uiState.value.selectedAnswers
        val timeTaken = ((System.currentTimeMillis() - quizStartTime) / 1000).toInt()
        
        // Convert answers map to list
        val answerList = (0 until quiz.totalQuestions).map { index ->
            answers[index] ?: -1 // -1 for unanswered
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, quizPhase = QuizPhase.SUBMITTED) }
            
            when (val result = submitQuizAttemptUseCase(quiz.id, answerList, timeTaken)) {
                is Resource.Success -> {
                    val attempt = result.data
                    processResult(attempt, quiz)
                }
                is Resource.Error -> {
                    // Calculate locally if server fails
                    val attempt = calculateLocalResult(quiz, answerList, timeTaken)
                    processResult(attempt, quiz)
                }
                is Resource.Loading -> { }
            }
        }
    }

    private suspend fun processResult(attempt: QuizAttempt, quiz: Quiz) {
        // Award XP
        val xpSource = if (attempt.scorePercentage >= 100) XpSource.QUIZ_PERFECT_SCORE 
                       else XpSource.QUIZ_COMPLETED
        val xpResult = addXpUseCase(attempt.earnedXp, xpSource, quiz.id)
        
        // Check for new badges
        val badgeResult = checkAndAwardBadgesUseCase()
        val newBadges = (badgeResult as? Resource.Success)?.data?.map { it.name } ?: emptyList()
        
        // Format time taken
        val minutes = attempt.timeTakenSeconds / 60
        val seconds = attempt.timeTakenSeconds % 60
        val timeFormatted = String.format("%d मिनट %d सेकंड", minutes, seconds)
        
        _resultState.update { 
            it.copy(
                isLoading = false,
                attempt = attempt,
                quiz = quiz,
                grade = attempt.grade,
                correctAnswers = attempt.correctAnswers,
                totalQuestions = attempt.totalQuestions,
                scorePercentage = attempt.scorePercentage,
                xpEarned = attempt.earnedXp,
                newBadges = newBadges,
                weakTopics = attempt.weakTopics,
                timeTaken = timeFormatted,
                showCelebration = attempt.scorePercentage >= 80
            )
        }
        
        _uiState.update { it.copy(isLoading = false, quizPhase = QuizPhase.RESULT) }
        
        // Announce result
        announceResult(attempt)
        
        // Play sounds and haptics
        if (attempt.isPassing) {
            sendEvent(QuizUiEvent.PlayCorrectSound)
            sendEvent(QuizUiEvent.HapticSuccess)
        } else {
            sendEvent(QuizUiEvent.PlayIncorrectSound)
            sendEvent(QuizUiEvent.HapticError)
        }
        
        // Show badges
        newBadges.forEach { badge ->
            sendEvent(QuizUiEvent.ShowBadgeEarned(badge))
        }
        
        sendEvent(QuizUiEvent.NavigateToResult)
    }

    private fun calculateLocalResult(quiz: Quiz, answers: List<Int>, timeTaken: Int): QuizAttempt {
        var correct = 0
        val weakTopics = mutableListOf<String>()
        
        quiz.questions.forEachIndexed { index, question ->
            if (answers.getOrNull(index) == question.correctAnswerIndex) {
                correct++
            } else {
                question.topic?.let { weakTopics.add(it) }
            }
        }
        
        val scorePercentage = (correct.toFloat() / quiz.totalQuestions) * 100
        
        return QuizAttempt(
            id = java.util.UUID.randomUUID().toString(),
            userId = "", // Will be filled by server
            quizId = quiz.id,
            subject = quiz.subject,
            totalQuestions = quiz.totalQuestions,
            correctAnswers = correct,
            scorePercentage = scorePercentage,
            timeTakenSeconds = timeTaken,
            weakTopics = weakTopics.distinct(),
            answers = answers,
            isCompleted = true
        )
    }

    private fun announceResult(attempt: QuizAttempt) {
        val message = """
            प्रश्नोत्तरी पूर्ण!
            आपने ${attempt.totalQuestions} में से ${attempt.correctAnswers} प्रश्न सही किए।
            आपका स्कोर ${attempt.scorePercentage.toInt()} प्रतिशत है।
            ग्रेड: ${attempt.grade.displayName} ${attempt.grade.emoji}
            आपने ${attempt.earnedXp} XP कमाए।
        """.trimIndent()
        sendEvent(QuizUiEvent.SpeakMessage(message))
    }

    // ==================== Accessibility ====================

    private fun readCurrentQuestion() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val questionNumber = state.currentQuestionIndex + 1
        val totalQuestions = state.totalQuestions
        
        val message = buildString {
            append("प्रश्न $questionNumber, $totalQuestions में से। ")
            append(question.questionText)
            append("। विकल्प हैं: ")
            question.options.forEachIndexed { index, option ->
                append("${index + 1}. $option। ")
            }
        }
        
        sendEvent(QuizUiEvent.SpeakQuestion(question.questionText, question.options))
        sendEvent(QuizUiEvent.SpeakMessage(message))
    }

    private fun repeatOptions() {
        val question = _uiState.value.currentQuestion ?: return
        val selectedAnswer = _uiState.value.selectedAnswers[_uiState.value.currentQuestionIndex]
        
        val message = buildString {
            append("विकल्प: ")
            question.options.forEachIndexed { index, option ->
                append("${index + 1}. $option")
                if (selectedAnswer == index) {
                    append(" (चुना गया)")
                }
                append("। ")
            }
        }
        
        sendEvent(QuizUiEvent.SpeakMessage(message))
    }

    // ==================== Navigation ====================

    private fun exitQuiz() {
        timerJob?.cancel()
        sendEvent(QuizUiEvent.NavigateBack)
    }

    private fun retryQuiz() {
        val quizId = _uiState.value.quiz?.id ?: return
        _uiState.update { QuizUiState() }
        _resultState.update { QuizResultState() }
        loadQuiz(quizId)
    }

    private fun goToNextChapter() {
        // Navigate to next chapter
        sendEvent(QuizUiEvent.NavigateBack)
    }

    private fun shareResult() {
        // Share result implementation
        val result = _resultState.value
        sendEvent(QuizUiEvent.ShowSnackbar("परिणाम साझा किया जा रहा है"))
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
