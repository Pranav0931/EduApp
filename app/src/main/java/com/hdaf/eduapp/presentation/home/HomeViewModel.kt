package com.hdaf.eduapp.presentation.home

import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.domain.usecase.content.GetBooksByClassUseCase
import com.hdaf.eduapp.domain.usecase.content.GetRecentlyReadChaptersUseCase
import com.hdaf.eduapp.domain.usecase.progress.GetStreakInfoUseCase
import com.hdaf.eduapp.domain.usecase.progress.GetTodayXpUseCase
import com.hdaf.eduapp.domain.usecase.progress.GetUserProgressUseCase
import com.hdaf.eduapp.domain.usecase.progress.UpdateStreakUseCase
import com.hdaf.eduapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen following MVVM + UDF pattern.
 * Manages UI state and handles user actions.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProgressUseCase: GetUserProgressUseCase,
    private val getRecentlyReadChaptersUseCase: GetRecentlyReadChaptersUseCase,
    private val getBooksByClassUseCase: GetBooksByClassUseCase,
    private val getStreakInfoUseCase: GetStreakInfoUseCase,
    private val getTodayXpUseCase: GetTodayXpUseCase,
    private val updateStreakUseCase: UpdateStreakUseCase,
    private val networkMonitor: NetworkMonitor
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState()) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
        loadInitialData()
    }

    /**
     * Handle user actions from the UI.
     */
    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.LoadData -> loadInitialData()
            is HomeAction.Refresh -> refreshData()
            is HomeAction.RefreshData -> refreshData()
            is HomeAction.ClearError -> dismissError()
            is HomeAction.SelectBook -> navigateToBook(action.bookId)
            is HomeAction.ContinueReading -> continueReading(action.chapterId)
            is HomeAction.OpenProfile -> sendEvent(HomeUiEvent.NavigateToProfile)
            is HomeAction.OpenLeaderboard -> sendEvent(HomeUiEvent.NavigateToLeaderboard)
            is HomeAction.OpenSettings -> sendEvent(HomeUiEvent.NavigateToSettings)
            is HomeAction.DismissError -> dismissError()
        }
    }

    private fun observeNetworkStatus() {
        networkMonitor.isConnected
            .onEach { isConnected ->
                _uiState.update { it.copy(isOfflineMode = !isConnected) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Update streak on app open
            updateStreakUseCase()

            // Load data in parallel using combine
            combine(
                getUserProgressUseCase(),
                getRecentlyReadChaptersUseCase(limit = 5),
                getTodayXpUseCase(),
                getStreakInfoUseCase()
            ) { progressResource, recentChapters, todayXpResource, streakStatusResource ->
                
                // recentChapters is List<Chapter> directly
                
                val todayXp = when (todayXpResource) {
                    is Resource.Success -> todayXpResource.data
                    else -> 0
                }
                
                val streakDays = when (streakStatusResource) {
                    is Resource.Success -> streakStatusResource.data.currentStreak
                    else -> 0
                }
                
                val streakStatus = when (streakStatusResource) {
                    is Resource.Success -> streakStatusResource.data
                    else -> null
                }
                
                when (progressResource) {
                    is Resource.Success -> {
                        val progress = progressResource.data
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                userName = "", // Would come from user session
                                userClass = 1, // Would come from user session
                                userProgress = progress,
                                recentChapters = recentChapters,
                                streak = streakDays,
                                totalXp = todayXp,
                                error = null
                            )
                        }
                        
                        // Check for streak celebration
                        if (streakStatus != null && streakStatus.currentStreak > 0 && streakStatus.currentStreak % 7 == 0) {
                            sendEvent(HomeUiEvent.ShowStreakCelebration)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                recentChapters = recentChapters,
                                streak = streakDays,
                                totalXp = todayXp,
                                error = progressResource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Keep loading state
                    }
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)

            // Load recommended books based on user's class
            loadRecommendedBooks()
        }
    }

    private fun loadRecommendedBooks() {
        viewModelScope.launch {
            val classId = _uiState.value.userClass.toString()
            getBooksByClassUseCase(classId)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(recommendedBooks = resource.data.take(6)) }
                        }
                        is Resource.Error -> {
                            // Don't override main error, just log
                        }
                        is Resource.Loading -> { /* Ignore */ }
                    }
                }
                .catch { /* Ignore book loading errors */ }
                .launchIn(viewModelScope)
        }
    }

    private fun refreshData() {
        loadInitialData()
        sendEvent(HomeUiEvent.ShowSnackbar("Refreshed"))
    }

    private fun navigateToBook(bookId: String) {
        sendEvent(HomeUiEvent.NavigateToBook(bookId))
    }

    private fun continueReading(chapterId: String) {
        sendEvent(HomeUiEvent.NavigateToChapter(chapterId))
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
