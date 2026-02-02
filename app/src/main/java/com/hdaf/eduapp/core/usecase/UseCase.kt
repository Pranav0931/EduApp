package com.hdaf.eduapp.core.usecase

import com.hdaf.eduapp.core.result.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.hdaf.eduapp.core.error.AppError

/**
 * Base UseCase for one-shot operations (suspend functions).
 * 
 * Usage:
 * ```kotlin
 * class GetUserUseCase @Inject constructor(
 *     private val userRepository: UserRepository
 * ) : UseCase<String, User>() {
 *     override suspend fun execute(params: String): User {
 *         return userRepository.getUser(params)
 *     }
 * }
 * 
 * // In ViewModel:
 * val result = getUserUseCase("user123")
 * result.onSuccess { user -> /* handle user */ }
 *       .onError { error -> /* handle error */ }
 * ```
 */
abstract class UseCase<in P, R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Execute the use case with parameters.
     */
    suspend operator fun invoke(params: P): AppResult<R> {
        return try {
            withContext(dispatcher) {
                AppResult.Success(execute(params))
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.from(e))
        }
    }
    
    /**
     * Execute the use case without wrapping in AppResult.
     * Use when you need raw exceptions.
     */
    suspend fun executeRaw(params: P): R = withContext(dispatcher) {
        execute(params)
    }
    
    /**
     * Override to implement the use case logic.
     */
    protected abstract suspend fun execute(params: P): R
}

/**
 * Base UseCase for operations with no parameters.
 */
abstract class NoParamsUseCase<R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(): AppResult<R> {
        return try {
            withContext(dispatcher) {
                AppResult.Success(execute())
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.from(e))
        }
    }
    
    suspend fun executeRaw(): R = withContext(dispatcher) {
        execute()
    }
    
    protected abstract suspend fun execute(): R
}

/**
 * Base UseCase for Flow-based operations (reactive streams).
 * 
 * Usage:
 * ```kotlin
 * class ObserveUserUseCase @Inject constructor(
 *     private val userRepository: UserRepository
 * ) : FlowUseCase<String, User>() {
 *     override fun execute(params: String): Flow<User> {
 *         return userRepository.observeUser(params)
 *     }
 * }
 * 
 * // In ViewModel:
 * observeUserUseCase("user123")
 *     .onEach { result ->
 *         when (result) {
 *             is AppResult.Success -> /* handle user */
 *             is AppResult.Error -> /* handle error */
 *             is AppResult.Loading -> /* show loading */
 *         }
 *     }
 *     .launchIn(viewModelScope)
 * ```
 */
abstract class FlowUseCase<in P, R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    operator fun invoke(params: P): Flow<AppResult<R>> {
        return execute(params)
            .catch { emit(throw it) }
            .flowOn(dispatcher)
            .let { flow ->
                kotlinx.coroutines.flow.flow {
                    try {
                        flow.collect { emit(AppResult.Success(it)) }
                    } catch (e: Exception) {
                        emit(AppResult.Error(AppError.from(e)))
                    }
                }
            }
    }
    
    protected abstract fun execute(params: P): Flow<R>
}

/**
 * Base UseCase for Flow operations with no parameters.
 */
abstract class NoParamsFlowUseCase<R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    operator fun invoke(): Flow<AppResult<R>> {
        return execute()
            .flowOn(dispatcher)
            .let { flow ->
                kotlinx.coroutines.flow.flow {
                    try {
                        flow.collect { emit(AppResult.Success(it)) }
                    } catch (e: Exception) {
                        emit(AppResult.Error(AppError.from(e)))
                    }
                }
            }
    }
    
    protected abstract fun execute(): Flow<R>
}

/**
 * Marker object for use cases with no parameters.
 */
object NoParams
