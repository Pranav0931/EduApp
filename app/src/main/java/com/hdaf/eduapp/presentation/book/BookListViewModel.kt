package com.hdaf.eduapp.presentation.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.usecase.content.GetBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Book List screen.
 * Handles book loading with proper offline-first support and error handling.
 */
@HiltViewModel
class BookListViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    private val _events = Channel<BookListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    
    private var loadBooksJob: Job? = null
    private var currentClassId: String? = null

    /**
     * Load books for a specific class.
     * Implements offline-first pattern with proper loading states.
     */
    fun loadBooks(classId: String) {
        // Cancel any existing job to prevent race conditions
        loadBooksJob?.cancel()
        currentClassId = classId
        
        loadBooksJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedClassId = classId) }
            
            try {
                getBooksUseCase(classId)
                    .onStart { 
                        Timber.d("Starting to load books for class: $classId")
                    }
                    .catch { e ->
                        Timber.e(e, "Error in books flow for class: $classId")
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = e.message ?: "पुस्तकें लोड करने में त्रुटि"
                            )
                        }
                        _events.send(BookListEvent.ShowError(e.message ?: "पुस्तकें लोड करने में विफल"))
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                _uiState.update { it.copy(isLoading = true) }
                            }
                            is Resource.Success -> {
                                val books = result.data ?: emptyList()
                                Timber.d("Loaded ${books.size} books for class: $classId")
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        isRefreshing = false,
                                        books = books,
                                        error = null,
                                        isEmpty = books.isEmpty()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                Timber.e("Error loading books: ${result.message}")
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        isRefreshing = false,
                                        error = result.message
                                    )
                                }
                                _events.send(BookListEvent.ShowError(result.message ?: "पुस्तकें लोड करने में विफल"))
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading books for class: $classId")
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "अप्रत्याशित त्रुटि"
                    )
                }
            }
        }
    }

    /**
     * Refresh books with pull-to-refresh indicator.
     */
    fun refreshBooks(classId: String) {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        loadBooks(classId)
    }
    
    /**
     * Retry loading after an error.
     */
    fun retry() {
        currentClassId?.let { loadBooks(it) }
    }
    
    /**
     * Clear any displayed error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Download a book for offline access.
     */
    fun downloadBook(book: Book) {
        viewModelScope.launch {
            _events.send(BookListEvent.DownloadStarted(book.id))
            // TODO: Implement actual download logic with progress
            try {
                // Simulate download (replace with actual implementation)
                kotlinx.coroutines.delay(1500)
                _events.send(BookListEvent.DownloadComplete(book.id))
            } catch (e: Exception) {
                _events.send(BookListEvent.DownloadFailed(book.id, e.message ?: "डाउनलोड विफल"))
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        loadBooksJob?.cancel()
    }
}

/**
 * UI State for Book List screen.
 */
data class BookListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val books: List<Book> = emptyList(),
    val selectedClassId: String? = null,
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    val showEmptyState: Boolean
        get() = !isLoading && books.isEmpty() && error == null
        
    val showErrorState: Boolean
        get() = !isLoading && error != null && books.isEmpty()
        
    val showContent: Boolean
        get() = books.isNotEmpty()
}

/**
 * One-time events from Book List ViewModel.
 */
sealed interface BookListEvent {
    data class ShowError(val message: String) : BookListEvent
    data class DownloadStarted(val bookId: String) : BookListEvent
    data class DownloadComplete(val bookId: String) : BookListEvent
    data class DownloadFailed(val bookId: String, val error: String) : BookListEvent
}
