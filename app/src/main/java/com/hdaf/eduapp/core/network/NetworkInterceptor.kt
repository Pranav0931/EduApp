package com.hdaf.eduapp.core.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor for network error handling and monitoring.
 * 
 * Features:
 * - Checks network connectivity before requests
 * - Handles common HTTP errors
 * - Logs request/response metrics
 * - Retries on transient failures
 */
@Singleton
class NetworkInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : Interceptor {

    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // Check network connectivity
        if (!networkMonitor.isCurrentlyConnected()) {
            throw NoNetworkException("No network connection available")
        }

        val request = chain.request()
        val startTime = System.currentTimeMillis()

        var lastException: IOException? = null
        var retryCount = 0

        while (retryCount < MAX_RETRIES) {
            try {
                val response = chain.proceed(request)
                val duration = System.currentTimeMillis() - startTime

                // Log request metrics
                Timber.d(
                    "HTTP ${response.code} ${request.method} ${request.url} (${duration}ms)"
                )

                // Handle specific HTTP errors
                when (response.code) {
                    401 -> throw UnauthorizedException("Unauthorized access")
                    403 -> throw ForbiddenException("Access forbidden")
                    404 -> throw NotFoundException("Resource not found: ${request.url}")
                    429 -> {
                        // Rate limited - wait and retry
                        val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 1
                        Thread.sleep(retryAfter * 1000)
                        retryCount++
                        continue
                    }
                    in 500..599 -> {
                        // Server error - retry
                        if (retryCount < MAX_RETRIES - 1) {
                            Thread.sleep(RETRY_DELAY_MS * (retryCount + 1))
                            retryCount++
                            continue
                        }
                        throw ServerException("Server error: ${response.code}")
                    }
                }

                return response

            } catch (e: IOException) {
                lastException = e
                Timber.w(e, "Network request failed, retry ${retryCount + 1}/$MAX_RETRIES")
                
                if (retryCount < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_MS * (retryCount + 1))
                }
                retryCount++
            }
        }

        throw lastException ?: IOException("Request failed after $MAX_RETRIES retries")
    }
}

// ==================== Custom Exceptions ====================

/**
 * Exception thrown when there's no network connection.
 */
class NoNetworkException(message: String) : IOException(message)

/**
 * Exception thrown for 401 Unauthorized responses.
 */
class UnauthorizedException(message: String) : IOException(message)

/**
 * Exception thrown for 403 Forbidden responses.
 */
class ForbiddenException(message: String) : IOException(message)

/**
 * Exception thrown for 404 Not Found responses.
 */
class NotFoundException(message: String) : IOException(message)

/**
 * Exception thrown for 5xx Server Error responses.
 */
class ServerException(message: String) : IOException(message)

/**
 * Exception thrown for rate limiting.
 */
class RateLimitedException(message: String, val retryAfterSeconds: Long) : IOException(message)
