package com.hdaf.eduapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.usecase.auth.GetCurrentSessionUseCase
import com.hdaf.eduapp.domain.usecase.progress.UpdateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for MainActivity.
 * 
 * Handles:
 * - Initial loading state for splash screen
 * - Session validation
 * - Daily streak updates
 * - Global navigation events
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase,
    private val updateStreakUseCase: UpdateStreakUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            try {
                // Check if user is logged in
                val session = getCurrentSessionUseCase().first()
                
                _isLoggedIn.value = session != null
                
                if (session != null) {
                    // User is logged in - update streak
                    updateDailyStreak()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during initial state check")
                _isLoggedIn.value = false
            } finally {
                // Minimum splash duration for branding
                delay(MIN_SPLASH_DURATION_MS)
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateDailyStreak() {
        try {
            val result = updateStreakUseCase()
            when (result) {
                is Resource.Success -> {
                    val streakStatus = result.data
                    if (streakStatus.streakBroken) {
                        _events.emit(MainUiEvent.ShowSnackbar("आपकी स्ट्रीक टूट गई। फिर से शुरू करें!"))
                    } else if (streakStatus.currentStreak > 1) {
                        _events.emit(MainUiEvent.ShowSnackbar("🔥 ${streakStatus.currentStreak} दिन की स्ट्रीक!"))
                    }
                }
                is Resource.Error -> {
                    Timber.w("Error updating streak: ${result.message}")
                }
                is Resource.Loading -> {}
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating daily streak")
        }
    }

    fun onSessionExpired() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            _events.emit(MainUiEvent.NavigateToAuth)
        }
    }

    fun onLoginSuccess() {
        viewModelScope.launch {
            _isLoggedIn.value = true
            updateDailyStreak()
            _events.emit(MainUiEvent.NavigateToHome)
        }
    }

    companion object {
        private const val MIN_SPLASH_DURATION_MS = 1000L
    }
}
