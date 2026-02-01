package com.hdaf.eduapp.presentation.mode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for mode selection screen.
 */
@HiltViewModel
class ModeSelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = Channel<ModeSelectionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun selectAudioMode() {
        viewModelScope.launch {
            authRepository.saveAccessibilityMode("AUDIO")
            _events.send(ModeSelectionEvent.NavigateToPhoneInput)
        }
    }

    fun selectVideoMode() {
        viewModelScope.launch {
            authRepository.saveAccessibilityMode("VIDEO")
            _events.send(ModeSelectionEvent.NavigateToPhoneInput)
        }
    }
}

sealed interface ModeSelectionEvent {
    data object NavigateToPhoneInput : ModeSelectionEvent
}
