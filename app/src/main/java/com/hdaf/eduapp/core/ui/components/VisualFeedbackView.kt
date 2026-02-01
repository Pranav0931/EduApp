package com.hdaf.eduapp.core.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.AlertType
import com.hdaf.eduapp.accessibility.SubtitleData
import com.hdaf.eduapp.accessibility.SubtitleType
import com.hdaf.eduapp.accessibility.VisualAlert
import com.hdaf.eduapp.databinding.ViewVisualFeedbackBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Visual Feedback View for deaf users.
 * 
 * Displays visual alerts and subtitles with animations for users
 * who cannot hear audio feedback. Integrates with DeafSupportManager.
 */
class VisualFeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewVisualFeedbackBinding = 
        ViewVisualFeedbackBinding.inflate(LayoutInflater.from(context), this)
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var alertHideJob: Job? = null
    private var subtitleHideJob: Job? = null
    
    init {
        // Ensure accessibility is set up properly
        binding.cardVisualAlert.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        binding.cardSubtitle.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    
    // ==================== Visual Alerts ====================
    
    /**
     * Show a visual alert with animation.
     */
    fun showAlert(alert: VisualAlert) {
        // Cancel any pending hide
        alertHideJob?.cancel()
        
        // Update content
        binding.tvAlertIcon.text = alert.icon ?: getDefaultIcon(alert.type)
        binding.tvAlertMessage.text = alert.message
        
        // Set colors based on type
        val backgroundColor = when (alert.type) {
            AlertType.SUCCESS -> context.getColor(R.color.eduai_success)
            AlertType.ERROR -> context.getColor(R.color.eduai_error)
            AlertType.WARNING -> context.getColor(R.color.eduai_warning)
            AlertType.INFO -> context.getColor(R.color.eduai_primary)
            AlertType.NOTIFICATION -> context.getColor(R.color.eduai_secondary)
            AlertType.TIMER -> context.getColor(R.color.eduai_tertiary)
            AlertType.SOUND_DETECTED -> context.getColor(R.color.eduai_surface)
        }
        binding.cardVisualAlert.setCardBackgroundColor(backgroundColor)
        
        // Set text color for contrast
        val textColor = when (alert.type) {
            AlertType.WARNING, AlertType.SOUND_DETECTED -> context.getColor(R.color.eduai_on_surface)
            else -> context.getColor(android.R.color.white)
        }
        binding.tvAlertMessage.setTextColor(textColor)
        binding.tvAlertIcon.setTextColor(textColor)
        
        // Show with animation
        binding.cardVisualAlert.alpha = 0f
        binding.cardVisualAlert.translationY = -50f
        binding.cardVisualAlert.isVisible = true
        
        val fadeIn = ObjectAnimator.ofFloat(binding.cardVisualAlert, View.ALPHA, 0f, 1f)
        val slideIn = ObjectAnimator.ofFloat(binding.cardVisualAlert, View.TRANSLATION_Y, -50f, 0f)
        
        AnimatorSet().apply {
            playTogether(fadeIn, slideIn)
            duration = 250
            interpolator = DecelerateInterpolator()
            start()
        }
        
        // Announce for accessibility
        binding.cardVisualAlert.contentDescription = "${alert.icon ?: ""} ${alert.message}"
        binding.cardVisualAlert.announceForAccessibility(alert.message)
        
        // Auto hide after duration
        alertHideJob = scope.launch {
            delay(alert.durationMs)
            hideAlert()
        }
    }
    
    /**
     * Hide the visual alert with animation.
     */
    fun hideAlert() {
        if (!binding.cardVisualAlert.isVisible) return
        
        val fadeOut = ObjectAnimator.ofFloat(binding.cardVisualAlert, View.ALPHA, 1f, 0f)
        val slideOut = ObjectAnimator.ofFloat(binding.cardVisualAlert, View.TRANSLATION_Y, 0f, -30f)
        
        AnimatorSet().apply {
            playTogether(fadeOut, slideOut)
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.cardVisualAlert.isVisible = false
                }
            })
            start()
        }
    }
    
    // ==================== Subtitles ====================
    
    /**
     * Show subtitle with animation.
     */
    fun showSubtitle(subtitle: SubtitleData) {
        // Cancel any pending hide
        subtitleHideJob?.cancel()
        
        // Format text based on type
        val formattedText = when (subtitle.type) {
            SubtitleType.SOUND_EFFECT -> "[${subtitle.text}]"
            SubtitleType.MUSIC -> "♪ ${subtitle.text} ♪"
            SubtitleType.INSTRUCTION -> "⚡ ${subtitle.text}"
            else -> subtitle.text
        }
        
        binding.tvSubtitleText.text = formattedText
        
        // Show speaker if available
        if (subtitle.speaker != null) {
            binding.tvSubtitleSpeaker.text = "[${subtitle.speaker}]"
            binding.tvSubtitleSpeaker.isVisible = true
        } else {
            binding.tvSubtitleSpeaker.isVisible = false
        }
        
        // Show with animation
        if (!binding.cardSubtitle.isVisible) {
            binding.cardSubtitle.alpha = 0f
            binding.cardSubtitle.translationY = 30f
            binding.cardSubtitle.isVisible = true
            
            val fadeIn = ObjectAnimator.ofFloat(binding.cardSubtitle, View.ALPHA, 0f, 1f)
            val slideIn = ObjectAnimator.ofFloat(binding.cardSubtitle, View.TRANSLATION_Y, 30f, 0f)
            
            AnimatorSet().apply {
                playTogether(fadeIn, slideIn)
                duration = 200
                interpolator = DecelerateInterpolator()
                start()
            }
        }
        
        // Accessibility
        val fullText = if (subtitle.speaker != null) {
            "${subtitle.speaker}: ${subtitle.text}"
        } else {
            subtitle.text
        }
        binding.cardSubtitle.contentDescription = fullText
    }
    
    /**
     * Hide subtitle with animation.
     */
    fun hideSubtitle() {
        if (!binding.cardSubtitle.isVisible) return
        
        val fadeOut = ObjectAnimator.ofFloat(binding.cardSubtitle, View.ALPHA, 1f, 0f)
        val slideOut = ObjectAnimator.ofFloat(binding.cardSubtitle, View.TRANSLATION_Y, 0f, 20f)
        
        AnimatorSet().apply {
            playTogether(fadeOut, slideOut)
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.cardSubtitle.isVisible = false
                }
            })
            start()
        }
    }
    
    /**
     * Show subtitle for a specific duration then auto-hide.
     */
    fun showSubtitleTimed(subtitle: SubtitleData, durationMs: Long = 3000L) {
        showSubtitle(subtitle)
        
        subtitleHideJob = scope.launch {
            delay(durationMs)
            hideSubtitle()
        }
    }
    
    // ==================== Helper Functions ====================
    
    private fun getDefaultIcon(type: AlertType): String {
        return when (type) {
            AlertType.SUCCESS -> "✓"
            AlertType.ERROR -> "✕"
            AlertType.WARNING -> "⚠"
            AlertType.INFO -> "ℹ"
            AlertType.NOTIFICATION -> "🔔"
            AlertType.TIMER -> "⏱"
            AlertType.SOUND_DETECTED -> "🔊"
        }
    }
    
    /**
     * Convenience method to show success alert.
     */
    fun showSuccess(message: String) {
        showAlert(VisualAlert(
            type = AlertType.SUCCESS,
            message = message,
            icon = "✓"
        ))
    }
    
    /**
     * Convenience method to show error alert.
     */
    fun showError(message: String) {
        showAlert(VisualAlert(
            type = AlertType.ERROR,
            message = message,
            icon = "✕"
        ))
    }
    
    /**
     * Convenience method to show warning alert.
     */
    fun showWarning(message: String) {
        showAlert(VisualAlert(
            type = AlertType.WARNING,
            message = message,
            icon = "⚠"
        ))
    }
    
    /**
     * Convenience method to show info alert.
     */
    fun showInfo(message: String) {
        showAlert(VisualAlert(
            type = AlertType.INFO,
            message = message,
            icon = "ℹ"
        ))
    }
    
    /**
     * Clean up resources.
     */
    fun release() {
        alertHideJob?.cancel()
        subtitleHideJob?.cancel()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}
