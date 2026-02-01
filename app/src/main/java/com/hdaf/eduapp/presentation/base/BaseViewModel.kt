package com.hdaf.eduapp.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base ViewModel class providing common functionality for all ViewModels.
 * 
 * Features:
 * - State management with StateFlow
 * - One-time events with SharedFlow
 * - Safe coroutine launching with error handling
 * - Loading state management
 * 
 * @param S The UI state type
 * @param E The UI event type (one-time events like navigation, toasts)
 */
abstract class BaseViewModel<S : UiState, E : UiEvent>(
    initialState: S
) : ViewModel() {

    // ==================== State Management ====================
    
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()
    
    /**
     * Current UI state value.
     */
    protected val currentState: S get() = _uiState.value
    
    /**
     * Update the UI state.
     */
    protected fun setState(reduce: S.() -> S) {
        _uiState.value = currentState.reduce()
    }

    // ==================== Events (One-time) ====================
    
    private val _events = MutableSharedFlow<E>()
    val events: SharedFlow<E> = _events.asSharedFlow()
    
    /**
     * Send a one-time event to the UI.
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    // ==================== Error Handling ====================
    
    private val _error = MutableSharedFlow<ErrorState>()
    val error: SharedFlow<ErrorState> = _error.asSharedFlow()
    
    /**
     * Global exception handler for coroutines.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unhandled exception in ViewModel")
        viewModelScope.launch {
            _error.emit(ErrorState(
                message = throwable.message ?: "An unexpected error occurred",
                throwable = throwable
            ))
        }
    }
    
    /**
     * Launch a coroutine with error handling.
     */
    protected fun safeLaunch(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "Error in safeLaunch")
                onError?.invoke(e) ?: _error.emit(ErrorState(
                    message = e.message ?: "An error occurred",
                    throwable = e
                ))
            }
        }
    }

    // ==================== Loading State ====================
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Set loading state.
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * Execute a block with loading state management.
     */
    protected suspend fun <T> withLoading(block: suspend () -> T): T {
        setLoading(true)
        return try {
            block()
        } finally {
            setLoading(false)
        }
    }
}

/**
 * Marker interface for UI states.
 * 
 * UI states represent the current state of the screen and should be:
 * - Immutable (use data class)
 * - Complete (contain all data needed to render the UI)
 * - Serializable (for state restoration)
 */
interface UiState

/**
 * Marker interface for UI events.
 * 
 * UI events are one-time actions like:
 * - Navigation
 * - Showing toasts/snackbars
 * - Triggering animations
 */
interface UiEvent

/**
 * Represents an error state.
 */
data class ErrorState(
    val message: String,
    val throwable: Throwable? = null,
    val retryAction: (() -> Unit)? = null
)

/**
 * Empty state for screens without complex state.
 */
object EmptyState : UiState

/**
 * Empty event for screens without events.
 */
object EmptyEvent : UiEvent
