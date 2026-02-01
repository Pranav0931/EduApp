package com.hdaf.eduapp.accessibility

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.VoiceCommandDao
import com.hdaf.eduapp.data.local.entity.VoiceCommandEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice Navigation Manager for blind user support.
 * Enables hands-free navigation via voice commands.
 */
@Singleton
class VoiceNavigationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsEngine: TTSEngine,
    private val voiceCommandDao: VoiceCommandDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private val _listeningState = MutableStateFlow(ListeningState.IDLE)
    val listeningState: StateFlow<ListeningState> = _listeningState.asStateFlow()
    
    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()
    
    private val _navigationCommand = MutableSharedFlow<NavigationCommand>()
    val navigationCommand: SharedFlow<NavigationCommand> = _navigationCommand.asSharedFlow()
    
    private val _voiceError = MutableSharedFlow<VoiceError>()
    val voiceError: SharedFlow<VoiceError> = _voiceError.asSharedFlow()
    
    // Command patterns for navigation
    private val commandPatterns = mapOf(
        // Navigation commands
        listOf("go home", "home", "main menu", "home screen") to NavigationCommand.GoHome,
        listOf("go back", "back", "previous", "return") to NavigationCommand.GoBack,
        listOf("next", "forward", "continue") to NavigationCommand.Next,
        listOf("previous", "back lesson") to NavigationCommand.Previous,
        
        // Content commands
        listOf("read", "read content", "read lesson", "read text") to NavigationCommand.ReadContent,
        listOf("stop", "stop reading", "pause", "quiet") to NavigationCommand.StopReading,
        listOf("repeat", "say again", "read again") to NavigationCommand.Repeat,
        listOf("slower", "slow down", "speak slower") to NavigationCommand.SpeakSlower,
        listOf("faster", "speed up", "speak faster") to NavigationCommand.SpeakFaster,
        
        // Learning commands
        listOf("start quiz", "quiz", "test me", "take quiz") to NavigationCommand.StartQuiz,
        listOf("submit", "submit answer", "done") to NavigationCommand.SubmitAnswer,
        listOf("skip", "skip question", "next question") to NavigationCommand.SkipQuestion,
        listOf("help", "help me", "what can i say") to NavigationCommand.Help,
        
        // Subject navigation
        listOf("open math", "math", "mathematics") to NavigationCommand.OpenSubject("math"),
        listOf("open science", "science") to NavigationCommand.OpenSubject("science"),
        listOf("open english", "english") to NavigationCommand.OpenSubject("english"),
        listOf("open hindi", "hindi") to NavigationCommand.OpenSubject("hindi"),
        listOf("open history", "history") to NavigationCommand.OpenSubject("history"),
        listOf("open geography", "geography") to NavigationCommand.OpenSubject("geography"),
        
        // Answer selection for quizzes
        listOf("option a", "select a", "answer a", "first option") to NavigationCommand.SelectOption(0),
        listOf("option b", "select b", "answer b", "second option") to NavigationCommand.SelectOption(1),
        listOf("option c", "select c", "answer c", "third option") to NavigationCommand.SelectOption(2),
        listOf("option d", "select d", "answer d", "fourth option") to NavigationCommand.SelectOption(3),
        
        // Profile and settings
        listOf("my profile", "profile", "settings") to NavigationCommand.OpenProfile,
        listOf("my progress", "progress", "how am i doing") to NavigationCommand.OpenProgress,
        listOf("achievements", "badges", "rewards") to NavigationCommand.OpenAchievements,
    )
    
    /**
     * Check if voice recognition is available.
     */
    fun isVoiceRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Check if microphone permission is granted.
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Initialize voice recognition.
     */
    fun initialize() {
        if (!isVoiceRecognitionAvailable()) {
            Timber.w("Voice recognition not available on this device")
            return
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(createRecognitionListener())
    }
    
    /**
     * Start listening for voice commands.
     */
    fun startListening() {
        if (!hasMicrophonePermission()) {
            scope.launch {
                _voiceError.emit(VoiceError.PermissionDenied)
            }
            return
        }
        
        if (!isVoiceRecognitionAvailable()) {
            scope.launch {
                _voiceError.emit(VoiceError.NotAvailable)
            }
            return
        }
        
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            _listeningState.value = ListeningState.LISTENING
            ttsEngine.provideHapticFeedback(HapticType.CLICK)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start voice recognition")
            scope.launch {
                _voiceError.emit(VoiceError.RecognitionFailed(e.message))
            }
        }
    }
    
    /**
     * Stop listening.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        _listeningState.value = ListeningState.IDLE
    }
    
    /**
     * Process recognized text and emit navigation command.
     */
    private fun processRecognizedText(text: String) {
        val normalizedText = text.lowercase().trim()
        _recognizedText.value = normalizedText
        
        // Save command to history
        scope.launch {
            saveCommandToHistory(normalizedText)
        }
        
        // Find matching command
        val command = findMatchingCommand(normalizedText)
        
        if (command != null) {
            scope.launch {
                _navigationCommand.emit(command)
                ttsEngine.provideHapticFeedback(HapticType.SUCCESS)
            }
        } else {
            // Check for dynamic commands (numbers, custom phrases)
            val dynamicCommand = parseDynamicCommand(normalizedText)
            if (dynamicCommand != null) {
                scope.launch {
                    _navigationCommand.emit(dynamicCommand)
                    ttsEngine.provideHapticFeedback(HapticType.SUCCESS)
                }
            } else {
                scope.launch {
                    _voiceError.emit(VoiceError.CommandNotRecognized(normalizedText))
                    ttsEngine.provideHapticFeedback(HapticType.WARNING)
                }
            }
        }
    }
    
    /**
     * Find matching command from patterns.
     */
    private fun findMatchingCommand(text: String): NavigationCommand? {
        for ((patterns, command) in commandPatterns) {
            if (patterns.any { pattern -> text.contains(pattern) || pattern.contains(text) }) {
                return command
            }
        }
        return null
    }
    
    /**
     * Parse dynamic commands like numbers or lesson names.
     */
    private fun parseDynamicCommand(text: String): NavigationCommand? {
        // Check for "open lesson X" or "go to lesson X"
        val lessonRegex = Regex("(open|go to|start)\\s+(lesson|chapter)\\s+(\\d+)")
        lessonRegex.find(text)?.let { match ->
            val lessonNumber = match.groupValues[3].toIntOrNull()
            if (lessonNumber != null) {
                return NavigationCommand.OpenLesson(lessonNumber)
            }
        }
        
        // Check for "open class X" or "go to class X"
        val classRegex = Regex("(open|go to|select)\\s+class\\s+(\\d+)")
        classRegex.find(text)?.let { match ->
            val classNumber = match.groupValues[2].toIntOrNull()
            if (classNumber != null && classNumber in 1..10) {
                return NavigationCommand.OpenClass(classNumber)
            }
        }
        
        // Check for just a number (for quiz answers)
        val numberRegex = Regex("^(one|two|three|four|1|2|3|4)$")
        numberRegex.find(text)?.let { match ->
            val optionIndex = when (match.value) {
                "one", "1" -> 0
                "two", "2" -> 1
                "three", "3" -> 2
                "four", "4" -> 3
                else -> null
            }
            if (optionIndex != null) {
                return NavigationCommand.SelectOption(optionIndex)
            }
        }
        
        return null
    }
    
    /**
     * Save command to history for analytics.
     */
    private suspend fun saveCommandToHistory(commandText: String) {
        try {
            voiceCommandDao.insertCommand(
                VoiceCommandEntity(
                    userId = "current_user", // TODO: Get from session
                    commandText = commandText,
                    recognizedAction = commandText,
                    wasSuccessful = true,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to save voice command")
        }
    }
    
    /**
     * Get help text for available commands.
     */
    fun getHelpText(): String {
        return """
            Available voice commands:
            
            Navigation:
            - "Go home" or "Main menu" - Go to home screen
            - "Go back" - Return to previous screen
            - "Next" or "Continue" - Go to next item
            
            Reading:
            - "Read content" - Read the current lesson
            - "Stop" or "Pause" - Stop reading
            - "Repeat" - Read again
            - "Slower" or "Faster" - Adjust speech speed
            
            Learning:
            - "Start quiz" - Begin a quiz
            - "Option A/B/C/D" - Select an answer
            - "Submit" - Submit your answer
            - "Skip" - Skip current question
            
            Subjects:
            - "Open Math/Science/English" - Go to subject
            - "Open class 5" - Switch to a class
            - "Open lesson 3" - Go to specific lesson
            
            Other:
            - "My profile" - Open profile
            - "My progress" - View progress
            - "Help" - Hear these commands
        """.trimIndent()
    }
    
    /**
     * Create recognition listener.
     */
    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _listeningState.value = ListeningState.READY
        }
        
        override fun onBeginningOfSpeech() {
            _listeningState.value = ListeningState.LISTENING
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could show visual feedback
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            _listeningState.value = ListeningState.PROCESSING
            isListening = false
        }
        
        override fun onError(error: Int) {
            isListening = false
            _listeningState.value = ListeningState.IDLE
            
            val voiceError = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> VoiceError.AudioError
                SpeechRecognizer.ERROR_CLIENT -> VoiceError.ClientError
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceError.PermissionDenied
                SpeechRecognizer.ERROR_NETWORK -> VoiceError.NetworkError
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceError.NetworkTimeout
                SpeechRecognizer.ERROR_NO_MATCH -> VoiceError.NoMatch
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceError.RecognizerBusy
                SpeechRecognizer.ERROR_SERVER -> VoiceError.ServerError
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError.SpeechTimeout
                else -> VoiceError.Unknown(error)
            }
            
            scope.launch {
                _voiceError.emit(voiceError)
            }
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestMatch = matches?.firstOrNull()
            
            if (bestMatch != null) {
                processRecognizedText(bestMatch)
            }
            
            _listeningState.value = ListeningState.IDLE
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull()
            if (partialText != null) {
                _recognizedText.value = partialText
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    /**
     * Release resources.
     */
    fun release() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

/**
 * Voice listening states.
 */
enum class ListeningState {
    IDLE,
    READY,
    LISTENING,
    PROCESSING
}

/**
 * Navigation commands from voice input.
 */
sealed class NavigationCommand {
    // Navigation
    data object GoHome : NavigationCommand()
    data object GoBack : NavigationCommand()
    data object Next : NavigationCommand()
    data object Previous : NavigationCommand()
    
    // Content
    data object ReadContent : NavigationCommand()
    data object StopReading : NavigationCommand()
    data object Repeat : NavigationCommand()
    data object SpeakSlower : NavigationCommand()
    data object SpeakFaster : NavigationCommand()
    
    // Learning
    data object StartQuiz : NavigationCommand()
    data object SubmitAnswer : NavigationCommand()
    data object SkipQuestion : NavigationCommand()
    data object Help : NavigationCommand()
    
    // Dynamic navigation
    data class OpenSubject(val subjectId: String) : NavigationCommand()
    data class OpenLesson(val lessonNumber: Int) : NavigationCommand()
    data class OpenClass(val classNumber: Int) : NavigationCommand()
    data class SelectOption(val optionIndex: Int) : NavigationCommand()
    
    // Profile
    data object OpenProfile : NavigationCommand()
    data object OpenProgress : NavigationCommand()
    data object OpenAchievements : NavigationCommand()
}

/**
 * Voice recognition errors.
 */
sealed class VoiceError {
    data object PermissionDenied : VoiceError()
    data object NotAvailable : VoiceError()
    data object AudioError : VoiceError()
    data object ClientError : VoiceError()
    data object NetworkError : VoiceError()
    data object NetworkTimeout : VoiceError()
    data object NoMatch : VoiceError()
    data object RecognizerBusy : VoiceError()
    data object ServerError : VoiceError()
    data object SpeechTimeout : VoiceError()
    data class Unknown(val errorCode: Int) : VoiceError()
    data class RecognitionFailed(val message: String?) : VoiceError()
    data class CommandNotRecognized(val text: String) : VoiceError()
}
