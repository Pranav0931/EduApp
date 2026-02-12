package com.hdaf.eduapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val updateProgressUseCase: UpdateChapterProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    private val _events = Channel<VideoPlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var chapterId: String = ""

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
                        // Check if sign language video is available by checking videoUrl contains "sign"
                        val hasSign = chapter?.videoUrl?.contains("sign", ignoreCase = true) == true
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                chapterTitle = chapter?.title ?: "",
                                bookTitle = chapter?.bookTitle ?: "",
                                duration = durationSeconds,
                                currentPosition = currentPos,
                                hasSignLanguage = hasSign
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(VideoPlayerEvent.ShowError(result.message ?: "Failed to load chapter"))
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        // TODO: Integrate with actual media player (ExoPlayer)
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

    fun toggleSignLanguage() {
        if (_uiState.value.hasSignLanguage) {
            _uiState.update { it.copy(showSignLanguage = !it.showSignLanguage) }
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun updateProgress() {
        if (_uiState.value.isPlaying) {
            val newPosition = _uiState.value.currentPosition + 1
            if (newPosition >= _uiState.value.duration) {
                // Video completed
                _uiState.update { it.copy(currentPosition = _uiState.value.duration, isPlaying = false) }
                viewModelScope.launch {
                    _events.send(VideoPlayerEvent.ChapterCompleted)
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
    
    fun onVideoCompleted() {
        _uiState.update { it.copy(isPlaying = false) }
        viewModelScope.launch {
            _events.send(VideoPlayerEvent.ChapterCompleted)
        }
        saveProgress()
    }
}

data class VideoPlayerUiState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val chapterTitle: String = "",
    val bookTitle: String = "",
    val duration: Int = 0,
    val currentPosition: Int = 0,
    val hasSignLanguage: Boolean = false,
    val showSignLanguage: Boolean = false
)

sealed interface VideoPlayerEvent {
    data class ShowError(val message: String) : VideoPlayerEvent
    data object ChapterCompleted : VideoPlayerEvent
    data class NavigateToQuiz(val chapterId: String) : VideoPlayerEvent
}
