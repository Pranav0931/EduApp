package com.hdaf.eduapp.presentation.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.ui.accessibility.AccessibilityViewHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Base fragment that automatically applies accessibility settings to all views.
 * Extend this for consistent accessibility experience across the app.
 */
abstract class AccessibleBaseFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    @Inject
    lateinit var accessibilityViewHelper: AccessibilityViewHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyAccessibilitySettings(view)
    }
    
    override fun onResume() {
        super.onResume()
        // Re-apply settings in case they changed
        view?.let { applyAccessibilitySettings(it) }
    }
    
    private fun applyAccessibilitySettings(rootView: View) {
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        val mode = AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }
        
        when (mode) {
            AccessibilityModeType.NORMAL -> {
                // No special modifications needed
            }
            AccessibilityModeType.BLIND -> {
                applyBlindModeSettings(rootView)
            }
            AccessibilityModeType.DEAF -> {
                applyDeafModeSettings(rootView)
            }
            AccessibilityModeType.LOW_VISION -> {
                applyLowVisionSettings(rootView)
            }
            AccessibilityModeType.SLOW_LEARNER -> {
                applySlowLearnerSettings(rootView)
            }
        }
    }
    
    private fun applyBlindModeSettings(rootView: View) {
        // Ensure all views have proper content descriptions
        accessibilityViewHelper.ensureContentDescriptions(rootView)
        
        // Make touch targets larger
        accessibilityViewHelper.enlargeTouchTargets(rootView, 56)
    }
    
    private fun applyDeafModeSettings(rootView: View) {
        // Apply high contrast for better visibility
        accessibilityViewHelper.applyHighContrast(rootView)
    }
    
    private fun applyLowVisionSettings(rootView: View) {
        // Apply large text and high contrast
        accessibilityViewHelper.applyLargeText(rootView, 1.5f)
        accessibilityViewHelper.applyHighContrast(rootView)
        accessibilityViewHelper.enlargeTouchTargets(rootView, 64)
    }
    
    private fun applySlowLearnerSettings(rootView: View) {
        // Apply slightly larger text for readability
        accessibilityViewHelper.applyLargeText(rootView, 1.2f)
    }
    
    /**
     * Get the current accessibility mode
     */
    protected fun getCurrentAccessibilityMode(): AccessibilityModeType {
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        return AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }
    }
    
    /**
     * Check if blind mode is active
     */
    protected fun isBlindMode(): Boolean = getCurrentAccessibilityMode() == AccessibilityModeType.BLIND
    
    /**
     * Check if deaf mode is active
     */
    protected fun isDeafMode(): Boolean = getCurrentAccessibilityMode() == AccessibilityModeType.DEAF
    
    /**
     * Check if low vision mode is active
     */
    protected fun isLowVisionMode(): Boolean = getCurrentAccessibilityMode() == AccessibilityModeType.LOW_VISION
    
    /**
     * Check if slow learner mode is active
     */
    protected fun isSlowLearnerMode(): Boolean = getCurrentAccessibilityMode() == AccessibilityModeType.SLOW_LEARNER
}
