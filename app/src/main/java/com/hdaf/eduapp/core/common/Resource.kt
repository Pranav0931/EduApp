package com.hdaf.eduapp.core.common

/**
 * A sealed class representing the result of an operation.
 * 
 * This is the standard way to handle success/error states across
 * the application, following the Result pattern.
 * 
 * @param T The type of data on success
 * 
 * Usage:
 * ```kotlin
 * when (result) {
 *     is Resource.Success -> handleSuccess(result.data)
 *     is Resource.Error -> handleError(result.message)
 *     is Resource.Loading -> showLoading()
 * }
 * ```
 */
sealed class Resource<out T> {
    
    /**
     * Represents a successful operation with data.
     * 
     * @param data The result data
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * Represents a failed operation.
     * 
     * @param message Human-readable error message
     * @param exception Optional exception that caused the error
     * @param errorCode Optional error code for programmatic handling
     */
    data class Error<T>(
        val message: String,
        val exception: Throwable? = null,
        val errorCode: ErrorCode = ErrorCode.UNKNOWN
    ) : Resource<T>()
    
    /**
     * Represents an ongoing operation.
     * 
     * @param data Optional cached/stale data to show while loading
     * @param progress Optional progress percentage (0-100)
     */
    data class Loading<T>(
        val data: T? = null,
        val progress: Int? = null
    ) : Resource<T>()
    
    /**
     * Check if this result is successful.
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Check if this result is an error.
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Check if this result is loading.
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Get data or null.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Loading -> data
        is Error -> null
    }
    
    /**
     * Get data or throw exception.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Loading -> throw IllegalStateException("Resource is still loading")
        is Error -> throw exception ?: IllegalStateException(message)
    }
    
    /**
     * Get data or default value.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = getOrNull() ?: default
    
    /**
     * Map the success data to another type.
     */
    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, exception, errorCode)
        is Loading -> Loading(data?.let(transform))
    }
    
    /**
     * FlatMap the success data to another Resource.
     */
    fun <R> flatMap(transform: (T) -> Resource<R>): Resource<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(message, exception, errorCode)
        is Loading -> Loading()
    }
    
    /**
     * Execute action on success.
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action on error.
     */
    inline fun onError(action: (String, Throwable?) -> Unit): Resource<T> {
        if (this is Error) action(message, exception)
        return this
    }
    
    /**
     * Execute action on loading.
     */
    inline fun onLoading(action: (T?) -> Unit): Resource<T> {
        if (this is Loading) action(data)
        return this
    }
    
    companion object {
        /**
         * Create a Success result.
         */
        fun <T> success(data: T): Resource<T> = Success(data)
        
        /**
         * Create an Error result.
         */
        fun <T> error(
            message: String,
            exception: Throwable? = null,
            errorCode: ErrorCode = ErrorCode.UNKNOWN
        ): Resource<T> = Error(message, exception, errorCode)
        
        /**
         * Create a Loading result.
         */
        fun <T> loading(data: T? = null, progress: Int? = null): Resource<T> = 
            Loading(data, progress)
    }
}

/**
 * Standard error codes for consistent error handling.
 */
enum class ErrorCode {
    UNKNOWN,
    NETWORK_ERROR,
    NETWORK_TIMEOUT,
    SERVER_ERROR,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    VALIDATION_ERROR,
    DATABASE_ERROR,
    CACHE_ERROR,
    PARSING_ERROR,
    EMPTY_RESULT,
    RATE_LIMITED,
    MAINTENANCE,
    OFFLINE
}
