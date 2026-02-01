package com.hdaf.eduapp.ui.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.accessibility.AdaptationResult
import com.hdaf.eduapp.accessibility.AdaptiveStudyEngine
import com.hdaf.eduapp.accessibility.AlertType
import com.hdaf.eduapp.accessibility.DeafSupportManager
import com.hdaf.eduapp.accessibility.DifficultyLevel
import com.hdaf.eduapp.accessibility.HapticType
import com.hdaf.eduapp.accessibility.LearningPace
import com.hdaf.eduapp.accessibility.ListeningState
import com.hdaf.eduapp.accessibility.MistakeType
import com.hdaf.eduapp.accessibility.NavigationCommand
import com.hdaf.eduapp.accessibility.OCREngine
import com.hdaf.eduapp.accessibility.OCRResult
import com.hdaf.eduapp.accessibility.SessionSummary
import com.hdaf.eduapp.accessibility.StudyRecommendation
import com.hdaf.eduapp.accessibility.SubtitleData
import com.hdaf.eduapp.accessibility.TTSEngine
import com.hdaf.eduapp.accessibility.VisualAlert
import com.hdaf.eduapp.accessibility.VoiceNavigationManager
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.SpeechRate
import com.hdaf.eduapp.domain.repository.AccessibilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for managing accessibility features across the app.
 */
@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    private val profileRepository: AccessibilityRepository,
    private val ttsEngine: TTSEngine,
    private val ocrEngine: OCREngine,
    private val voiceNavigationManager: VoiceNavigationManager,
    private val deafSupportManager: DeafSupportManager,
    private val adaptiveStudyEngine: AdaptiveStudyEngine
) : ViewModel() {

    // Profile state
    private val _profileState = MutableStateFlow<AccessibilityProfileState>(AccessibilityProfileState.Loading)
    val profileState: StateFlow<AccessibilityProfileState> = _profileState.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<AccessibilityProfile?>(null)
    val currentProfile: StateFlow<AccessibilityProfile?> = _currentProfile.asStateFlow()
    
    // TTS state
    val isSpeaking: StateFlow<Boolean> = ttsEngine.isSpeaking
    val currentUtterance: StateFlow<String?> = ttsEngine.currentUtterance
    
    // Voice navigation state
    val listeningState: StateFlow<ListeningState> = voiceNavigationManager.listeningState
    val recognizedText: StateFlow<String?> = voiceNavigationManager.recognizedText
    
    // Deaf support state
    val currentSubtitle: StateFlow<SubtitleData?> = deafSupportManager.currentSubtitle
    
    // Adaptive learning state
    val currentDifficulty: StateFlow<DifficultyLevel> = adaptiveStudyEngine.currentDifficultyLevel
    val learningPace: StateFlow<LearningPace> = adaptiveStudyEngine.learningPace
    
    // Navigation commands
    private val _navigationEvent = MutableSharedFlow<NavigationCommand>()
    val navigationEvent: SharedFlow<NavigationCommand> = _navigationEvent.asSharedFlow()
    
    // Events
    private val _uiEvent = MutableSharedFlow<AccessibilityUiEvent>()
    val uiEvent: SharedFlow<AccessibilityUiEvent> = _uiEvent.asSharedFlow()
    
    // OCR state
    private val _ocrState = MutableStateFlow<OCRState>(OCRState.Idle)
    val ocrState: StateFlow<OCRState> = _ocrState.asStateFlow()
    
    // Combined UI state
    val accessibilityUiState: StateFlow<AccessibilityUiState> = combine(
        currentProfile,
        isSpeaking,
        listeningState,
        currentSubtitle,
        currentDifficulty
    ) { profile, speaking, listening, subtitle, difficulty ->
        AccessibilityUiState(
            profile = profile,
            isBlindMode = profile?.accessibilityMode == AccessibilityModeType.BLIND,
            isDeafMode = profile?.accessibilityMode == AccessibilityModeType.DEAF,
            isLowVisionMode = profile?.accessibilityMode == AccessibilityModeType.LOW_VISION,
            isSlowLearnerMode = profile?.accessibilityMode == AccessibilityModeType.SLOW_LEARNER,
            isSpeaking = speaking,
            isListening = listening == ListeningState.LISTENING,
            currentSubtitle = subtitle,
            currentDifficulty = difficulty,
            isHighContrast = profile?.contrastLevel != com.hdaf.eduapp.domain.model.ContrastLevel.NORMAL
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccessibilityUiState()
    )
    
    init {
        initializeEngines()
        loadProfile()
        collectNavigationCommands()
        collectVisualAlerts()
    }
    
    private fun initializeEngines() {
        viewModelScope.launch {
            // Initialize TTS
            val ttsReady = ttsEngine.initialize()
            if (!ttsReady) {
                Timber.w("TTS initialization failed")
            }
            
            // Initialize voice navigation
            voiceNavigationManager.initialize()
        }
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = AccessibilityProfileState.Loading
            
            profileRepository.observeProfile("current_user")
                .collect { profile ->
                    if (profile != null) {
                        _currentProfile.value = profile
                        _profileState.value = AccessibilityProfileState.Loaded(profile)
                        applyProfileSettings(profile)
                    } else {
                        // Create default profile if none exists
                        val defaultProfile = AccessibilityProfile(
                            userId = "current_user",
                            accessibilityMode = AccessibilityModeType.NORMAL
                        )
                        _currentProfile.value = defaultProfile
                        _profileState.value = AccessibilityProfileState.Loaded(defaultProfile)
                    }
                }
        }
    }
    
    private fun applyProfileSettings(profile: AccessibilityProfile) {
        ttsEngine.applyProfile(profile)
        deafSupportManager.applyProfile(profile)
        adaptiveStudyEngine.initialize(profile)
    }
    
    private fun collectNavigationCommands() {
        viewModelScope.launch {
            voiceNavigationManager.navigationCommand.collect { command ->
                handleNavigationCommand(command)
            }
        }
    }
    
    private fun collectVisualAlerts() {
        viewModelScope.launch {
            deafSupportManager.visualAlert.collect { alert ->
                _uiEvent.emit(AccessibilityUiEvent.ShowVisualAlert(alert))
            }
        }
    }
    
    private suspend fun handleNavigationCommand(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.Help -> {
                val helpText = voiceNavigationManager.getHelpText()
                speak(helpText)
            }
            is NavigationCommand.StopReading -> {
                stopSpeaking()
            }
            is NavigationCommand.SpeakSlower -> {
                adjustSpeechRate(slower = true)
            }
            is NavigationCommand.SpeakFaster -> {
                adjustSpeechRate(slower = false)
            }
            else -> {
                // Forward navigation command to UI
                _navigationEvent.emit(command)
            }
        }
    }
    
    // === Profile Management ===
    
    fun updateAccessibilityMode(mode: AccessibilityModeType) {
        viewModelScope.launch {
            val userId = _currentProfile.value?.userId ?: "current_user"
            profileRepository.updateAccessibilityMode(userId, mode)
            
            // Provide feedback
            val modeName = when (mode) {
                AccessibilityModeType.NORMAL -> "Normal mode"
                AccessibilityModeType.BLIND -> "Blind mode with voice navigation"
                AccessibilityModeType.DEAF -> "Deaf mode with visual feedback"
                AccessibilityModeType.LOW_VISION -> "Low vision mode with high contrast"
                AccessibilityModeType.SLOW_LEARNER -> "Gentle learning mode"
            }
            
            if (mode == AccessibilityModeType.BLIND) {
                speak("Switched to $modeName")
            } else if (mode == AccessibilityModeType.DEAF) {
                deafSupportManager.showSubtitle("Switched to $modeName")
            }
        }
    }
    
    fun saveProfile(profile: AccessibilityProfile) {
        viewModelScope.launch {
            when (val result = profileRepository.saveProfile(profile)) {
                is Resource.Success -> {
                    _uiEvent.emit(AccessibilityUiEvent.ShowMessage("Settings saved"))
                }
                is Resource.Error -> {
                    _uiEvent.emit(AccessibilityUiEvent.ShowError(result.message ?: "Save failed"))
                }
                else -> {}
            }
        }
    }
    
    // === TTS Functions ===
    
    fun speak(text: String) {
        if (_currentProfile.value?.accessibilityMode == AccessibilityModeType.BLIND ||
            _currentProfile.value?.screenReaderEnabled == true ||
            _currentProfile.value?.autoReadContent == true) {
            ttsEngine.speak(text)
        }
    }
    
    fun speakWithHaptic(text: String, hapticType: HapticType = HapticType.CLICK) {
        ttsEngine.speakWithHaptic(text, hapticType)
    }
    
    fun stopSpeaking() {
        ttsEngine.stop()
    }
    
    fun adjustSpeechRate(slower: Boolean) {
        val currentRate = _currentProfile.value?.speechRate ?: SpeechRate.NORMAL
        val newRate = if (slower) {
            when (currentRate) {
                SpeechRate.VERY_FAST -> SpeechRate.FAST
                SpeechRate.FAST -> SpeechRate.NORMAL
                SpeechRate.NORMAL -> SpeechRate.SLOW
                SpeechRate.SLOW -> SpeechRate.VERY_SLOW
                SpeechRate.VERY_SLOW -> SpeechRate.VERY_SLOW
            }
        } else {
            when (currentRate) {
                SpeechRate.VERY_SLOW -> SpeechRate.SLOW
                SpeechRate.SLOW -> SpeechRate.NORMAL
                SpeechRate.NORMAL -> SpeechRate.FAST
                SpeechRate.FAST -> SpeechRate.VERY_FAST
                SpeechRate.VERY_FAST -> SpeechRate.VERY_FAST
            }
        }
        
        ttsEngine.setSpeechRate(newRate)
        speak("Speech rate: ${newRate.name.lowercase().replace("_", " ")}")
    }
    
    // === Voice Navigation ===
    
    fun startVoiceListening() {
        voiceNavigationManager.startListening()
    }
    
    fun stopVoiceListening() {
        voiceNavigationManager.stopListening()
    }
    
    fun isVoiceNavigationAvailable(): Boolean {
        return voiceNavigationManager.isVoiceRecognitionAvailable() &&
               voiceNavigationManager.hasMicrophonePermission()
    }
    
    // === OCR Functions ===
    
    fun processImageForOCR(imageBytes: ByteArray) {
        viewModelScope.launch {
            _ocrState.value = OCRState.Processing
            
            when (val result = ocrEngine.processImage(imageBytes)) {
                is Resource.Success -> {
                    result.data?.let { ocrResult ->
                        _ocrState.value = OCRState.Success(ocrResult)
                        
                        // Auto-speak for blind users
                        if (_currentProfile.value?.accessibilityMode == AccessibilityModeType.BLIND) {
                            speak(ocrResult.text)
                        }
                    }
                }
                is Resource.Error -> {
                    _ocrState.value = OCRState.Error(result.message ?: "OCR failed")
                    speakWithHaptic("Could not read text from image", HapticType.ERROR)
                }
                else -> {}
            }
        }
    }
    
    // === Deaf Support ===
    
    fun showSubtitle(text: String, speaker: String? = null) {
        deafSupportManager.showSubtitle(text, speaker)
    }
    
    fun hideSubtitle() {
        deafSupportManager.hideSubtitle()
    }
    
    fun alertSuccess(message: String) {
        deafSupportManager.alertSuccess(message)
    }
    
    fun alertError(message: String) {
        deafSupportManager.alertError(message)
    }
    
    // === Adaptive Learning ===
    
    fun startStudySession(subjectId: String, topicId: String) {
        viewModelScope.launch {
            val userId = _currentProfile.value?.userId ?: "current_user"
            adaptiveStudyEngine.startSession(userId, subjectId, topicId)
        }
    }
    
    fun recordQuizAnswer(
        questionId: String,
        isCorrect: Boolean,
        timeSpentMs: Long,
        mistakeType: MistakeType? = null
    ) {
        viewModelScope.launch {
            val result = adaptiveStudyEngine.recordAnswer(
                questionId, isCorrect, timeSpentMs, mistakeType
            )
            
            handleAdaptationResult(result)
        }
    }
    
    private suspend fun handleAdaptationResult(result: AdaptationResult) {
        // Show encouragement for slow learners
        if (result.showEncouragement && result.feedback.isNotEmpty()) {
            _uiEvent.emit(AccessibilityUiEvent.ShowEncouragement(result.feedback))
            
            if (_currentProfile.value?.accessibilityMode == AccessibilityModeType.BLIND) {
                speak(result.feedback)
            }
        }
        
        // Suggest break if needed
        if (result.suggestBreak) {
            _uiEvent.emit(AccessibilityUiEvent.SuggestBreak)
        }
        
        // Suggest review if struggling
        if (result.suggestReview) {
            _uiEvent.emit(AccessibilityUiEvent.SuggestReview)
        }
    }
    
    fun endStudySession() {
        viewModelScope.launch {
            val summary = adaptiveStudyEngine.endSession()
            summary?.let {
                _uiEvent.emit(AccessibilityUiEvent.ShowSessionSummary(it))
            }
        }
    }
    
    fun getStudyRecommendations() {
        viewModelScope.launch {
            val userId = _currentProfile.value?.userId ?: "current_user"
            val recommendations = adaptiveStudyEngine.generateRecommendations(userId)
            _uiEvent.emit(AccessibilityUiEvent.ShowRecommendations(recommendations))
        }
    }
    
    // === Cleanup ===
    
    override fun onCleared() {
        super.onCleared()
        ttsEngine.release()
        voiceNavigationManager.release()
        deafSupportManager.release()
    }
}

/**
 * Profile loading states.
 */
sealed class AccessibilityProfileState {
    data object Loading : AccessibilityProfileState()
    data class Loaded(val profile: AccessibilityProfile) : AccessibilityProfileState()
    data class Error(val message: String) : AccessibilityProfileState()
}

/**
 * OCR processing states.
 */
sealed class OCRState {
    data object Idle : OCRState()
    data object Processing : OCRState()
    data class Success(val result: OCRResult) : OCRState()
    data class Error(val message: String) : OCRState()
}

/**
 * Combined UI state for accessibility.
 */
data class AccessibilityUiState(
    val profile: AccessibilityProfile? = null,
    val isBlindMode: Boolean = false,
    val isDeafMode: Boolean = false,
    val isLowVisionMode: Boolean = false,
    val isSlowLearnerMode: Boolean = false,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val currentSubtitle: SubtitleData? = null,
    val currentDifficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val isHighContrast: Boolean = false
)

/**
 * UI events for accessibility.
 */
sealed class AccessibilityUiEvent {
    data class ShowMessage(val message: String) : AccessibilityUiEvent()
    data class ShowError(val message: String) : AccessibilityUiEvent()
    data class ShowVisualAlert(val alert: VisualAlert) : AccessibilityUiEvent()
    data class ShowEncouragement(val message: String) : AccessibilityUiEvent()
    data object SuggestBreak : AccessibilityUiEvent()
    data object SuggestReview : AccessibilityUiEvent()
    data class ShowSessionSummary(val summary: SessionSummary) : AccessibilityUiEvent()
    data class ShowRecommendations(val recommendations: List<StudyRecommendation>) : AccessibilityUiEvent()
}
