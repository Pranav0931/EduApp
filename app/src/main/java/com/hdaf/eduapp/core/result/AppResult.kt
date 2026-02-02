package com.hdaf.eduapp.core.result

import com.hdaf.eduapp.core.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * A sealed class representing the result of an operation.
 * 
 * Usage:
 * ```kotlin
 * when (result) {
 *     is AppResult.Success -> handleSuccess(result.data)
 *     is AppResult.Error -> handleError(result.error)
 *     is AppResult.Loading -> showLoading()
 * }
 * ```
 */
sealed class AppResult<out T> {
    
    /**
     * Successful result with data.
     */
    data class Success<T>(val data: T) : AppResult<T>()
    
    /**
     * Error result with AppError.
     */
    data class Error(val error: AppError) : AppResult<Nothing>()
    
    /**
     * Loading state.
     */
    data object Loading : AppResult<Nothing>()
    
    /**
     * Check if result is successful.
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Check if result is an error.
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Check if result is loading.
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Get data if successful, null otherwise.
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Get data if successful, throw exception otherwise.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw error.cause ?: IllegalStateException(error.message)
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    /**
     * Get data if successful, default value otherwise.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = (this as? Success)?.data ?: default
    
    /**
     * Get error if failed, null otherwise.
     */
    fun errorOrNull(): AppError? = (this as? Error)?.error
    
    /**
     * Map success data to another type.
     */
    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
    
    /**
     * FlatMap success data to another result.
     */
    inline fun <R> flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
    
    /**
     * Execute action on success.
     */
    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action on error.
     */
    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }
    
    /**
     * Execute action on loading.
     */
    inline fun onLoading(action: () -> Unit): AppResult<T> {
        if (this is Loading) action()
        return this
    }
    
    /**
     * Recover from error with a default value.
     */
    inline fun recover(transform: (AppError) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> transform(error)
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    /**
     * Recover from error with another result.
     */
    inline fun recoverWith(transform: (AppError) -> AppResult<@UnsafeVariance T>): AppResult<T> = when (this) {
        is Success -> this
        is Error -> transform(error)
        is Loading -> this
    }
    
    companion object {
        /**
         * Create a success result.
         */
        fun <T> success(data: T): AppResult<T> = Success(data)
        
        /**
         * Create an error result.
         */
        fun error(error: AppError): AppResult<Nothing> = Error(error)
        
        /**
         * Create an error result from throwable.
         */
        fun error(throwable: Throwable): AppResult<Nothing> = Error(AppError.from(throwable))
        
        /**
         * Create a loading result.
         */
        fun loading(): AppResult<Nothing> = Loading
        
        /**
         * Wrap a suspending operation in a result.
         */
        suspend inline fun <T> runCatching(block: suspend () -> T): AppResult<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(AppError.from(e))
            }
        }
    }
}

/**
 * Convert Flow<T> to Flow<AppResult<T>> with error handling.
 */
fun <T> Flow<T>.asResult(): Flow<AppResult<T>> = this
    .map<T, AppResult<T>> { AppResult.Success(it) }
    .catch { emit(AppResult.Error(AppError.from(it))) }

/**
 * Convert Flow<AppResult<T>> to Flow<T?> (null on error).
 */
fun <T> Flow<AppResult<T>>.dataOrNull(): Flow<T?> = this
    .map { it.getOrNull() }
