package com.hdaf.eduapp.core.common

/**
 * Base interface for all Use Cases in the domain layer.
 * 
 * Use Cases encapsulate business logic and orchestrate data flow
 * between the presentation and data layers.
 * 
 * @param P The input parameter type
 * @param R The return type wrapped in Resource
 */
interface UseCase<in P, out R> {
    /**
     * Execute the use case.
     * 
     * @param params The input parameters
     * @return The result wrapped in Resource
     */
    suspend operator fun invoke(params: P): R
}

/**
 * Use case that doesn't require any parameters.
 */
interface NoParamUseCase<out R> {
    suspend operator fun invoke(): R
}

/**
 * Base class for use cases that return a Flow.
 */
interface FlowUseCase<in P, out R> {
    operator fun invoke(params: P): kotlinx.coroutines.flow.Flow<R>
}

/**
 * Flow use case that doesn't require parameters.
 */
interface NoParamFlowUseCase<out R> {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<R>
}

/**
 * Marker class for use cases that don't need parameters.
 */
object None
