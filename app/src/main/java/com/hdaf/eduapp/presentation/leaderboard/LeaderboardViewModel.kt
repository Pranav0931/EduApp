package com.hdaf.eduapp.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.usecase.gamification.GetLeaderboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var currentFilter = LeaderboardFilter.WEEKLY

    fun loadLeaderboard(filter: LeaderboardFilter = LeaderboardFilter.WEEKLY) {
        currentFilter = filter
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getLeaderboardUseCase(filter.value).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val entriesData = result.data ?: emptyList()
                        val entries = entriesData.map { data ->
                            LeaderboardEntry(
                                rank = data.rank,
                                userId = data.userId,
                                userName = data.userName,
                                avatarUrl = data.avatarUrl ?: "",
                                xp = data.xp,
                                level = data.level,
                                isCurrentUser = data.isCurrentUser
                            )
                        }
                        val currentUser = entries.find { it.isCurrentUser }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                entries = entries,
                                currentUserEntry = currentUser,
                                filter = filter
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun refreshLeaderboard() {
        loadLeaderboard(currentFilter)
    }
}

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUserEntry: LeaderboardEntry? = null,
    val filter: LeaderboardFilter = LeaderboardFilter.WEEKLY,
    val error: String? = null
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatarUrl: String,
    val xp: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

enum class LeaderboardFilter(val value: String) {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    ALL_TIME("all_time")
}
