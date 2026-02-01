package com.hdaf.eduapp.ui.accessibility

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.card.MaterialCardView
import com.hdaf.eduapp.domain.model.AccessibilityProfile
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.model.ContrastLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * View-based accessibility helper for the EduApp.
 * Applies accessibility modifications to existing Views based on user profile.
 */
@Singleton
class AccessibilityViewHelper @Inject constructor(
    private val context: Context
) {
    
    private val accessibilityManager: AccessibilityManager? by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    }
    
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Apply accessibility settings to a view hierarchy based on profile.
     */
    fun applyAccessibilitySettings(rootView: View, profile: AccessibilityProfile) {
        when (profile.accessibilityMode) {
            AccessibilityModeType.BLIND -> applyBlindSettings(rootView, profile)
            AccessibilityModeType.LOW_VISION -> applyLowVisionSettings(rootView, profile)
            AccessibilityModeType.DEAF -> applyDeafSettings(rootView, profile)
            AccessibilityModeType.SLOW_LEARNER -> applySlowLearnerSettings(rootView, profile)
            AccessibilityModeType.NORMAL -> applyRegularSettings(rootView, profile)
        }
        
        // Apply universal accessibility improvements
        applyUniversalSettings(rootView, profile)
    }
    
    /**
     * Settings for blind users - focus on touch targets and audio feedback.
     */
    private fun applyBlindSettings(rootView: View, profile: AccessibilityProfile) {
        processViewHierarchy(rootView) { view ->
            // Ensure all interactive elements have content descriptions
            ensureContentDescription(view)
            
            // Increase touch targets to at least 48dp
            enlargeTouchTarget(view, minTouchTargetDp = 56)
            
            // Make focus more obvious with larger focus indicators
            if (view.isFocusable) {
                view.isFocusableInTouchMode = true
            }
            
            // Set up accessibility delegate for custom announcements
            if (view is Button || view is ImageButton) {
                setupCustomAccessibilityDelegate(view)
            }
        }
    }
    
    /**
     * Settings for low vision users - focus on contrast and text size.
     */
    private fun applyLowVisionSettings(rootView: View, profile: AccessibilityProfile) {
        processViewHierarchy(rootView) { view ->
            when (view) {
                is TextView -> applyLowVisionTextSettings(view, profile)
                is MaterialCardView -> applyHighContrastCard(view, profile.contrastLevel)
            }
            
            // Ensure adequate touch targets
            enlargeTouchTarget(view, minTouchTargetDp = 52)
        }
    }
    
    /**
     * Settings for deaf users - visual feedback instead of audio.
     */
    private fun applyDeafSettings(rootView: View, profile: AccessibilityProfile) {
        processViewHierarchy(rootView) { view ->
            // Enable visual feedback for interactive elements
            if (view is Button || view is ImageButton) {
                setupVisualFeedback(view, profile)
            }
        }
    }
    
    /**
     * Settings for slow learners - simplified UI and larger text.
     */
    private fun applySlowLearnerSettings(rootView: View, profile: AccessibilityProfile) {
        processViewHierarchy(rootView) { view ->
            when (view) {
                is TextView -> {
                    // Increase readability
                    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 
                        view.textSize / context.resources.displayMetrics.scaledDensity * 1.15f)
                    view.letterSpacing = 0.03f
                    view.setLineSpacing(4f, 1.2f)
                }
            }
            
            // Larger touch targets for easier interaction
            enlargeTouchTarget(view, minTouchTargetDp = 52)
        }
    }
    
    /**
     * Settings for regular users - respect user preferences.
     */
    private fun applyRegularSettings(rootView: View, profile: AccessibilityProfile) {
        // Apply any user-specific preferences
        if (profile.largeTextEnabled) {
            processViewHierarchy(rootView) { view ->
                if (view is TextView) {
                    val currentSize = view.textSize / context.resources.displayMetrics.scaledDensity
                    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize * 1.2f)
                }
            }
        }
    }
    
    /**
     * Universal accessibility improvements applied to all profiles.
     */
    private fun applyUniversalSettings(rootView: View, profile: AccessibilityProfile) {
        processViewHierarchy(rootView) { view ->
            // Ensure minimum touch target size
            if (view.isClickable || view.isFocusable) {
                enlargeTouchTarget(view, minTouchTargetDp = 48)
            }
            
            // Improve focus navigation
            if (view.isFocusable) {
                view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }
    }
    
    /**
     * Apply text settings for low vision users.
     */
    private fun applyLowVisionTextSettings(textView: TextView, profile: AccessibilityProfile) {
        val scaleFactor = when (profile.contrastLevel) {
            ContrastLevel.NORMAL -> 1.0f
            ContrastLevel.MEDIUM -> 1.15f
            ContrastLevel.HIGH -> 1.3f
            ContrastLevel.MAXIMUM -> 1.5f
            ContrastLevel.INVERTED -> 1.3f
        }
        
        val currentSize = textView.textSize / context.resources.displayMetrics.scaledDensity
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize * scaleFactor)
        
        // Apply high contrast colors
        when (profile.contrastLevel) {
            ContrastLevel.HIGH, ContrastLevel.MAXIMUM -> {
                textView.setTextColor(Color.BLACK)
                textView.setBackgroundColor(Color.WHITE)
                textView.setTypeface(textView.typeface, Typeface.BOLD)
            }
            ContrastLevel.INVERTED -> {
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundColor(Color.BLACK)
            }
            else -> { /* Keep default colors */ }
        }
    }
    
    /**
     * Apply high contrast styling to cards.
     */
    private fun applyHighContrastCard(card: MaterialCardView, contrastLevel: ContrastLevel) {
        when (contrastLevel) {
            ContrastLevel.HIGH, ContrastLevel.MAXIMUM -> {
                card.setCardBackgroundColor(Color.WHITE)
                card.strokeColor = Color.BLACK
                card.strokeWidth = dpToPx(3)
                card.cardElevation = dpToPx(8).toFloat()
            }
            ContrastLevel.INVERTED -> {
                card.setCardBackgroundColor(Color.BLACK)
                card.strokeColor = Color.WHITE
                card.strokeWidth = dpToPx(2)
            }
            else -> { /* Keep default styling */ }
        }
    }
    
    /**
     * Set up visual feedback for deaf users.
     */
    private fun setupVisualFeedback(view: View, profile: AccessibilityProfile) {
        view.setOnClickListener { originalView ->
            // Flash/highlight feedback
            val originalBackground = originalView.background
            originalView.setBackgroundColor(Color.parseColor("#FFEB3B")) // Yellow flash
            originalView.postDelayed({
                originalView.background = originalBackground
            }, 150)
            
            // Vibration feedback if enabled
            if (profile.hapticFeedbackEnabled) {
                provideHapticFeedback(HapticType.CLICK)
            }
        }
    }
    
    /**
     * Ensure content description is set for accessibility.
     */
    private fun ensureContentDescription(view: View) {
        if (view.contentDescription.isNullOrEmpty()) {
            when (view) {
                is Button -> {
                    view.contentDescription = view.text
                }
                is ImageButton -> {
                    view.contentDescription = "Button"
                }
                is ImageView -> {
                    // Try to get description from drawable or set generic
                    view.contentDescription = view.drawable?.let { "Image" } ?: ""
                }
                is EditText -> {
                    view.contentDescription = view.hint ?: "Text input"
                }
            }
        }
    }
    
    /**
     * Enlarge touch target to meet minimum size requirements.
     */
    private fun enlargeTouchTarget(view: View, minTouchTargetDp: Int) {
        if (!view.isClickable && !view.isFocusable) return
        
        val minSize = dpToPx(minTouchTargetDp)
        
        view.post {
            val currentWidth = view.width
            val currentHeight = view.height
            
            if (currentWidth < minSize || currentHeight < minSize) {
                val params = view.layoutParams
                if (params != null) {
                    if (currentWidth < minSize) {
                        params.width = minSize
                    }
                    if (currentHeight < minSize) {
                        params.height = minSize
                    }
                    view.layoutParams = params
                }
                
                // Also add touch delegate for expanded touch area
                val parent = view.parent
                if (parent is ViewGroup) {
                    parent.post {
                        val hitRect = android.graphics.Rect()
                        view.getHitRect(hitRect)
                        val extraPadding = (minSize - currentWidth.coerceAtMost(currentHeight)) / 2
                        hitRect.left -= extraPadding
                        hitRect.top -= extraPadding
                        hitRect.right += extraPadding
                        hitRect.bottom += extraPadding
                        parent.touchDelegate = android.view.TouchDelegate(hitRect, view)
                    }
                }
            }
        }
    }
    
    /**
     * Set up custom accessibility delegate for announcements.
     */
    private fun setupCustomAccessibilityDelegate(view: View) {
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                // Add custom actions or hints
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        "Activate"
                    )
                )
            }
        })
    }
    
    /**
     * Process all views in a hierarchy.
     */
    private fun processViewHierarchy(view: View, action: (View) -> Unit) {
        action(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                processViewHierarchy(view.getChildAt(i), action)
            }
        }
    }
    
    /**
     * Provide haptic feedback.
     */
    fun provideHapticFeedback(type: HapticType) {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (type) {
                    HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticType.DOUBLE_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    HapticType.SUCCESS -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.ERROR -> VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 100, 100), -1
                    )
                    HapticType.WARNING -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.NAVIGATION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(50)
            }
        }
    }
    
    /**
     * Announce message to accessibility services.
     */
    fun announceForAccessibility(view: View, message: String) {
        if (accessibilityManager?.isEnabled == true) {
            view.announceForAccessibility(message)
        }
    }
    
    /**
     * Request focus for TalkBack.
     */
    fun requestAccessibilityFocus(view: View) {
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        view.requestFocus()
    }
    
    /**
     * Check if screen reader is active.
     */
    fun isScreenReaderActive(): Boolean {
        return accessibilityManager?.isTouchExplorationEnabled == true
    }
    
    // ==================== Simplified Public Methods ====================
    
    /**
     * Ensure all views have content descriptions.
     */
    fun ensureContentDescriptions(rootView: View) {
        processViewHierarchy(rootView) { view ->
            ensureContentDescription(view)
        }
    }
    
    /**
     * Enlarge touch targets for all clickable views.
     */
    fun enlargeTouchTargets(rootView: View, minSizeDp: Int) {
        processViewHierarchy(rootView) { view ->
            if (view.isClickable || view is Button || view is ImageButton) {
                enlargeTouchTarget(view, minSizeDp)
            }
        }
    }
    
    /**
     * Apply high contrast to all views.
     */
    fun applyHighContrast(rootView: View) {
        processViewHierarchy(rootView) { view ->
            when (view) {
                is TextView -> {
                    view.setTextColor(Color.BLACK)
                }
                is MaterialCardView -> {
                    view.strokeWidth = 4
                    view.strokeColor = Color.BLACK
                }
            }
        }
    }
    
    /**
     * Apply large text to all TextViews.
     */
    fun applyLargeText(rootView: View, scaleFactor: Float) {
        processViewHierarchy(rootView) { view ->
            if (view is TextView) {
                val currentSize = view.textSize / context.resources.displayMetrics.scaledDensity
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize * scaleFactor)
            }
        }
    }
    
    /**
     * Convert dp to pixels.
     */
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
    
    enum class HapticType {
        CLICK,
        DOUBLE_CLICK,
        SUCCESS,
        ERROR,
        WARNING,
        NAVIGATION
    }
}
