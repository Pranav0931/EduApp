package com.hdaf.eduapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.UserProfile
import com.hdaf.eduapp.domain.model.UserStats
import com.hdaf.eduapp.domain.usecase.user.GetUserBadgesUseCase
import com.hdaf.eduapp.domain.usecase.user.GetUserProfileUseCase
import com.hdaf.eduapp.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserBadgesUseCase: GetUserBadgesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load user profile
            getUserProfileUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(user = result.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load stats
            getUserStatsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(stats = result.data) }
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            // Load badges
            getUserBadgesUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                badges = result.data ?: emptyList()
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val stats: UserStats? = null,
    val badges: List<Badge> = emptyList(),
    val error: String? = null
)
