package com.hdaf.eduapp.presentation.player

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.accessibility.TTSEngine
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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val updateProgressUseCase: UpdateChapterProgressUseCase,
    private val ttsEngine: TTSEngine,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private val _events = Channel<AudioPlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var chapterId: String = ""
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
    }

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
                        val durationSeconds = (chapter?.durationMinutes ?: 0) * 60
                        val currentPos = ((chapter?.readProgress ?: 0f) * durationSeconds).toInt()
                        
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
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                chapterTitle = chapter?.title ?: "",
                                bookTitle = chapter?.bookTitle ?: "",
                                duration = durationSeconds,
                                currentPosition = currentPos,
                                contentAvailable = chapterContent.isNotEmpty()
                            )
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
            // Pause TTS
            ttsEngine.stop()
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
    }

    fun seekBackward() {
        val newPosition = maxOf(
            _uiState.value.currentPosition - 10,
            0
        )
        _uiState.update { it.copy(currentPosition = newPosition) }
    }

    fun seekTo(position: Int) {
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
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
        // Stop current TTS and announce
        ttsEngine.stop()
        val langCode = sharedPreferences.getString("app_language", "en") ?: "en"
        val message = if (langCode == "hi") "अगला अध्याय उपलब्ध नहीं है" else "Next chapter not available"
        ttsEngine.speak(message)
    }

    fun playPrevious() {
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
                    _events.send(AudioPlayerEvent.ChapterCompleted)
                }
            } else {
                _uiState.update { it.copy(currentPosition = newPosition) }
            }
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            val state = _uiState.value
            val progressPercent = if (state.duration > 0) {
                state.currentPosition.toFloat() / state.duration
            } else 0f
            
            updateProgressUseCase(
                chapterId = chapterId,
                progress = progressPercent,
                timeSpentSeconds = state.currentPosition
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsEngine.stop()
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
    val contentAvailable: Boolean = false
)

sealed interface AudioPlayerEvent {
    data class ShowError(val message: String) : AudioPlayerEvent
    data object ChapterCompleted : AudioPlayerEvent
    data class NavigateToQuiz(val chapterId: String) : AudioPlayerEvent
}
