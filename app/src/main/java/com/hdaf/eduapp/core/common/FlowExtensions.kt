package com.hdaf.eduapp.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Extension functions for Flow with Resource wrapper.
 */

/**
 * Wrap a Flow into Resource states with loading, success, and error.
 */
fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return this
        .map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading()) }
        .catch { e -> emit(Resource.Error(e.message ?: "Unknown error", e)) }
}

/**
 * Map Resource data within a Flow.
 */
fun <T, R> Flow<Resource<T>>.mapResource(transform: (T) -> R): Flow<Resource<R>> {
    return this.map { resource -> resource.map(transform) }
}

/**
 * Filter success values from a Resource Flow.
 */
fun <T> Flow<Resource<T>>.filterSuccess(): Flow<T> {
    return this.map { resource ->
        when (resource) {
            is Resource.Success -> resource.data
            else -> null
        }
    }.map { it!! }
}
