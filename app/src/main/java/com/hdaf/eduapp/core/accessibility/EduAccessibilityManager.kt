package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.accessibility.AccessibilityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized accessibility manager for TTS, haptic feedback, and accessibility state.
 * Provides production-ready accessibility features for blind and deaf users.
 */
@Singleton
class EduAccessibilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _currentLanguage = MutableStateFlow(Locale("hi", "IN"))
    val currentLanguage: StateFlow<Locale> = _currentLanguage.asStateFlow()
    
    private var speechRate = 1.0f
    private var speechPitch = 1.0f
    
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
     * Initialize Text-to-Speech engine.
     */
    fun initializeTts(
        locale: Locale = Locale("hi", "IN"),
        onInitialized: (Boolean) -> Unit = {}
    ) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English
                    textToSpeech?.setLanguage(Locale.US)
                    _currentLanguage.value = Locale.US
                    Timber.w("TTS: Hindi not available, falling back to English")
                } else {
                    _currentLanguage.value = locale
                }
                
                textToSpeech?.setSpeechRate(speechRate)
                textToSpeech?.setPitch(speechPitch)
                
                setupUtteranceListener()
                _isTtsReady.value = true
                onInitialized(true)
                Timber.d("TTS initialized successfully")
            } else {
                _isTtsReady.value = false
                onInitialized(false)
                Timber.e("TTS initialization failed with status: $status")
            }
        }
    }
    
    private fun setupUtteranceListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Timber.e("TTS error for utterance: $utteranceId")
            }
        })
    }
    
    /**
     * Speak text with accessibility-friendly features.
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        utteranceId: String = UUID.randomUUID().toString()
    ): Flow<SpeechState> = callbackFlow {
        if (!_isTtsReady.value) {
            trySend(SpeechState.Error("TTS not initialized"))
            close()
            return@callbackFlow
        }
        
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) {
                    trySend(SpeechState.Started)
                }
            }
            
            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    trySend(SpeechState.Completed)
                    close()
                }
            }
            
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    trySend(SpeechState.Error("Speech error"))
                    close()
                }
            }
            
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                trySend(SpeechState.Progress(start, end, text.length))
            }
        })
        
        textToSpeech?.speak(text, queueMode, params, utteranceId)
        trySend(SpeechState.Pending)
        
        awaitClose {
            // Cleanup if needed
        }
    }
    
    /**
     * Simple speak without flow tracking.
     */
    fun speakNow(text: String, interrupt: Boolean = true) {
        if (!_isTtsReady.value) {
            Timber.w("TTS not ready, cannot speak: $text")
            return
        }
        
        val queueMode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString())
        }
        
        textToSpeech?.speak(text, queueMode, params, UUID.randomUUID().toString())
    }
    
    /**
     * Stop any ongoing speech.
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Set speech rate (0.5f to 2.0f).
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setSpeechRate(speechRate)
    }
    
    /**
     * Set speech pitch (0.5f to 2.0f).
     */
    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(speechPitch)
    }
    
    /**
     * Change TTS language.
     */
    fun setLanguage(locale: Locale): Boolean {
        val result = textToSpeech?.setLanguage(locale)
        return if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            _currentLanguage.value = locale
            true
        } else {
            Timber.w("Language not available: ${locale.displayLanguage}")
            false
        }
    }
    
    // ==================== Haptic Feedback ====================
    
    /**
     * Provide haptic feedback for interactions.
     */
    fun hapticFeedback(type: HapticType) {
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.CLICK -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.LONG_PRESS -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
                HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                HapticType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 75, 75, 75), -1)
                HapticType.NAVIGATION -> VibrationEffect.createOneShot(30, 100)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.CLICK -> vibrator.vibrate(50)
                HapticType.LONG_PRESS -> vibrator.vibrate(100)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
                HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                HapticType.WARNING -> vibrator.vibrate(longArrayOf(0, 75, 75, 75), -1)
                HapticType.NAVIGATION -> vibrator.vibrate(30)
            }
        }
    }
    
    /**
     * Alias for hapticFeedback for backward compatibility.
     */
    fun provideHapticFeedback(type: HapticType) = hapticFeedback(type)
    
    // ==================== Accessibility State ====================
    
    /**
     * Check if screen reader (TalkBack) is enabled.
     */
    fun isScreenReaderEnabled(): Boolean {
        return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
    }
    
    /**
     * Check if accessibility services are enabled.
     */
    fun isAccessibilityEnabled(): Boolean {
        return accessibilityManager.isEnabled
    }
    
    /**
     * Announce for accessibility (for dynamic content updates).
     */
    fun announceForAccessibility(message: String) {
        if (isScreenReaderEnabled()) {
            speakNow(message, interrupt = false)
        }
    }
    
    // ==================== Cleanup ====================
    
    /**
     * Release TTS resources.
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isTtsReady.value = false
        _isSpeaking.value = false
        Timber.d("TTS shutdown complete")
    }
}

/**
 * Speech state for tracking TTS progress.
 */
sealed class SpeechState {
    data object Pending : SpeechState()
    data object Started : SpeechState()
    data class Progress(val start: Int, val end: Int, val total: Int) : SpeechState()
    data object Completed : SpeechState()
    data class Error(val message: String) : SpeechState()
}

/**
 * Haptic feedback types.
 */
enum class HapticType {
    CLICK,
    LONG_PRESS,
    SUCCESS,
    ERROR,
    WARNING,
    NAVIGATION
}
