package com.hdaf.eduapp.core.accessibility

import android.content.Context
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
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * TTS (Text-to-Speech) Manager with accessibility fallbacks.
 * 
 * Features:
 * - TTS availability detection
 * - Fallback to TalkBack when TTS unavailable
 * - Hindi language support
 * - Adjustable speech rate for different accessibility needs
 * - Queue management for consecutive announcements
 */
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    
    private val _ttsState = MutableStateFlow(TTSState.INITIALIZING)
    val ttsState: StateFlow<TTSState> = _ttsState.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private var speechRate: Float = 1.0f
    private var pitch: Float = 1.0f
    private var currentLocale: Locale = Locale("hi", "IN")
    
    private val accessibilityManager = 
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        _ttsState.value = TTSState.INITIALIZING
        tts = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(currentLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if Hindi not available
                tts?.setLanguage(Locale.US)
                _ttsState.value = TTSState.AVAILABLE_LIMITED
            } else {
                _ttsState.value = TTSState.AVAILABLE
            }
            
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }
                
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            _ttsState.value = TTSState.UNAVAILABLE
        }
    }
    
    /**
     * Speak text with TTS, with automatic fallback.
     * 
     * @param text Text to speak
     * @param queueMode QUEUE_FLUSH to interrupt, QUEUE_ADD to append
     * @param useHindi Whether to use Hindi language
     * @return true if successfully queued, false if fallback was used
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        useHindi: Boolean = true
    ): Boolean {
        if (text.isBlank()) return false
        
        return when (_ttsState.value) {
            TTSState.AVAILABLE, TTSState.AVAILABLE_LIMITED -> {
                setLanguage(useHindi)
                val utteranceId = UUID.randomUUID().toString()
                tts?.speak(text, queueMode, null, utteranceId)
                true
            }
            TTSState.UNAVAILABLE -> {
                // Fallback: Use TalkBack announcement if available
                announceForAccessibility(text)
                false
            }
            TTSState.INITIALIZING -> {
                // Queue for later or use fallback
                announceForAccessibility(text)
                false
            }
        }
    }
    
    /**
     * Speak text and wait for completion.
     */
    suspend fun speakAndWait(text: String, useHindi: Boolean = true): Boolean {
        if (text.isBlank()) return false
        
        return suspendCancellableCoroutine { continuation ->
            if (_ttsState.value == TTSState.UNAVAILABLE) {
                announceForAccessibility(text)
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            
            setLanguage(useHindi)
            val utteranceId = UUID.randomUUID().toString()
            
            val listener = object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                
                override fun onDone(id: String?) {
                    if (id == utteranceId) {
                        tts?.setOnUtteranceProgressListener(null)
                        continuation.resume(true)
                    }
                }
                
                override fun onError(id: String?) {
                    if (id == utteranceId) {
                        tts?.setOnUtteranceProgressListener(null)
                        continuation.resume(false)
                    }
                }
            }
            
            tts?.setOnUtteranceProgressListener(listener)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            
            continuation.invokeOnCancellation {
                stop()
            }
        }
    }
    
    /**
     * Use TalkBack/Accessibility service to announce text.
     * Fallback when TTS is unavailable.
     */
    private fun announceForAccessibility(text: String) {
        if (accessibilityManager.isEnabled) {
            val event = android.view.accessibility.AccessibilityEvent.obtain(
                android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
            )
            event.text.add(text)
            event.className = javaClass.name
            event.packageName = context.packageName
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }
    
    /**
     * Stop current speech.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Set speech rate.
     * @param rate 0.5 = half speed, 1.0 = normal, 2.0 = double speed
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.25f, 4.0f)
        tts?.setSpeechRate(speechRate)
    }
    
    /**
     * Set pitch.
     * @param pitchValue 0.5 = lower, 1.0 = normal, 2.0 = higher
     */
    fun setPitch(pitchValue: Float) {
        pitch = pitchValue.coerceIn(0.5f, 2.0f)
        tts?.setPitch(pitch)
    }
    
    /**
     * Set language.
     */
    fun setLanguage(useHindi: Boolean) {
        val locale = if (useHindi) Locale("hi", "IN") else Locale.US
        if (currentLocale != locale) {
            currentLocale = locale
            tts?.setLanguage(locale)
        }
    }
    
    /**
     * Check if TTS is available.
     */
    fun isAvailable(): Boolean = _ttsState.value != TTSState.UNAVAILABLE
    
    /**
     * Check if TalkBack is enabled.
     */
    fun isTalkBackEnabled(): Boolean = accessibilityManager.isTouchExplorationEnabled
    
    /**
     * Get accessible status message.
     */
    fun getAccessibleStatusMessage(isHindi: Boolean = false): String {
        return when (_ttsState.value) {
            TTSState.AVAILABLE -> {
                if (isHindi) "टेक्स्ट-टू-स्पीच उपलब्ध है"
                else "Text-to-speech is available"
            }
            TTSState.AVAILABLE_LIMITED -> {
                if (isHindi) "टेक्स्ट-टू-स्पीच सीमित भाषा सहायता के साथ उपलब्ध है"
                else "Text-to-speech available with limited language support"
            }
            TTSState.UNAVAILABLE -> {
                if (isHindi) "टेक्स्ट-टू-स्पीच उपलब्ध नहीं है। TalkBack का उपयोग किया जाएगा।"
                else "Text-to-speech unavailable. TalkBack will be used."
            }
            TTSState.INITIALIZING -> {
                if (isHindi) "टेक्स्ट-टू-स्पीच आरंभ हो रहा है"
                else "Text-to-speech initializing"
            }
        }
    }
    
    /**
     * Release TTS resources.
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _ttsState.value = TTSState.UNAVAILABLE
    }
}

/**
 * TTS availability state.
 */
enum class TTSState {
    INITIALIZING,
    AVAILABLE,
    AVAILABLE_LIMITED,  // TTS available but preferred language not supported
    UNAVAILABLE
}
