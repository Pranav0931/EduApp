package com.hdaf.eduapp.ui.accessibility

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.SubtitleData
import com.hdaf.eduapp.accessibility.SubtitleType
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.ContrastLevel

/**
 * Manages subtitle overlay display for deaf users.
 * Can be attached to any ViewGroup to show subtitles.
 */
class SubtitleOverlayManager(
    private val container: ViewGroup
) {
    
    private var overlayView: View? = null
    private var subtitleCard: MaterialCardView? = null
    private var speakerText: TextView? = null
    private var subtitleText: TextView? = null
    private var soundEffectIndicator: View? = null
    private var soundEffectText: TextView? = null
    
    private var isHighContrast: Boolean = false
    private var autoDismissDelayMs: Long = 5000
    private var dismissRunnable: Runnable? = null
    
    /**
     * Initialize the subtitle overlay.
     */
    fun initialize() {
        if (overlayView != null) return
        
        val inflater = LayoutInflater.from(container.context)
        overlayView = inflater.inflate(R.layout.layout_subtitle_overlay, container, false)
        
        overlayView?.let { view ->
            subtitleCard = view.findViewById(R.id.subtitleCard)
            speakerText = view.findViewById(R.id.speakerText)
            subtitleText = view.findViewById(R.id.subtitleText)
            soundEffectIndicator = view.findViewById(R.id.soundEffectIndicator)
            soundEffectText = view.findViewById(R.id.soundEffectText)
            
            // Add to bottom of container
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            container.addView(view, params)
        }
    }
    
    /**
     * Apply accessibility profile settings.
     */
    fun applyProfile(profile: AccessibilityProfile) {
        isHighContrast = profile.contrastLevel == ContrastLevel.HIGH || 
                        profile.contrastLevel == ContrastLevel.MAXIMUM
        
        subtitleCard?.let { card ->
            if (isHighContrast) {
                card.setCardBackgroundColor(Color.BLACK)
                subtitleText?.setTextColor(Color.WHITE)
                subtitleText?.textSize = 22f
            } else {
                card.setCardBackgroundColor(Color.parseColor("#CC000000"))
                subtitleText?.setTextColor(Color.WHITE)
                subtitleText?.textSize = 18f
            }
        }
    }
    
    /**
     * Show subtitle with animation.
     */
    fun showSubtitle(data: SubtitleData) {
        initialize()
        
        // Cancel any pending dismissal
        dismissRunnable?.let { overlayView?.removeCallbacks(it) }
        
        // Update content
        speakerText?.apply {
            text = if (data.speaker != null) "[${data.speaker}]" else ""
            isVisible = data.speaker != null
        }
        
        val prefix = when (data.type) {
            SubtitleType.SOUND_EFFECT -> "♪ "
            SubtitleType.MUSIC -> "🎵 "
            SubtitleType.INSTRUCTION -> "ℹ️ "
            else -> ""
        }
        subtitleText?.text = prefix + data.text
        
        // Show sound effect indicator if applicable
        soundEffectIndicator?.isVisible = data.type == SubtitleType.SOUND_EFFECT || 
                                          data.type == SubtitleType.MUSIC
        soundEffectText?.text = when (data.type) {
            SubtitleType.SOUND_EFFECT -> data.text
            SubtitleType.MUSIC -> "Music playing"
            else -> ""
        }
        
        // Animate in
        overlayView?.apply {
            if (!isVisible) {
                visibility = View.VISIBLE
                translationY = height.toFloat()
                animate()
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }
        
        // Auto dismiss
        dismissRunnable = Runnable { hideSubtitle() }
        overlayView?.postDelayed(dismissRunnable!!, autoDismissDelayMs)
        
        // Announce for accessibility
        subtitleText?.announceForAccessibility(data.text)
    }
    
    /**
     * Hide subtitle with animation.
     */
    fun hideSubtitle() {
        dismissRunnable?.let { overlayView?.removeCallbacks(it) }
        
        overlayView?.animate()
            ?.translationY(overlayView?.height?.toFloat() ?: 100f)
            ?.setDuration(200)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    overlayView?.visibility = View.GONE
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
     * Check if subtitles are currently visible.
     */
    fun isShowing(): Boolean = overlayView?.isVisible == true
    
    /**
     * Clean up resources.
     */
    fun destroy() {
        dismissRunnable?.let { overlayView?.removeCallbacks(it) }
        overlayView?.let { container.removeView(it) }
        overlayView = null
    }
}
