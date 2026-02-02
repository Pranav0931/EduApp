package com.hdaf.eduapp.presentation.player

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.accessibility.TTSEngine
import com.hdaf.eduapp.core.accessibility.ChapterAudioManager
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.usecase.content.GetChapterDetailUseCase
import com.hdaf.eduapp.domain.usecase.progress.UpdateChapterProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for chapter-wise audio learning.
 * 
 * Key features:
 * - Per-chapter audio state isolation
 * - Resume from last position
 * - TTS-based audio playback
 * - Accessibility mode support
 */
@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val updateProgressUseCase: UpdateChapterProgressUseCase,
    private val ttsEngine: TTSEngine,
    private val sharedPreferences: SharedPreferences,
    private val chapterAudioManager: ChapterAudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private val _events = Channel<AudioPlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var chapterId: String = ""
    private var bookId: String = ""
    private var chapterContent: String = ""

    init {
        // Initialize TTS engine
        viewModelScope.launch {
            val initialized = ttsEngine.initialize()
            if (initialized) {
                // Get language preference from SharedPreferences (default: English)
                val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
                val locale = when (langCode) {
                    "hi" -> Locale("hi", "IN")
                    else -> Locale.ENGLISH
                }
                ttsEngine.setLanguage(locale)
            }
        }
        
        // Observe TTS speaking state
        viewModelScope.launch {
            ttsEngine.isSpeaking.collect { isSpeaking ->
                _uiState.update { it.copy(isPlaying = isSpeaking) }
            }
        }
        
        // Observe chapter audio manager state
        viewModelScope.launch {
            chapterAudioManager.currentPosition.collect { positionMs ->
                _uiState.update { it.copy(currentPosition = (positionMs / 1000).toInt()) }
            }
        }
        
        viewModelScope.launch {
            chapterAudioManager.playbackSpeed.collect { speed ->
                _uiState.update { it.copy(playbackSpeed = speed) }
            }
        }
    }

    /**
     * Load chapter and restore previous playback position.
     */
    fun loadChapter(chapterId: String) {
        this.chapterId = chapterId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getChapterDetailUseCase(chapterId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val chapter = result.data
                        val durationSeconds = (chapter?.durationMinutes ?: 5) * 60
                        
                        // Store book ID for tracking
                        bookId = chapter?.bookId ?: ""
                        
                        // Store chapter content for TTS - use contentText, description, or fallback
                        chapterContent = chapter?.contentText 
                            ?: chapter?.description 
                            ?: "Content is loading. Please wait or try again."
                        
                        // If content is still too short, provide fallback with chapter info
                        if (chapterContent.length < 10) {
                            val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
                            chapterContent = if (langCode == "hi") {
                                "अध्याय ${chapter?.title ?: ""} लोड हो रहा है। कृपया प्रतीक्षा करें।"
                            } else {
                                "Loading chapter ${chapter?.title ?: ""}. Please wait."
                            }
                        }
                        
                        // Load chapter in audio manager and get resume position
                        val resumePositionMs = chapterAudioManager.loadChapter(
                            chapterId = chapterId,
                            chapterTitle = chapter?.title ?: "",
                            bookId = bookId,
                            totalDurationMs = durationSeconds * 1000L
                        )
                        
                        val currentPosSeconds = (resumePositionMs / 1000).toInt()
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                chapterTitle = chapter?.title ?: "",
                                bookTitle = chapter?.bookTitle ?: "",
                                duration = durationSeconds,
                                currentPosition = currentPosSeconds,
                                contentAvailable = chapterContent.isNotEmpty(),
                                hasResumePosition = currentPosSeconds > 0
                            )
                        }
                        
                        // Announce resume position if applicable
                        if (currentPosSeconds > 0) {
                            val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
                            val message = if (langCode == "hi") {
                                "पिछली स्थिति से जारी रखा जा रहा है"
                            } else {
                                "Resuming from previous position"
                            }
                            Timber.d("Resuming chapter from ${currentPosSeconds}s")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(AudioPlayerEvent.ShowError(result.message ?: "अध्याय लोड करने में विफल"))
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            // Pause TTS and save position
            ttsEngine.stop()
            chapterAudioManager.pause()
        } else {
            // Start TTS with content
            if (chapterContent.isNotEmpty()) {
                val speechRate = when (_uiState.value.playbackSpeed) {
                    0.75f -> com.hdaf.eduapp.domain.model.SpeechRate.SLOW
                    1.25f -> com.hdaf.eduapp.domain.model.SpeechRate.FAST
                    1.5f -> com.hdaf.eduapp.domain.model.SpeechRate.VERY_FAST
                    else -> com.hdaf.eduapp.domain.model.SpeechRate.NORMAL
                }
                ttsEngine.setSpeechRate(speechRate)
                ttsEngine.speak(chapterContent)
                chapterAudioManager.play()
                
                // Update UI to show playing state immediately for better UX
                _uiState.update { it.copy(isPlaying = true) }
            } else {
                viewModelScope.launch {
                    val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
                    val errorMsg = if (langCode == "hi") "सामग्री उपलब्ध नहीं है" else "Content not available"
                    _events.send(AudioPlayerEvent.ShowError(errorMsg))
                }
            }
        }
    }

    fun seekForward() {
        val newPosition = minOf(
            _uiState.value.currentPosition + 10,
            _uiState.value.duration
        )
        _uiState.update { it.copy(currentPosition = newPosition) }
        chapterAudioManager.seekTo(newPosition * 1000L)
    }

    fun seekBackward() {
        val newPosition = maxOf(
            _uiState.value.currentPosition - 10,
            0
        )
        _uiState.update { it.copy(currentPosition = newPosition) }
        chapterAudioManager.seekTo(newPosition * 1000L)
    }

    fun seekTo(position: Int) {
        _uiState.update { it.copy(currentPosition = position) }
        chapterAudioManager.seekTo(position * 1000L)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        chapterAudioManager.setPlaybackSpeed(speed)
        
        // Apply speed to TTS
        val speechRate = when (speed) {
            0.75f -> com.hdaf.eduapp.domain.model.SpeechRate.SLOW
            1.25f -> com.hdaf.eduapp.domain.model.SpeechRate.FAST
            1.5f -> com.hdaf.eduapp.domain.model.SpeechRate.VERY_FAST
            else -> com.hdaf.eduapp.domain.model.SpeechRate.NORMAL
        }
        ttsEngine.setSpeechRate(speechRate)
    }

    fun playNext() {
        // Save current progress before moving
        saveProgress()
        
        // Stop current TTS and announce
        ttsEngine.stop()
        val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
        val message = if (langCode == "hi") "अगला अध्याय उपलब्ध नहीं है" else "Next chapter not available"
        ttsEngine.speak(message)
    }

    fun playPrevious() {
        // Save current progress before moving
        saveProgress()
        
        // Stop current TTS and announce
        ttsEngine.stop()
        val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
        val message = if (langCode == "hi") "पिछला अध्याय उपलब्ध नहीं है" else "Previous chapter not available"
        ttsEngine.speak(message)
    }

    fun updateProgress() {
        if (_uiState.value.isPlaying) {
            val newPosition = _uiState.value.currentPosition + 1
            if (newPosition >= _uiState.value.duration) {
                // Chapter completed
                _uiState.update { it.copy(currentPosition = _uiState.value.duration, isPlaying = false) }
                viewModelScope.launch {
                    chapterAudioManager.updatePosition(_uiState.value.duration * 1000L)
                    _events.send(AudioPlayerEvent.ChapterCompleted)
                }
            } else {
                _uiState.update { it.copy(currentPosition = newPosition) }
                // Update manager position (triggers periodic auto-save)
                chapterAudioManager.updatePosition(newPosition * 1000L)
            }
        }
    }

    /**
     * Save current progress to database.
     * Called when pausing, leaving screen, or switching chapters.
     */
    fun saveProgress() {
        viewModelScope.launch {
            val state = _uiState.value
            val progressPercent = if (state.duration > 0) {
                state.currentPosition.toFloat() / state.duration
            } else 0f
            
            // Save via chapter audio manager (persists to Room DB)
            chapterAudioManager.saveProgress(chapterId)
            
            // Also update the general chapter progress
            updateProgressUseCase(
                chapterId = chapterId,
                progress = progressPercent,
                timeSpentSeconds = state.currentPosition
            )
            
            Timber.d("Saved chapter progress: $chapterId at ${state.currentPosition}s")
        }
    }
    
    /**
     * Reset progress and start from beginning.
     */
    fun restartChapter() {
        viewModelScope.launch {
            chapterAudioManager.resetChapterProgress(chapterId)
            _uiState.update { it.copy(currentPosition = 0, hasResumePosition = false) }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsEngine.stop()
        // Save progress when leaving
        viewModelScope.launch {
            chapterAudioManager.saveProgress(chapterId)
        }
    }
}

data class AudioPlayerUiState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val chapterTitle: String = "",
    val bookTitle: String = "",
    val duration: Int = 0,
    val currentPosition: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val contentAvailable: Boolean = false,
    val hasResumePosition: Boolean = false
)

sealed interface AudioPlayerEvent {
    data class ShowError(val message: String) : AudioPlayerEvent
    data object ChapterCompleted : AudioPlayerEvent
    data class NavigateToQuiz(val chapterId: String) : AudioPlayerEvent
}
