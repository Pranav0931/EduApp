package com.hdaf.eduapp.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.domain.repository.AuthRepository
import com.hdaf.eduapp.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    private val prefManager = PreferenceManager.getInstance(context)

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val hasSelectedLanguage = prefManager.getStringPref("selected_language") != null
                val isLoggedIn = authRepository.isUserLoggedIn()
                val hasCompletedOnboarding = authRepository.hasCompletedOnboarding()
                
                _navigationState.value = when {
                    !hasSelectedLanguage -> SplashNavigationState.LanguageSelection
                    !hasCompletedOnboarding -> SplashNavigationState.Onboarding
                    isLoggedIn -> SplashNavigationState.Home
                    else -> SplashNavigationState.Onboarding
                }
            } catch (e: Exception) {
                // Default to language selection on error
                _navigationState.value = SplashNavigationState.LanguageSelection
            }
        }
    }
}

sealed interface SplashNavigationState {
    data object Loading : SplashNavigationState
    data object LanguageSelection : SplashNavigationState
    data object Onboarding : SplashNavigationState
    data object Home : SplashNavigationState
}
