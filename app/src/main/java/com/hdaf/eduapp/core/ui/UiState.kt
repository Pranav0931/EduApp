package com.hdaf.eduapp.core.ui

/**
 * Unified UI state wrapper for consistent state management across all screens.
 * Provides loading, error, success, and empty states with retry capability.
 * 
 * Usage:
 * ```
 * sealed interface MyScreenState : UiState<MyData>
 * ```
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val retryAction: (() -> Unit)? = null
    ) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

/**
 * Extension to check if state represents loading.
 */
val <T> UiState<T>.isLoading: Boolean
    get() = this is UiState.Loading

/**
 * Extension to check if state represents success.
 */
val <T> UiState<T>.isSuccess: Boolean
    get() = this is UiState.Success

/**
 * Extension to check if state represents error.
 */
val <T> UiState<T>.isError: Boolean
    get() = this is UiState.Error

/**
 * Extension to check if state represents empty.
 */
val <T> UiState<T>.isEmpty: Boolean
    get() = this is UiState.Empty

/**
 * Get data if state is Success, null otherwise.
 */
fun <T> UiState<T>.getOrNull(): T? = (this as? UiState.Success)?.data

/**
 * Get data if Success, or default value.
 */
fun <T> UiState<T>.getOrDefault(default: T): T = getOrNull() ?: default

/**
 * Transform data if Success.
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> = when (this) {
    is UiState.Loading -> UiState.Loading
    is UiState.Success -> UiState.Success(transform(data))
    is UiState.Error -> UiState.Error(message, throwable, retryAction)
    is UiState.Empty -> UiState.Empty
}

/**
 * Execute action if state is Success.
 */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

/**
 * Execute action if state is Error.
 */
inline fun <T> UiState<T>.onError(action: (String, Throwable?) -> Unit): UiState<T> {
    if (this is UiState.Error) action(message, throwable)
    return this
}

/**
 * Execute action if state is Loading.
 */
inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

/**
 * Execute action if state is Empty.
 */
inline fun <T> UiState<T>.onEmpty(action: () -> Unit): UiState<T> {
    if (this is UiState.Empty) action()
    return this
}

/**
 * Combined state for list data that also tracks if data is refreshing.
 */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false,
    val hasMoreItems: Boolean = false,
    val canRetry: Boolean = true
) {
    companion object {
        fun <T> loading(): ListUiState<T> = ListUiState(isLoading = true)
        fun <T> success(items: List<T>, hasMore: Boolean = false): ListUiState<T> = 
            ListUiState(items = items, isEmpty = items.isEmpty(), hasMoreItems = hasMore)
        fun <T> error(message: String, items: List<T> = emptyList()): ListUiState<T> = 
            ListUiState(items = items, error = message)
        fun <T> empty(): ListUiState<T> = ListUiState(isEmpty = true)
    }
}

/**
 * State for tracking async operations like downloads, syncs, etc.
 */
data class AsyncOperationState(
    val isInProgress: Boolean = false,
    val progress: Float = 0f,
    val message: String? = null,
    val error: String? = null,
    val isComplete: Boolean = false
)
