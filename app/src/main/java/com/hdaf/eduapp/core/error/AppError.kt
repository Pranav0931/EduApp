package com.hdaf.eduapp.core.error

import android.content.Context
import com.hdaf.eduapp.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling for EduApp.
 * 
 * This sealed class hierarchy represents all possible errors in the app,
 * providing:
 * - Type-safe error handling
 * - User-friendly error messages (English & Hindi)
 * - Actionable recovery suggestions
 * - Accessibility-friendly descriptions
 */
sealed class AppError(
    open val message: String,
    open val messageHindi: String,
    open val cause: Throwable? = null,
    open val isRecoverable: Boolean = true
) {
    
    /**
     * Get TalkBack-friendly description with recovery suggestion.
     */
    fun getAccessibleDescription(isHindi: Boolean = false): String {
        val msg = if (isHindi) messageHindi else message
        val recovery = getRecoverySuggestion(isHindi)
        return if (recovery.isNotEmpty()) "$msg। $recovery" else msg
    }
    
    /**
     * Get recovery suggestion for the error.
     */
    abstract fun getRecoverySuggestion(isHindi: Boolean = false): String
    
    // ========== Network Errors ==========
    
    /**
     * No internet connection available.
     */
    data class NoInternet(
        override val cause: Throwable? = null
    ) : AppError(
        message = "No internet connection",
        messageHindi = "इंटरनेट कनेक्शन नहीं है",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें"
            else "Please check your internet connection and try again"
    }
    
    /**
     * Request timed out.
     */
    data class Timeout(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Request timed out",
        messageHindi = "अनुरोध का समय समाप्त हो गया",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया कुछ देर बाद पुनः प्रयास करें"
            else "Please try again after some time"
    }
    
    /**
     * Server error (5xx).
     */
    data class ServerError(
        val statusCode: Int = 500,
        override val cause: Throwable? = null
    ) : AppError(
        message = "Server error. Please try again later",
        messageHindi = "सर्वर त्रुटि। कृपया बाद में पुनः प्रयास करें",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "हमारी टीम इस समस्या पर काम कर रही है"
            else "Our team is working on this issue"
    }
    
    /**
     * API rate limit exceeded.
     */
    data class RateLimitExceeded(
        val retryAfterSeconds: Int = 60,
        override val cause: Throwable? = null
    ) : AppError(
        message = "Too many requests. Please wait a moment",
        messageHindi = "बहुत सारे अनुरोध। कृपया कुछ देर प्रतीक्षा करें",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "$retryAfterSeconds सेकंड बाद पुनः प्रयास करें"
            else "Try again in $retryAfterSeconds seconds"
    }
    
    // ========== Data Errors ==========
    
    /**
     * Data not found (404 or missing local data).
     */
    data class NotFound(
        val resourceType: String = "Data",
        override val cause: Throwable? = null
    ) : AppError(
        message = "$resourceType not found",
        messageHindi = "$resourceType नहीं मिला",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया पृष्ठ को रीफ्रेश करें"
            else "Please refresh the page"
    }
    
    /**
     * Data parsing/validation error.
     */
    data class DataParsing(
        override val message: String = "Unable to process data",
        override val messageHindi: String = "डेटा प्रोसेस करने में असमर्थ",
        override val cause: Throwable? = null
    ) : AppError(
        message = message,
        messageHindi = messageHindi,
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया ऐप को पुनः आरंभ करें"
            else "Please restart the app"
    }
    
    /**
     * Database error.
     */
    data class Database(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Unable to save data locally",
        messageHindi = "डेटा स्थानीय रूप से सहेजने में असमर्थ",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया ऐप का डेटा साफ़ करें या पुनः इंस्टॉल करें"
            else "Please clear app data or reinstall"
    }
    
    // ========== Device Errors ==========
    
    /**
     * Storage full.
     */
    data class StorageFull(
        val requiredSpaceMB: Long = 0,
        override val cause: Throwable? = null
    ) : AppError(
        message = "Not enough storage space",
        messageHindi = "पर्याप्त स्टोरेज स्पेस नहीं है",
        cause = cause,
        isRecoverable = true
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "${requiredSpaceMB}MB स्पेस खाली करें और पुनः प्रयास करें"
            else "Free up ${requiredSpaceMB}MB space and try again"
    }
    
    /**
     * Low memory.
     */
    data class LowMemory(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Low memory. Some features may be limited",
        messageHindi = "मेमोरी कम है। कुछ सुविधाएं सीमित हो सकती हैं",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया अन्य ऐप्स बंद करें"
            else "Please close other apps"
    }
    
    // ========== Accessibility Errors ==========
    
    /**
     * TTS (Text-to-Speech) not available.
     */
    data class TTSNotAvailable(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Text-to-speech is not available",
        messageHindi = "टेक्स्ट-टू-स्पीच उपलब्ध नहीं है",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया सेटिंग्स में TTS इंजन इंस्टॉल करें"
            else "Please install a TTS engine in Settings"
    }
    
    /**
     * Audio playback error.
     */
    data class AudioPlayback(
        override val message: String = "Unable to play audio",
        override val messageHindi: String = "ऑडियो चलाने में असमर्थ",
        override val cause: Throwable? = null
    ) : AppError(
        message = message,
        messageHindi = messageHindi,
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया ऑडियो फाइल पुनः डाउनलोड करें"
            else "Please re-download the audio file"
    }
    
    // ========== AI/Quiz Errors ==========
    
    /**
     * AI service unavailable.
     */
    data class AIServiceUnavailable(
        override val cause: Throwable? = null
    ) : AppError(
        message = "AI service is currently unavailable",
        messageHindi = "AI सेवा वर्तमान में अनुपलब्ध है",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया बाद में पुनः प्रयास करें"
            else "Please try again later"
    }
    
    /**
     * Quiz generation failed.
     */
    data class QuizGenerationFailed(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Unable to generate quiz",
        messageHindi = "क्विज़ बनाने में असमर्थ",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया अध्याय पुनः खोलें और प्रयास करें"
            else "Please reopen the chapter and try again"
    }
    
    // ========== Authentication Errors ==========
    
    /**
     * User not authenticated.
     */
    data class NotAuthenticated(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Please sign in to continue",
        messageHindi = "जारी रखने के लिए कृपया साइन इन करें",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "साइन इन पृष्ठ पर जाएं"
            else "Go to sign in page"
    }
    
    /**
     * Session expired.
     */
    data class SessionExpired(
        override val cause: Throwable? = null
    ) : AppError(
        message = "Session expired. Please sign in again",
        messageHindi = "सत्र समाप्त। कृपया पुनः साइन इन करें",
        cause = cause
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "साइन इन पृष्ठ पर जाएं"
            else "Go to sign in page"
    }
    
    // ========== Generic Errors ==========
    
    /**
     * Unknown/unexpected error.
     */
    data class Unknown(
        override val message: String = "Something went wrong",
        override val messageHindi: String = "कुछ गलत हो गया",
        override val cause: Throwable? = null
    ) : AppError(
        message = message,
        messageHindi = messageHindi,
        cause = cause,
        isRecoverable = true
    ) {
        override fun getRecoverySuggestion(isHindi: Boolean): String =
            if (isHindi) "कृपया पुनः प्रयास करें"
            else "Please try again"
    }
    
    companion object {
        /**
         * Convert throwable to AppError.
         */
        fun from(throwable: Throwable): AppError {
            return when (throwable) {
                is UnknownHostException -> NoInternet(throwable)
                is SocketTimeoutException -> Timeout(throwable)
                is IOException -> {
                    when {
                        throwable.message?.contains("No space left") == true -> StorageFull(cause = throwable)
                        throwable.message?.contains("network", ignoreCase = true) == true -> NoInternet(throwable)
                        else -> Unknown(cause = throwable)
                    }
                }
                is OutOfMemoryError -> LowMemory(throwable)
                else -> Unknown(
                    message = throwable.message ?: "Something went wrong",
                    messageHindi = "कुछ गलत हो गया",
                    cause = throwable
                )
            }
        }
        
        /**
         * Create error from HTTP status code.
         */
        fun fromHttpStatus(statusCode: Int, message: String? = null): AppError {
            return when (statusCode) {
                401 -> NotAuthenticated()
                403 -> NotAuthenticated()
                404 -> NotFound()
                429 -> RateLimitExceeded()
                in 500..599 -> ServerError(statusCode)
                else -> Unknown(message = message ?: "HTTP Error $statusCode")
            }
        }
    }
}

/**
 * Extension function to get localized error message.
 */
fun AppError.getLocalizedMessage(context: Context): String {
    // Check if Hindi is preferred (simplified check)
    val isHindi = context.resources.configuration.locales.get(0)?.language == "hi"
    return if (isHindi) messageHindi else message
}
