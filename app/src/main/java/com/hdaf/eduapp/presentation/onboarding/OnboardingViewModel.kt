package com.hdaf.eduapp.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Onboarding screen.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setCurrentPage(page: Int) {
        _uiState.update { it.copy(
            currentPage = page,
            isLastPage = page == TOTAL_PAGES - 1
        ) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.setOnboardingCompleted(true)
        }
    }

    companion object {
        const val TOTAL_PAGES = 3
    }
}

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false
)
