package com.hdaf.eduapp.presentation.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.usecase.content.GetChaptersUseCase
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
class ChapterListViewModel @Inject constructor(
    private val getChaptersUseCase: GetChaptersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapterListUiState())
    val uiState: StateFlow<ChapterListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChapterListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadChapters(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getChaptersUseCase(bookId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val chapters = result.data ?: emptyList()
                        val overallProgress = calculateOverallProgress(chapters)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                chapters = chapters,
                                overallProgress = overallProgress
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(ChapterListEvent.ShowError(result.message ?: "Failed to load chapters"))
                    }
                }
            }
        }
    }

    fun refreshChapters(bookId: String) {
        loadChapters(bookId)
    }

    fun downloadChapter(chapter: Chapter) {
        viewModelScope.launch {
            // TODO: Implement actual download
            _events.send(ChapterListEvent.DownloadComplete(chapter.id))
        }
    }

    private fun calculateOverallProgress(chapters: List<Chapter>): Int {
        if (chapters.isEmpty()) return 0
        return chapters.sumOf { it.progress } / chapters.size
    }
}

data class ChapterListUiState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val overallProgress: Int = 0
)

sealed interface ChapterListEvent {
    data class ShowError(val message: String) : ChapterListEvent
    data class DownloadComplete(val chapterId: String) : ChapterListEvent
}
