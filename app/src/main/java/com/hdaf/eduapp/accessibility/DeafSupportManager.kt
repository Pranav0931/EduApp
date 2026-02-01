package com.hdaf.eduapp.accessibility

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deaf Support Manager for subtitle generation and visual feedback.
 * Provides visual alternatives for audio content.
 */
@Singleton
class DeafSupportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    
    private val _currentSubtitle = MutableStateFlow<SubtitleData?>(null)
    val currentSubtitle: StateFlow<SubtitleData?> = _currentSubtitle.asStateFlow()
    
    private val _subtitleQueue = MutableStateFlow<List<SubtitleData>>(emptyList())
    val subtitleQueue: StateFlow<List<SubtitleData>> = _subtitleQueue.asStateFlow()
    
    private val _visualAlert = MutableSharedFlow<VisualAlert>()
    val visualAlert: SharedFlow<VisualAlert> = _visualAlert.asSharedFlow()
    
    private val _isMonitoringSound = MutableStateFlow(false)
    val isMonitoringSound: StateFlow<Boolean> = _isMonitoringSound.asStateFlow()
    
    private var subtitleDisplayJob: Job? = null
    private var soundMonitorJob: Job? = null
    
    private var subtitleDurationMs = 3000L
    private var flashOnSound = true
    private var vibrateOnSound = true
    
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
     * Apply settings from accessibility profile.
     */
    fun applyProfile(profile: AccessibilityProfile) {
        // Use hapticFeedbackEnabled for both flash and vibrate features
        flashOnSound = profile.hapticFeedbackEnabled
        vibrateOnSound = profile.hapticFeedbackEnabled
    }
    
    /**
     * Show a subtitle on screen.
     */
    fun showSubtitle(text: String, speaker: String? = null, type: SubtitleType = SubtitleType.SPEECH) {
        val subtitle = SubtitleData(
            text = text,
            speaker = speaker,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        
        _currentSubtitle.value = subtitle
        
        // Add to queue for history
        _subtitleQueue.value = (_subtitleQueue.value + subtitle).takeLast(50)
        
        // Auto-hide after duration
        subtitleDisplayJob?.cancel()
        subtitleDisplayJob = scope.launch {
            delay(subtitleDurationMs)
            _currentSubtitle.value = null
        }
    }
    
    /**
     * Show subtitles from a list with timing.
     */
    suspend fun showTimedSubtitles(subtitles: List<TimedSubtitle>) {
        for (subtitle in subtitles) {
            delay(subtitle.delayMs)
            showSubtitle(subtitle.text, subtitle.speaker, subtitle.type)
            delay(subtitle.durationMs)
        }
        _currentSubtitle.value = null
    }
    
    /**
     * Convert lesson content to timed subtitles.
     */
    fun contentToSubtitles(content: String, wordsPerMinute: Int = 150): List<TimedSubtitle> {
        val sentences = content.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        val subtitles = mutableListOf<TimedSubtitle>()
        
        for (sentence in sentences) {
            val wordCount = sentence.trim().split(Regex("\\s+")).size
            val durationMs = (wordCount.toFloat() / wordsPerMinute * 60 * 1000).toLong()
            
            subtitles.add(
                TimedSubtitle(
                    text = sentence.trim(),
                    delayMs = 0,
                    durationMs = maxOf(durationMs, 2000L) // Minimum 2 seconds
                )
            )
        }
        
        return subtitles
    }
    
    /**
     * Clear current subtitle.
     */
    fun hideSubtitle() {
        subtitleDisplayJob?.cancel()
        _currentSubtitle.value = null
    }
    
    /**
     * Emit a visual alert.
     */
    fun sendVisualAlert(alert: VisualAlert) {
        scope.launch {
            _visualAlert.emit(alert)
            
            if (vibrateOnSound) {
                provideHapticAlert(alert.type)
            }
        }
    }
    
    /**
     * Send success visual alert.
     */
    fun alertSuccess(message: String) {
        sendVisualAlert(VisualAlert(
            type = AlertType.SUCCESS,
            message = message,
            icon = "✓"
        ))
    }
    
    /**
     * Send error visual alert.
     */
    fun alertError(message: String) {
        sendVisualAlert(VisualAlert(
            type = AlertType.ERROR,
            message = message,
            icon = "✕"
        ))
    }
    
    /**
     * Send warning visual alert.
     */
    fun alertWarning(message: String) {
        sendVisualAlert(VisualAlert(
            type = AlertType.WARNING,
            message = message,
            icon = "⚠"
        ))
    }
    
    /**
     * Send info visual alert.
     */
    fun alertInfo(message: String) {
        sendVisualAlert(VisualAlert(
            type = AlertType.INFO,
            message = message,
            icon = "ℹ"
        ))
    }
    
    /**
     * Send notification visual alert.
     */
    fun alertNotification(title: String, message: String) {
        sendVisualAlert(VisualAlert(
            type = AlertType.NOTIFICATION,
            message = "$title: $message",
            icon = "🔔"
        ))
    }
    
    /**
     * Provide haptic feedback for alerts.
     */
    private fun provideHapticAlert(type: AlertType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                AlertType.SUCCESS -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                AlertType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
                AlertType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
                AlertType.INFO -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                AlertType.NOTIFICATION -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50, 50, 100), -1)
                AlertType.TIMER -> VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 200), -1)
                AlertType.SOUND_DETECTED -> VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                AlertType.SUCCESS -> vibrator.vibrate(100)
                AlertType.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
                AlertType.WARNING -> vibrator.vibrate(longArrayOf(0, 150, 100, 150), -1)
                AlertType.INFO -> vibrator.vibrate(50)
                AlertType.NOTIFICATION -> vibrator.vibrate(longArrayOf(0, 50, 50, 50, 50, 100), -1)
                AlertType.TIMER -> vibrator.vibrate(longArrayOf(0, 200, 100, 200, 100, 200), -1)
                AlertType.SOUND_DETECTED -> vibrator.vibrate(75)
            }
        }
    }
    
    /**
     * Set subtitle display duration.
     */
    fun setSubtitleDuration(durationMs: Long) {
        subtitleDurationMs = durationMs
    }
    
    /**
     * Clear subtitle history.
     */
    fun clearHistory() {
        _subtitleQueue.value = emptyList()
    }
    
    /**
     * Release resources.
     */
    fun release() {
        subtitleDisplayJob?.cancel()
        soundMonitorJob?.cancel()
        hideSubtitle()
    }
}

/**
 * Data class for subtitle display.
 */
data class SubtitleData(
    val text: String,
    val speaker: String? = null,
    val type: SubtitleType = SubtitleType.SPEECH,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Timed subtitle for sequenced display.
 */
data class TimedSubtitle(
    val text: String,
    val speaker: String? = null,
    val type: SubtitleType = SubtitleType.SPEECH,
    val delayMs: Long = 0,
    val durationMs: Long = 3000
)

/**
 * Types of subtitles.
 */
enum class SubtitleType {
    SPEECH,          // Regular speech content
    NARRATION,       // Narrator/system voice
    SOUND_EFFECT,    // [Door closes], [Bell rings]
    MUSIC,           // ♪ Music playing ♪
    INSTRUCTION,     // System instructions
    QUIZ_QUESTION,   // Quiz question being read
    FEEDBACK         // Answer feedback
}

/**
 * Visual alert for deaf users.
 */
data class VisualAlert(
    val type: AlertType,
    val message: String,
    val icon: String? = null,
    val durationMs: Long = 3000
)

/**
 * Types of visual alerts.
 */
enum class AlertType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    NOTIFICATION,
    TIMER,
    SOUND_DETECTED
}
