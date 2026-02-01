package com.hdaf.eduapp.presentation.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.usecase.content.GetBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    private val _events = Channel<BookListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadBooks(classId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getBooksUseCase(classId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                books = result.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(BookListEvent.ShowError(result.message ?: "Failed to load books"))
                    }
                }
            }
        }
    }

    fun refreshBooks(classId: String) {
        loadBooks(classId)
    }

    fun downloadBook(book: Book) {
        viewModelScope.launch {
            _events.send(BookListEvent.DownloadStarted(book.id))
            // TODO: Implement actual download logic
            _events.send(BookListEvent.DownloadComplete(book.id))
        }
    }
}

data class BookListUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val selectedClassId: String? = null
)

sealed interface BookListEvent {
    data class ShowError(val message: String) : BookListEvent
    data class DownloadStarted(val bookId: String) : BookListEvent
    data class DownloadComplete(val bookId: String) : BookListEvent
}
