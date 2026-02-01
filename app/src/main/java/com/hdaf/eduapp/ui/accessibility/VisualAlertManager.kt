package com.hdaf.eduapp.ui.accessibility

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.AlertType
import com.hdaf.eduapp.accessibility.VisualAlert
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages visual alert display for deaf users.
 * Shows colored pop-up alerts instead of audio notifications.
 */
@Singleton
class VisualAlertManager @Inject constructor() {
    
    private var container: ViewGroup? = null
    private var context: Context? = null
    private var alertView: View? = null
    private var alertCard: MaterialCardView? = null
    private var alertIcon: ImageView? = null
    private var alertTitle: TextView? = null
    private var alertMessage: TextView? = null
    private var dismissButton: ImageButton? = null
    
    private var autoDismissDelayMs: Long = 4000
    private var dismissRunnable: Runnable? = null
    private var hapticEnabled: Boolean = true
    
    private val vibrator: Vibrator? by lazy {
        context?.let { ctx ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }
    }
    
    /**
     * Initialize with just a context for toast-based alerts.
     */
    fun initialize(context: Context) {
        this.context = context
    }
    
    /**
     * Initialize the visual alert overlay with a container.
     */
    fun initialize(container: ViewGroup) {
        this.container = container
        this.context = container.context
        setupAlertView()
    }
    
    private fun setupAlertView() {
        val container = this.container ?: return
        if (alertView != null) return
        
        val inflater = LayoutInflater.from(container.context)
        alertView = inflater.inflate(R.layout.layout_visual_alert, container, false)
        
        alertView?.let { view ->
            alertCard = view.findViewById(R.id.alertCard)
            alertIcon = view.findViewById(R.id.alertIcon)
            alertTitle = view.findViewById(R.id.alertTitle)
            alertMessage = view.findViewById(R.id.alertMessage)
            dismissButton = view.findViewById(R.id.alertDismissButton)
            
            dismissButton?.setOnClickListener { hideAlert() }
            
            // Add to top of container
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
            container.addView(view, params)
        }
    }
    
    /**
     * Show success alert (convenience method).
     */
    fun showSuccessAlert(message: String) {
        showQuickAlert(AlertType.SUCCESS, message)
    }
    
    /**
     * Show error alert (convenience method).
     */
    fun showErrorAlert(message: String) {
        showQuickAlert(AlertType.ERROR, message)
    }
    
    /**
     * Show warning alert (convenience method).
     */
    fun showWarningAlert(message: String) {
        showQuickAlert(AlertType.WARNING, message)
    }
    
    /**
     * Show info alert (convenience method).
     */
    fun showInfoAlert(message: String) {
        showQuickAlert(AlertType.INFO, message)
    }
    
    /**
     * Apply accessibility profile settings.
     */
    fun applyProfile(profile: AccessibilityProfile) {
        hapticEnabled = profile.hapticFeedbackEnabled
    }
    
    /**
     * Show visual alert.
     */
    fun showAlert(alert: VisualAlert) {
        // If no container, use toast fallback
        if (container == null) {
            showToastAlert(alert)
            return
        }
        
        setupAlertView()
        
        // Cancel any pending dismissal
        dismissRunnable?.let { alertView?.removeCallbacks(it) }
        
        // Configure alert appearance based on type
        val (backgroundColor, iconRes) = when (alert.type) {
            AlertType.SUCCESS -> Color.parseColor("#4CAF50") to android.R.drawable.ic_dialog_info
            AlertType.ERROR -> Color.parseColor("#F44336") to android.R.drawable.ic_dialog_alert
            AlertType.WARNING -> Color.parseColor("#FF9800") to android.R.drawable.ic_dialog_alert
            AlertType.INFO -> Color.parseColor("#2196F3") to android.R.drawable.ic_dialog_info
            AlertType.NOTIFICATION -> Color.parseColor("#9C27B0") to android.R.drawable.ic_popup_reminder
            AlertType.TIMER -> Color.parseColor("#FF5722") to android.R.drawable.ic_menu_recent_history
            AlertType.SOUND_DETECTED -> Color.parseColor("#00BCD4") to android.R.drawable.ic_lock_silent_mode_off
        }
        
        alertCard?.setCardBackgroundColor(backgroundColor)
        alertIcon?.setImageResource(iconRes)
        
        // Get title from alert type
        val title = when (alert.type) {
            AlertType.SUCCESS -> "Success ✓"
            AlertType.ERROR -> "Error ✗"
            AlertType.WARNING -> "Warning ⚠"
            AlertType.INFO -> "Info ℹ"
            AlertType.NOTIFICATION -> "Notification"
            AlertType.TIMER -> "Timer ⏱"
            AlertType.SOUND_DETECTED -> "Sound Detected 🔊"
        }
        
        alertTitle?.text = title
        alertMessage?.text = alert.message
        
        // Show message only if not empty
        alertMessage?.isVisible = alert.message.isNotBlank()
        
        // Animate in
        alertView?.apply {
            if (!isVisible) {
                visibility = View.VISIBLE
                translationY = -height.toFloat()
                alpha = 0f
                animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(250)
                    .start()
            }
        }
        
        // Haptic feedback
        if (hapticEnabled) {
            provideHapticFeedback(alert.type)
        }
        
        // Auto dismiss
        dismissRunnable = Runnable { hideAlert() }
        alertView?.postDelayed(dismissRunnable!!, autoDismissDelayMs)
        
        // Announce for accessibility
        val announcementTitle = alertTitle?.text ?: ""
        alertTitle?.announceForAccessibility("$announcementTitle. ${alert.message}")
    }
    
    private fun showToastAlert(alert: VisualAlert) {
        val emoji = when (alert.type) {
            AlertType.SUCCESS -> "✅"
            AlertType.ERROR -> "❌"
            AlertType.WARNING -> "⚠️"
            AlertType.INFO -> "ℹ️"
            AlertType.NOTIFICATION -> "🔔"
            AlertType.TIMER -> "⏱️"
            AlertType.SOUND_DETECTED -> "🔊"
        }
        
        context?.let { ctx ->
            Toast.makeText(ctx, "$emoji ${alert.message}", Toast.LENGTH_LONG).show()
            provideHapticFeedback(alert.type)
        }
    }
    
    private fun provideHapticFeedback(type: AlertType) {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    AlertType.SUCCESS -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    AlertType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
                    AlertType.WARNING -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(100)
            }
        }
    }
    
    /**
     * Show quick alert with just a message.
     */
    fun showQuickAlert(type: AlertType, message: String) {
        showAlert(VisualAlert(type, message))
    }
    
    /**
     * Hide alert with animation.
     */
    fun hideAlert() {
        dismissRunnable?.let { alertView?.removeCallbacks(it) }
        
        val viewHeight = alertView?.height?.toFloat() ?: 100f
        alertView?.animate()
            ?.translationY(-viewHeight)
            ?.alpha(0f)
            ?.setDuration(200)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    alertView?.visibility = View.GONE
                }
            })
            ?.start()
    }
    
    /**
     * Set auto-dismiss delay.
     */
    fun setAutoDismissDelay(delayMs: Long) {
        autoDismissDelayMs = delayMs
    }
    
    /**
     * Check if alert is currently visible.
     */
    fun isShowing(): Boolean = alertView?.isVisible == true
    
    /**
     * Release resources.
     */
    fun release() {
        dismissRunnable?.let { alertView?.removeCallbacks(it) }
        alertView?.let { container?.removeView(it) }
        alertView = null
        container = null
    }
    
    /**
     * Clean up resources.
     */
    fun destroy() {
        release()
    }
}
