package com.hdaf.eduapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Splash screen.
 * Checks auth state and determines navigation destination.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isUserLoggedIn()
                val hasCompletedOnboarding = authRepository.hasCompletedOnboarding()
                
                _navigationState.value = when {
                    !hasCompletedOnboarding -> SplashNavigationState.Onboarding
                    isLoggedIn -> SplashNavigationState.Home
                    else -> SplashNavigationState.Onboarding
                }
            } catch (e: Exception) {
                // Default to onboarding on error
                _navigationState.value = SplashNavigationState.Onboarding
            }
        }
    }
}

sealed interface SplashNavigationState {
    data object Loading : SplashNavigationState
    data object Onboarding : SplashNavigationState
    data object Home : SplashNavigationState
}
