package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Visual Alert System for Deaf Students.
 * 
 * Provides visual alternatives to audio cues:
 * - Screen flash for notifications
 * - Color-coded overlays for different alert types
 * - Pulsing borders for attention
 * - Vibration patterns synchronized with visual alerts
 * 
 * Bilingual support: English and Hindi
 */
@Singleton
class DeafAlertManager @Inject constructor(
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
    
    private val _currentAlert = MutableStateFlow<DeafAlert?>(null)
    val currentAlert: StateFlow<DeafAlert?> = _currentAlert.asStateFlow()
    
    private var flashOverlay: View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Show visual alert for deaf users.
     */
    fun showAlert(
        type: AlertType,
        message: String,
        messageHindi: String,
        duration: Long = 2000L
    ) {
        if (!_isEnabled.value) return
        
        _currentAlert.value = DeafAlert(type, message, messageHindi)
        
        // Vibrate based on alert type
        vibrateForAlert(type)
        
        // Auto-dismiss after duration
        scope.launch {
            delay(duration)
            _currentAlert.value = null
        }
    }
    
    /**
     * Flash screen for important notifications.
     */
    fun flashScreen(rootView: View, times: Int = 3, color: Int = Color.WHITE) {
        if (!_isEnabled.value) return
        
        val overlay = View(context).apply {
            setBackgroundColor(color)
            alpha = 0f
        }
        
        if (rootView is FrameLayout) {
            rootView.addView(overlay, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            flashOverlay = overlay
            
            scope.launch {
                repeat(times) {
                    overlay.animate()
                        .alpha(0.7f)
                        .setDuration(100)
                        .withEndAction {
                            overlay.animate()
                                .alpha(0f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                    delay(300)
                }
                rootView.removeView(overlay)
                flashOverlay = null
            }
        }
    }
    
    /**
     * Vibration patterns for different alerts.
     */
    private fun vibrateForAlert(type: AlertType) {
        if (!vibrator.hasVibrator()) return
        
        val pattern = when (type) {
            AlertType.SUCCESS -> longArrayOf(0, 100, 50, 100)  // Two short pulses
            AlertType.ERROR -> longArrayOf(0, 200, 100, 200, 100, 200)  // Three long pulses
            AlertType.WARNING -> longArrayOf(0, 150, 75, 150)  // Two medium pulses
            AlertType.INFO -> longArrayOf(0, 75)  // Single short pulse
            AlertType.QUIZ_CORRECT -> longArrayOf(0, 50, 50, 50, 50, 150)  // Celebratory pattern
            AlertType.QUIZ_WRONG -> longArrayOf(0, 300, 100, 300)  // Two long sad pulses
            AlertType.TIMER_WARNING -> longArrayOf(0, 100, 100, 100, 100, 100)  // Urgent pulses
            AlertType.NEW_MESSAGE -> longArrayOf(0, 50, 100, 50)  // Notification pattern
            AlertType.CHAPTER_COMPLETE -> longArrayOf(0, 100, 50, 100, 50, 200)  // Achievement pattern
            AlertType.BADGE_EARNED -> longArrayOf(0, 50, 50, 50, 50, 50, 50, 200)  // Celebration
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Show visual countdown timer overlay.
     */
    fun startVisualCountdown(
        seconds: Int,
        onTick: (Int) -> Unit,
        onFinish: () -> Unit
    ): Job {
        return scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                onTick(remaining)
                
                // Vibrate on last 10 seconds
                if (remaining <= 10) {
                    vibrateForAlert(AlertType.TIMER_WARNING)
                }
                
                delay(1000)
                remaining--
            }
            onFinish()
        }
    }
    
    /**
     * Enable/disable deaf alerts.
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
    
    /**
     * Get accessible description for alert.
     */
    fun getAlertDescription(type: AlertType, isHindi: Boolean): String {
        return when (type) {
            AlertType.SUCCESS -> if (isHindi) "सफलता" else "Success"
            AlertType.ERROR -> if (isHindi) "त्रुटि" else "Error"
            AlertType.WARNING -> if (isHindi) "चेतावनी" else "Warning"
            AlertType.INFO -> if (isHindi) "जानकारी" else "Information"
            AlertType.QUIZ_CORRECT -> if (isHindi) "सही उत्तर!" else "Correct Answer!"
            AlertType.QUIZ_WRONG -> if (isHindi) "गलत उत्तर" else "Wrong Answer"
            AlertType.TIMER_WARNING -> if (isHindi) "समय समाप्त हो रहा है" else "Time Running Out"
            AlertType.NEW_MESSAGE -> if (isHindi) "नया संदेश" else "New Message"
            AlertType.CHAPTER_COMPLETE -> if (isHindi) "अध्याय पूर्ण!" else "Chapter Complete!"
            AlertType.BADGE_EARNED -> if (isHindi) "बैज अर्जित!" else "Badge Earned!"
        }
    }
    
    fun release() {
        scope.cancel()
        flashOverlay = null
    }
}

/**
 * Alert types with associated colors.
 */
enum class AlertType(val color: Int, val iconRes: String) {
    SUCCESS(Color.parseColor("#4CAF50"), "✓"),
    ERROR(Color.parseColor("#F44336"), "✗"),
    WARNING(Color.parseColor("#FF9800"), "⚠"),
    INFO(Color.parseColor("#2196F3"), "ℹ"),
    QUIZ_CORRECT(Color.parseColor("#4CAF50"), "🎉"),
    QUIZ_WRONG(Color.parseColor("#F44336"), "❌"),
    TIMER_WARNING(Color.parseColor("#FF5722"), "⏱"),
    NEW_MESSAGE(Color.parseColor("#9C27B0"), "💬"),
    CHAPTER_COMPLETE(Color.parseColor("#8BC34A"), "📖"),
    BADGE_EARNED(Color.parseColor("#FFD700"), "🏆")
}

/**
 * Visual alert data.
 */
data class DeafAlert(
    val type: AlertType,
    val message: String,
    val messageHindi: String
)
