package com.hdaf.eduapp.accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.SpeechRate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Text-to-Speech Engine for blind user support.
 * Provides spoken feedback for all content and navigation.
 */
@Singleton
class TTSEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _currentUtterance = MutableStateFlow<String?>(null)
    val currentUtterance: StateFlow<String?> = _currentUtterance.asStateFlow()
    
    private var currentSpeechRate = 1.0f
    private var currentPitch = 1.0f
    private var currentLocale = Locale.getDefault()
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Initialize TTS engine.
     */
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        if (isInitialized && tts != null) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }
        
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                tts?.language = currentLocale
                tts?.setSpeechRate(currentSpeechRate)
                tts?.setPitch(currentPitch)
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtterance.value = null
                    }
                    
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtterance.value = null
                    }
                    
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _currentUtterance.value = null
                        Timber.e("TTS error: $errorCode for utterance: $utteranceId")
                    }
                })
            }
            if (continuation.isActive) {
                continuation.resume(isInitialized)
            }
        }
        
        continuation.invokeOnCancellation {
            // TTS handles its own lifecycle
        }
    }
    
    /**
     * Apply settings from accessibility profile.
     */
    fun applyProfile(profile: AccessibilityProfile) {
        currentSpeechRate = profile.speechRate.toFloat()
        tts?.setSpeechRate(currentSpeechRate)
    }
    
    /**
     * Set speech rate.
     */
    fun setSpeechRate(rate: SpeechRate) {
        currentSpeechRate = rate.toFloat()
        tts?.setSpeechRate(currentSpeechRate)
    }
    
    /**
     * Set language/locale.
     */
    fun setLanguage(locale: Locale): Boolean {
        currentLocale = locale
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }
    
    /**
     * Speak text immediately, interrupting any current speech.
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized) {
            Timber.w("TTS not initialized, cannot speak")
            return
        }
        
        _currentUtterance.value = text
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, queueMode, null, utteranceId)
    }
    
    /**
     * Speak text and wait for completion.
     */
    suspend fun speakAndWait(text: String): Boolean = suspendCancellableCoroutine { continuation ->
        if (!isInitialized) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        val utteranceId = UUID.randomUUID().toString()
        
        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            
            override fun onDone(id: String?) {
                if (id == utteranceId && continuation.isActive) {
                    continuation.resume(true)
                }
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId && continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
        
        tts?.setOnUtteranceProgressListener(listener)
        _currentUtterance.value = text
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        
        continuation.invokeOnCancellation {
            stop()
        }
    }
    
    /**
     * Add text to speech queue.
     */
    fun addToQueue(text: String) {
        speak(text, TextToSpeech.QUEUE_ADD)
    }
    
    /**
     * Stop current speech.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentUtterance.value = null
    }
    
    /**
     * Provide haptic feedback for navigation.
     */
    fun provideHapticFeedback(type: HapticType = HapticType.CLICK) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.CLICK -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.DOUBLE_CLICK -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
                HapticType.LONG -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 100), -1)
                HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                HapticType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.CLICK -> vibrator.vibrate(50)
                HapticType.DOUBLE_CLICK -> vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
                HapticType.LONG -> vibrator.vibrate(200)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 100, 100), -1)
                HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                HapticType.WARNING -> vibrator.vibrate(longArrayOf(0, 150, 100, 150), -1)
            }
        }
    }
    
    /**
     * Speak with haptic feedback.
     */
    fun speakWithHaptic(text: String, hapticType: HapticType = HapticType.CLICK) {
        provideHapticFeedback(hapticType)
        speak(text)
    }
    
    /**
     * Release TTS resources.
     */
    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

/**
 * Types of haptic feedback for different interactions.
 */
enum class HapticType {
    CLICK,
    DOUBLE_CLICK,
    LONG,
    SUCCESS,
    ERROR,
    WARNING
}
