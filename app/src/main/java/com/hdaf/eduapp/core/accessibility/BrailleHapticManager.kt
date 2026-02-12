package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Braille-like Haptic Patterns for Blind Navigation.
 * 
 * Uses distinct vibration patterns to convey information:
 * - Navigation cues (left, right, up, down)
 * - Content type identification
 * - Progress indicators
 * - Error/success feedback
 * - Numbers and letters (simplified Braille-inspired patterns)
 * 
 * Helps blind users navigate without audio.
 */
@Singleton
class BrailleHapticManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // ========== Navigation Patterns ==========
    
    /**
     * Indicate direction for navigation.
     */
    fun indicateDirection(direction: Direction) {
        if (!_isEnabled.value || !vibrator.hasVibrator()) return
        
        val pattern = when (direction) {
            Direction.LEFT -> longArrayOf(0, 50, 0)  // Single short
            Direction.RIGHT -> longArrayOf(0, 50, 50, 50)  // Two short
            Direction.UP -> longArrayOf(0, 100)  // Single long
            Direction.DOWN -> longArrayOf(0, 100, 50, 100)  // Two long
            Direction.FORWARD -> longArrayOf(0, 50, 30, 50, 30, 50)  // Three short ascending
            Direction.BACK -> longArrayOf(0, 100, 50, 75, 50, 50)  // Three descending
        }
        
        vibrate(pattern)
    }
    
    /**
     * Indicate current position in a list.
     */
    fun indicateListPosition(current: Int, total: Int) {
        if (!_isEnabled.value || !vibrator.hasVibrator()) return
        
        // At beginning
        if (current == 1) {
            vibrate(longArrayOf(0, 200))  // Long pulse = at start
            return
        }
        
        // At end
        if (current == total) {
            vibrate(longArrayOf(0, 200, 100, 200))  // Two long pulses = at end
            return
        }
        
        // In middle - short pulse
        vibrate(longArrayOf(0, 50))
    }
    
    // ========== Content Type Patterns ==========
    
    /**
     * Indicate type of content user is on.
     */
    fun indicateContentType(type: ContentHapticType) {
        if (!_isEnabled.value || !vibrator.hasVibrator()) return
        
        val pattern = when (type) {
            ContentHapticType.HEADING -> longArrayOf(0, 150, 50, 150)  // Two medium
            ContentHapticType.PARAGRAPH -> longArrayOf(0, 30)  // Very short
            ContentHapticType.LINK -> longArrayOf(0, 50, 30, 50, 30, 50)  // Three quick
            ContentHapticType.BUTTON -> longArrayOf(0, 100, 50, 50)  // Long + short
            ContentHapticType.IMAGE -> longArrayOf(0, 75, 75, 75, 75, 75)  // Three equal
            ContentHapticType.LIST_ITEM -> longArrayOf(0, 40, 40, 40)  // Two short
            ContentHapticType.INPUT_FIELD -> longArrayOf(0, 100, 100, 100)  // Two medium spaced
            ContentHapticType.CHECKBOX -> longArrayOf(0, 50, 100, 100)  // Short + long
            ContentHapticType.AUDIO -> longArrayOf(0, 100, 50, 50, 50, 50)  // Wave pattern
            ContentHapticType.VIDEO -> longArrayOf(0, 50, 50, 100, 50, 100)  // Rising pattern
        }
        
        vibrate(pattern)
    }
    
    // ========== Progress Patterns ==========
    
    /**
     * Indicate progress percentage through haptic feedback.
     */
    fun indicateProgress(percent: Int) {
        if (!_isEnabled.value || !vibrator.hasVibrator()) return
        
        val pattern = when {
            percent == 0 -> longArrayOf(0, 30)  // Tiny
            percent < 25 -> longArrayOf(0, 50)  // Short
            percent < 50 -> longArrayOf(0, 100)  // Medium
            percent < 75 -> longArrayOf(0, 150)  // Long
            percent < 100 -> longArrayOf(0, 200)  // Longer
            else -> longArrayOf(0, 100, 50, 100, 50, 200)  // Complete celebration
        }
        
        vibrate(pattern)
    }
    
    /**
     * Indicate loading state with pulsing pattern.
     */
    fun startLoadingPulse(): Job {
        return scope.launch {
            while (isActive && _isEnabled.value) {
                vibrate(longArrayOf(0, 50, 150))
                delay(400)
            }
        }
    }
    
    // ========== Number Patterns (Braille-inspired) ==========
    
    /**
     * Vibrate a number pattern (1-10).
     * Uses Braille-inspired patterns for digits.
     */
    fun vibrateNumber(number: Int) {
        if (!_isEnabled.value || !vibrator.hasVibrator()) return
        
        val pattern = when (number) {
            0 -> longArrayOf(0, 200)  // Long single
            1 -> longArrayOf(0, 50)  // Dot 1
            2 -> longArrayOf(0, 50, 50, 50)  // Dots 1,2
            3 -> longArrayOf(0, 50, 30, 100)  // Dots 1,4
            4 -> longArrayOf(0, 50, 30, 100, 30, 50)  // Dots 1,4,5
            5 -> longArrayOf(0, 50, 30, 50, 30, 50)  // Dots 1,5
            6 -> longArrayOf(0, 50, 30, 100, 30, 100)  // Dots 1,2,4
            7 -> longArrayOf(0, 50, 30, 100, 30, 100, 30, 50)  // Dots 1,2,4,5
            8 -> longArrayOf(0, 50, 30, 50, 30, 100)  // Dots 1,2,5
            9 -> longArrayOf(0, 100, 30, 100)  // Dots 2,4
            10 -> longArrayOf(0, 50, 100, 200)  // Special for 10
            else -> longArrayOf(0, 50)
        }
        
        vibrate(pattern)
    }
    
    // ========== Quiz Feedback Patterns ==========
    
    /**
     * Indicate quiz option selection.
     */
    fun indicateOptionSelected(optionNumber: Int) {
        if (!_isEnabled.value) return
        
        // Quick confirmation + number pattern
        scope.launch {
            vibrate(longArrayOf(0, 30))  // Selection click
            delay(100)
            vibrateNumber(optionNumber)
        }
    }
    
    /**
     * Indicate correct/incorrect answer with distinct patterns.
     */
    fun indicateQuizResult(isCorrect: Boolean) {
        if (!_isEnabled.value) return
        
        if (isCorrect) {
            // Rising celebratory pattern
            vibrate(longArrayOf(0, 50, 50, 75, 50, 100, 50, 150))
        } else {
            // Falling sad pattern
            vibrate(longArrayOf(0, 150, 100, 100, 100, 50))
        }
    }
    
    /**
     * Indicate score with proportional vibration.
     */
    fun indicateScore(score: Int, maxScore: Int) {
        if (!_isEnabled.value) return
        
        val percent = (score * 100) / maxScore
        scope.launch {
            // Number of pulses based on percentage
            val pulseCount = (percent / 20).coerceIn(1, 5)
            repeat(pulseCount) {
                vibrate(longArrayOf(0, 100))
                delay(150)
            }
        }
    }
    
    // ========== Reading Position Patterns ==========
    
    /**
     * Indicate current reading position in chapter.
     */
    fun indicateReadingPosition(
        currentParagraph: Int,
        totalParagraphs: Int
    ) {
        if (!_isEnabled.value) return
        
        when {
            currentParagraph == 1 -> {
                // Start of chapter
                vibrate(longArrayOf(0, 100, 50, 50, 50, 50))  // Long + two short
            }
            currentParagraph == totalParagraphs -> {
                // End of chapter
                vibrate(longArrayOf(0, 50, 50, 50, 50, 100))  // Two short + long
            }
            else -> {
                // Middle - proportional pulse
                val progress = (currentParagraph * 100) / totalParagraphs
                indicateProgress(progress)
            }
        }
    }
    
    // ========== Utility Methods ==========
    
    private fun vibrate(pattern: LongArray) {
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
    
    fun release() {
        scope.cancel()
    }
}

/**
 * Navigation directions.
 */
enum class Direction {
    LEFT, RIGHT, UP, DOWN, FORWARD, BACK
}

/**
 * Content types for haptic identification.
 */
enum class ContentHapticType {
    HEADING,
    PARAGRAPH,
    LINK,
    BUTTON,
    IMAGE,
    LIST_ITEM,
    INPUT_FIELD,
    CHECKBOX,
    AUDIO,
    VIDEO
}
