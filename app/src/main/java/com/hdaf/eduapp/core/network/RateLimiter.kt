package com.hdaf.eduapp.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Rate Limiter with queuing and retry support.
 * 
 * Features:
 * - Token bucket rate limiting
 * - Request queuing for rate-limited APIs
 * - Automatic retry with exponential backoff
 * - Per-endpoint rate limiting
 * - Accessibility-friendly status messages
 */
@Singleton
class RateLimiter @Inject constructor() {
    
    // Default: 60 requests per minute
    private val defaultRequestsPerMinute = 60
    private val defaultBurstSize = 10
    
    // Per-endpoint rate limits
    private val endpointLimits = ConcurrentHashMap<String, EndpointRateLimit>()
    
    // Token buckets for each endpoint
    private val tokenBuckets = ConcurrentHashMap<String, TokenBucket>()
    
    // Request queues for rate-limited requests
    private val requestQueues = ConcurrentHashMap<String, ConcurrentLinkedQueue<QueuedRequest>>()
    
    private val mutex = Mutex()
    
    /**
     * Configure rate limit for an endpoint.
     */
    fun configure(
        endpoint: String,
        requestsPerMinute: Int,
        burstSize: Int = requestsPerMinute / 6
    ) {
        endpointLimits[endpoint] = EndpointRateLimit(
            requestsPerMinute = requestsPerMinute,
            burstSize = burstSize
        )
        tokenBuckets[endpoint] = TokenBucket(
            maxTokens = burstSize,
            refillRate = requestsPerMinute.toDouble() / 60.0 // tokens per second
        )
    }
    
    /**
     * Acquire permission to make a request.
     * Will block until permission is granted.
     * 
     * @param endpoint The endpoint identifier
     * @return true if permission granted, false if should abort
     */
    suspend fun acquire(endpoint: String = "default"): Boolean {
        val bucket = tokenBuckets.getOrPut(endpoint) {
            TokenBucket(
                maxTokens = defaultBurstSize,
                refillRate = defaultRequestsPerMinute.toDouble() / 60.0
            )
        }
        
        return mutex.withLock {
            bucket.refill()
            
            if (bucket.tokens >= 1) {
                bucket.tokens -= 1
                true
            } else {
                // Wait for refill
                val waitTime = ((1 - bucket.tokens) / bucket.refillRate * 1000).toLong()
                delay(waitTime.coerceAtMost(60000)) // Max 1 minute wait
                bucket.refill()
                
                if (bucket.tokens >= 1) {
                    bucket.tokens -= 1
                    true
                } else {
                    false
                }
            }
        }
    }
    
    /**
     * Try to acquire without blocking.
     * 
     * @return true if acquired, false if rate limited
     */
    fun tryAcquire(endpoint: String = "default"): Boolean {
        val bucket = tokenBuckets.getOrPut(endpoint) {
            TokenBucket(
                maxTokens = defaultBurstSize,
                refillRate = defaultRequestsPerMinute.toDouble() / 60.0
            )
        }
        
        bucket.refill()
        
        return if (bucket.tokens >= 1) {
            bucket.tokens -= 1
            true
        } else {
            false
        }
    }
    
    /**
     * Queue a request for later execution.
     * 
     * @param endpoint The endpoint identifier
     * @param requestId Unique request ID
     * @param priority Higher priority = earlier execution
     */
    fun queueRequest(
        endpoint: String,
        requestId: String,
        priority: Int = 0,
        onExecute: suspend () -> Unit
    ) {
        val queue = requestQueues.getOrPut(endpoint) { ConcurrentLinkedQueue() }
        queue.add(QueuedRequest(requestId, priority, onExecute))
    }
    
    /**
     * Get estimated wait time in seconds.
     */
    fun getEstimatedWaitSeconds(endpoint: String = "default"): Int {
        val bucket = tokenBuckets[endpoint] ?: return 0
        bucket.refill()
        
        return if (bucket.tokens >= 1) {
            0
        } else {
            ((1 - bucket.tokens) / bucket.refillRate).toInt() + 1
        }
    }
    
    /**
     * Get accessibility-friendly status message.
     */
    fun getAccessibleStatusMessage(
        endpoint: String = "default",
        isHindi: Boolean = false
    ): String {
        val waitSeconds = getEstimatedWaitSeconds(endpoint)
        
        return if (waitSeconds == 0) {
            if (isHindi) "अनुरोध भेजने के लिए तैयार"
            else "Ready to send request"
        } else {
            if (isHindi) "कृपया $waitSeconds सेकंड प्रतीक्षा करें"
            else "Please wait $waitSeconds seconds"
        }
    }
    
    /**
     * Reset rate limit for an endpoint (e.g., after error recovery).
     */
    fun reset(endpoint: String) {
        tokenBuckets.remove(endpoint)
        requestQueues.remove(endpoint)
    }
    
    /**
     * Get queue size for an endpoint.
     */
    fun getQueueSize(endpoint: String): Int {
        return requestQueues[endpoint]?.size ?: 0
    }
}

/**
 * Token bucket for rate limiting.
 */
private class TokenBucket(
    val maxTokens: Int,
    val refillRate: Double // tokens per second
) {
    var tokens: Double = maxTokens.toDouble()
    var lastRefillTime: Long = System.currentTimeMillis()
    
    fun refill() {
        val now = System.currentTimeMillis()
        val elapsed = (now - lastRefillTime) / 1000.0
        tokens = (tokens + elapsed * refillRate).coerceAtMost(maxTokens.toDouble())
        lastRefillTime = now
    }
}

/**
 * Endpoint-specific rate limit configuration.
 */
private data class EndpointRateLimit(
    val requestsPerMinute: Int,
    val burstSize: Int
)

/**
 * Queued request.
 */
private data class QueuedRequest(
    val requestId: String,
    val priority: Int,
    val onExecute: suspend () -> Unit
)

/**
 * Retry with exponential backoff.
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 30000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    repeat(maxRetries - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            // Check if it's a rate limit error
            if (e.message?.contains("429", ignoreCase = true) == true ||
                e.message?.contains("rate limit", ignoreCase = true) == true) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            } else {
                throw e
            }
        }
    }
    return block() // Last attempt
}
