package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Accessibility Settings Manager.
 * 
 * Central configuration for all accessibility features:
 * - Deaf mode settings (captions, sign language, visual alerts)
 * - Blind mode settings (TTS, haptics, gestures, voice control)
 * - Low vision settings (font size, contrast, colors)
 * - Learning preferences
 * 
 * Provides presets for common accessibility needs.
 */
@Singleton
class AccessibilitySettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // ========== Active Mode ==========
    
    private val _accessibilityMode = MutableStateFlow(loadAccessibilityMode())
    val accessibilityMode: StateFlow<AccessibilityMode> = _accessibilityMode.asStateFlow()
    
    // ========== Deaf Mode Settings ==========
    
    private val _deafSettings = MutableStateFlow(loadDeafSettings())
    val deafSettings: StateFlow<DeafAccessibilitySettings> = _deafSettings.asStateFlow()
    
    // ========== Blind Mode Settings ==========
    
    private val _blindSettings = MutableStateFlow(loadBlindSettings())
    val blindSettings: StateFlow<BlindAccessibilitySettings> = _blindSettings.asStateFlow()
    
    // ========== Low Vision Settings ==========
    
    private val _lowVisionSettings = MutableStateFlow(loadLowVisionSettings())
    val lowVisionSettings: StateFlow<LowVisionSettings> = _lowVisionSettings.asStateFlow()
    
    // ========== Mode Selection ==========
    
    fun setAccessibilityMode(mode: AccessibilityMode) {
        _accessibilityMode.value = mode
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        
        // Apply preset settings based on mode
        when (mode) {
            AccessibilityMode.DEAF -> applyDeafPreset()
            AccessibilityMode.BLIND -> applyBlindPreset()
            AccessibilityMode.LOW_VISION -> applyLowVisionPreset()
            AccessibilityMode.STANDARD -> applyStandardPreset()
            AccessibilityMode.CUSTOM -> { /* User customizes individually */ }
        }
    }
    
    // ========== Deaf Mode Configuration ==========
    
    fun updateDeafSettings(settings: DeafAccessibilitySettings) {
        _deafSettings.value = settings
        saveDeafSettings(settings)
    }
    
    fun setSignLanguageEnabled(enabled: Boolean) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(signLanguageEnabled = enabled))
    }
    
    fun setSignLanguageType(type: SignLanguageType) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(signLanguageType = type))
    }
    
    fun setCaptionsEnabled(enabled: Boolean) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(captionsEnabled = enabled))
    }
    
    fun setCaptionStyle(style: CaptionStyle) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(captionStyle = style))
    }
    
    fun setVisualAlertsEnabled(enabled: Boolean) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(visualAlertsEnabled = enabled))
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        val current = _deafSettings.value
        updateDeafSettings(current.copy(vibrationEnabled = enabled))
    }
    
    // ========== Blind Mode Configuration ==========
    
    fun updateBlindSettings(settings: BlindAccessibilitySettings) {
        _blindSettings.value = settings
        saveBlindSettings(settings)
    }
    
    fun setTTSEnabled(enabled: Boolean) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(ttsEnabled = enabled))
    }
    
    fun setTTSSpeed(speed: Float) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(ttsSpeed = speed.coerceIn(0.5f, 2.0f)))
    }
    
    fun setTTSPitch(pitch: Float) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(ttsPitch = pitch.coerceIn(0.5f, 2.0f)))
    }
    
    fun setHapticFeedbackEnabled(enabled: Boolean) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(hapticFeedbackEnabled = enabled))
    }
    
    fun setVoiceControlEnabled(enabled: Boolean) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(voiceControlEnabled = enabled))
    }
    
    fun setGestureNavigationEnabled(enabled: Boolean) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(gestureNavigationEnabled = enabled))
    }
    
    fun setAudioDescriptionsEnabled(enabled: Boolean) {
        val current = _blindSettings.value
        updateBlindSettings(current.copy(audioDescriptionsEnabled = enabled))
    }
    
    // ========== Low Vision Configuration ==========
    
    fun updateLowVisionSettings(settings: LowVisionSettings) {
        _lowVisionSettings.value = settings
        saveLowVisionSettings(settings)
    }
    
    fun setFontScale(scale: Float) {
        val current = _lowVisionSettings.value
        updateLowVisionSettings(current.copy(fontScale = scale.coerceIn(1.0f, 3.0f)))
    }
    
    fun setHighContrastEnabled(enabled: Boolean) {
        val current = _lowVisionSettings.value
        updateLowVisionSettings(current.copy(highContrastEnabled = enabled))
    }
    
    fun setColorScheme(scheme: ColorScheme) {
        val current = _lowVisionSettings.value
        updateLowVisionSettings(current.copy(colorScheme = scheme))
    }
    
    fun setBoldTextEnabled(enabled: Boolean) {
        val current = _lowVisionSettings.value
        updateLowVisionSettings(current.copy(boldTextEnabled = enabled))
    }
    
    fun setReduceMotionEnabled(enabled: Boolean) {
        val current = _lowVisionSettings.value
        updateLowVisionSettings(current.copy(reduceMotionEnabled = enabled))
    }
    
    // ========== Presets ==========
    
    private fun applyDeafPreset() {
        updateDeafSettings(
            DeafAccessibilitySettings(
                signLanguageEnabled = true,
                signLanguageType = SignLanguageType.ISL,
                captionsEnabled = true,
                captionStyle = CaptionStyle.default(),
                captionLanguage = CaptionLanguage.BILINGUAL,
                visualAlertsEnabled = true,
                vibrationEnabled = true,
                flashAlertsEnabled = true,
                soundVisualizerEnabled = true
            )
        )
        
        // Also enable some visual aids
        updateLowVisionSettings(
            _lowVisionSettings.value.copy(
                fontScale = 1.2f,
                highContrastEnabled = false
            )
        )
    }
    
    private fun applyBlindPreset() {
        updateBlindSettings(
            BlindAccessibilitySettings(
                ttsEnabled = true,
                ttsSpeed = 1.0f,
                ttsPitch = 1.0f,
                ttsLanguage = TTSLanguage.HINDI,
                hapticFeedbackEnabled = true,
                voiceControlEnabled = true,
                gestureNavigationEnabled = true,
                audioDescriptionsEnabled = true,
                screenReaderOptimized = true,
                announceNotifications = true,
                announceProgress = true,
                simplifiedLayout = true
            )
        )
    }
    
    private fun applyLowVisionPreset() {
        updateLowVisionSettings(
            LowVisionSettings(
                fontScale = 1.5f,
                highContrastEnabled = true,
                colorScheme = ColorScheme.HIGH_CONTRAST_DARK,
                boldTextEnabled = true,
                reduceMotionEnabled = true,
                largeButtonsEnabled = true,
                increasedSpacingEnabled = true,
                magnificationEnabled = true
            )
        )
    }
    
    private fun applyStandardPreset() {
        updateDeafSettings(DeafAccessibilitySettings.default())
        updateBlindSettings(BlindAccessibilitySettings.default())
        updateLowVisionSettings(LowVisionSettings.default())
    }
    
    // ========== Persistence ==========
    
    private fun loadAccessibilityMode(): AccessibilityMode {
        val modeName = prefs.getString(KEY_MODE, AccessibilityMode.STANDARD.name)
        return try {
            AccessibilityMode.valueOf(modeName ?: AccessibilityMode.STANDARD.name)
        } catch (e: Exception) {
            AccessibilityMode.STANDARD
        }
    }
    
    private fun loadDeafSettings(): DeafAccessibilitySettings {
        return DeafAccessibilitySettings(
            signLanguageEnabled = prefs.getBoolean(KEY_SIGN_LANGUAGE, false),
            signLanguageType = loadEnum(KEY_SIGN_TYPE, SignLanguageType.ISL),
            captionsEnabled = prefs.getBoolean(KEY_CAPTIONS, false),
            captionStyle = CaptionStyle.default(),
            captionLanguage = loadEnum(KEY_CAPTION_LANG, CaptionLanguage.HINDI),
            visualAlertsEnabled = prefs.getBoolean(KEY_VISUAL_ALERTS, false),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            flashAlertsEnabled = prefs.getBoolean(KEY_FLASH_ALERTS, false),
            soundVisualizerEnabled = prefs.getBoolean(KEY_SOUND_VISUALIZER, false)
        )
    }
    
    private fun saveDeafSettings(settings: DeafAccessibilitySettings) {
        prefs.edit().apply {
            putBoolean(KEY_SIGN_LANGUAGE, settings.signLanguageEnabled)
            putString(KEY_SIGN_TYPE, settings.signLanguageType.name)
            putBoolean(KEY_CAPTIONS, settings.captionsEnabled)
            putString(KEY_CAPTION_LANG, settings.captionLanguage.name)
            putBoolean(KEY_VISUAL_ALERTS, settings.visualAlertsEnabled)
            putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
            putBoolean(KEY_FLASH_ALERTS, settings.flashAlertsEnabled)
            putBoolean(KEY_SOUND_VISUALIZER, settings.soundVisualizerEnabled)
            apply()
        }
    }
    
    private fun loadBlindSettings(): BlindAccessibilitySettings {
        return BlindAccessibilitySettings(
            ttsEnabled = prefs.getBoolean(KEY_TTS, true),
            ttsSpeed = prefs.getFloat(KEY_TTS_SPEED, 1.0f),
            ttsPitch = prefs.getFloat(KEY_TTS_PITCH, 1.0f),
            ttsLanguage = loadEnum(KEY_TTS_LANG, TTSLanguage.HINDI),
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            voiceControlEnabled = prefs.getBoolean(KEY_VOICE_CONTROL, false),
            gestureNavigationEnabled = prefs.getBoolean(KEY_GESTURE_NAV, true),
            audioDescriptionsEnabled = prefs.getBoolean(KEY_AUDIO_DESC, true),
            screenReaderOptimized = prefs.getBoolean(KEY_SCREEN_READER, true),
            announceNotifications = prefs.getBoolean(KEY_ANNOUNCE_NOTIF, true),
            announceProgress = prefs.getBoolean(KEY_ANNOUNCE_PROGRESS, true),
            simplifiedLayout = prefs.getBoolean(KEY_SIMPLIFIED, false)
        )
    }
    
    private fun saveBlindSettings(settings: BlindAccessibilitySettings) {
        prefs.edit().apply {
            putBoolean(KEY_TTS, settings.ttsEnabled)
            putFloat(KEY_TTS_SPEED, settings.ttsSpeed)
            putFloat(KEY_TTS_PITCH, settings.ttsPitch)
            putString(KEY_TTS_LANG, settings.ttsLanguage.name)
            putBoolean(KEY_HAPTIC, settings.hapticFeedbackEnabled)
            putBoolean(KEY_VOICE_CONTROL, settings.voiceControlEnabled)
            putBoolean(KEY_GESTURE_NAV, settings.gestureNavigationEnabled)
            putBoolean(KEY_AUDIO_DESC, settings.audioDescriptionsEnabled)
            putBoolean(KEY_SCREEN_READER, settings.screenReaderOptimized)
            putBoolean(KEY_ANNOUNCE_NOTIF, settings.announceNotifications)
            putBoolean(KEY_ANNOUNCE_PROGRESS, settings.announceProgress)
            putBoolean(KEY_SIMPLIFIED, settings.simplifiedLayout)
            apply()
        }
    }
    
    private fun loadLowVisionSettings(): LowVisionSettings {
        return LowVisionSettings(
            fontScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f),
            highContrastEnabled = prefs.getBoolean(KEY_HIGH_CONTRAST, false),
            colorScheme = loadEnum(KEY_COLOR_SCHEME, ColorScheme.DEFAULT),
            boldTextEnabled = prefs.getBoolean(KEY_BOLD_TEXT, false),
            reduceMotionEnabled = prefs.getBoolean(KEY_REDUCE_MOTION, false),
            largeButtonsEnabled = prefs.getBoolean(KEY_LARGE_BUTTONS, false),
            increasedSpacingEnabled = prefs.getBoolean(KEY_SPACING, false),
            magnificationEnabled = prefs.getBoolean(KEY_MAGNIFICATION, false)
        )
    }
    
    private fun saveLowVisionSettings(settings: LowVisionSettings) {
        prefs.edit().apply {
            putFloat(KEY_FONT_SCALE, settings.fontScale)
            putBoolean(KEY_HIGH_CONTRAST, settings.highContrastEnabled)
            putString(KEY_COLOR_SCHEME, settings.colorScheme.name)
            putBoolean(KEY_BOLD_TEXT, settings.boldTextEnabled)
            putBoolean(KEY_REDUCE_MOTION, settings.reduceMotionEnabled)
            putBoolean(KEY_LARGE_BUTTONS, settings.largeButtonsEnabled)
            putBoolean(KEY_SPACING, settings.increasedSpacingEnabled)
            putBoolean(KEY_MAGNIFICATION, settings.magnificationEnabled)
            apply()
        }
    }
    
    private inline fun <reified T : Enum<T>> loadEnum(key: String, default: T): T {
        val name = prefs.getString(key, default.name)
        return try {
            enumValueOf<T>(name ?: default.name)
        } catch (e: Exception) {
            default
        }
    }
    
    companion object {
        private const val PREFS_NAME = "accessibility_settings"
        private const val KEY_MODE = "accessibility_mode"
        
        // Deaf settings keys
        private const val KEY_SIGN_LANGUAGE = "sign_language_enabled"
        private const val KEY_SIGN_TYPE = "sign_language_type"
        private const val KEY_CAPTIONS = "captions_enabled"
        private const val KEY_CAPTION_LANG = "caption_language"
        private const val KEY_VISUAL_ALERTS = "visual_alerts_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_FLASH_ALERTS = "flash_alerts_enabled"
        private const val KEY_SOUND_VISUALIZER = "sound_visualizer_enabled"
        
        // Blind settings keys
        private const val KEY_TTS = "tts_enabled"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_TTS_LANG = "tts_language"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_VOICE_CONTROL = "voice_control_enabled"
        private const val KEY_GESTURE_NAV = "gesture_nav_enabled"
        private const val KEY_AUDIO_DESC = "audio_descriptions_enabled"
        private const val KEY_SCREEN_READER = "screen_reader_optimized"
        private const val KEY_ANNOUNCE_NOTIF = "announce_notifications"
        private const val KEY_ANNOUNCE_PROGRESS = "announce_progress"
        private const val KEY_SIMPLIFIED = "simplified_layout"
        
        // Low vision settings keys
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast_enabled"
        private const val KEY_COLOR_SCHEME = "color_scheme"
        private const val KEY_BOLD_TEXT = "bold_text_enabled"
        private const val KEY_REDUCE_MOTION = "reduce_motion_enabled"
        private const val KEY_LARGE_BUTTONS = "large_buttons_enabled"
        private const val KEY_SPACING = "increased_spacing_enabled"
        private const val KEY_MAGNIFICATION = "magnification_enabled"
    }
}

/**
 * Accessibility modes.
 */
enum class AccessibilityMode(val displayNameHindi: String, val displayNameEnglish: String) {
    DEAF("बधिर मोड", "Deaf Mode"),
    BLIND("दृष्टिबाधित मोड", "Blind Mode"),
    LOW_VISION("कम दृष्टि मोड", "Low Vision Mode"),
    STANDARD("सामान्य मोड", "Standard Mode"),
    CUSTOM("कस्टम मोड", "Custom Mode")
}

/**
 * Deaf accessibility settings.
 */
data class DeafAccessibilitySettings(
    val signLanguageEnabled: Boolean = false,
    val signLanguageType: SignLanguageType = SignLanguageType.ISL,
    val captionsEnabled: Boolean = false,
    val captionStyle: CaptionStyle = CaptionStyle.default(),
    val captionLanguage: CaptionLanguage = CaptionLanguage.HINDI,
    val visualAlertsEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val flashAlertsEnabled: Boolean = false,
    val soundVisualizerEnabled: Boolean = false
) {
    companion object {
        fun default() = DeafAccessibilitySettings()
    }
}

/**
 * Blind accessibility settings.
 */
data class BlindAccessibilitySettings(
    val ttsEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val ttsLanguage: TTSLanguage = TTSLanguage.HINDI,
    val hapticFeedbackEnabled: Boolean = true,
    val voiceControlEnabled: Boolean = false,
    val gestureNavigationEnabled: Boolean = true,
    val audioDescriptionsEnabled: Boolean = true,
    val screenReaderOptimized: Boolean = true,
    val announceNotifications: Boolean = true,
    val announceProgress: Boolean = true,
    val simplifiedLayout: Boolean = false
) {
    companion object {
        fun default() = BlindAccessibilitySettings()
    }
}

/**
 * Low vision settings.
 */
data class LowVisionSettings(
    val fontScale: Float = 1.0f,
    val highContrastEnabled: Boolean = false,
    val colorScheme: ColorScheme = ColorScheme.DEFAULT,
    val boldTextEnabled: Boolean = false,
    val reduceMotionEnabled: Boolean = false,
    val largeButtonsEnabled: Boolean = false,
    val increasedSpacingEnabled: Boolean = false,
    val magnificationEnabled: Boolean = false
) {
    companion object {
        fun default() = LowVisionSettings()
    }
}

/**
 * TTS language options.
 */
enum class TTSLanguage(val code: String, val displayName: String) {
    HINDI("hi-IN", "हिंदी"),
    ENGLISH("en-IN", "English (India)"),
    HINGLISH("hi-EN", "Hinglish")
}

/**
 * Color schemes for low vision.
 */
enum class ColorScheme(val displayName: String) {
    DEFAULT("Default / डिफ़ॉल्ट"),
    HIGH_CONTRAST_DARK("High Contrast Dark / गहरा कंट्रास्ट"),
    HIGH_CONTRAST_LIGHT("High Contrast Light / हल्का कंट्रास्ट"),
    YELLOW_ON_BLACK("Yellow on Black / काले पर पीला"),
    WHITE_ON_BLACK("White on Black / काले पर सफेद"),
    BLUE_ON_WHITE("Blue on White / सफेद पर नीला")
}
