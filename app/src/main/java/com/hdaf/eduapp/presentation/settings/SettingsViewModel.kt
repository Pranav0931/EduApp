package com.hdaf.eduapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.security.SecurePreferences
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.repository.AuthRepository
import com.hdaf.eduapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        val savedMode = securePreferences.getString(KEY_ACCESSIBILITY_MODE, AccessibilityModeType.NORMAL.name)
        val accessibilityMode = try {
            AccessibilityModeType.valueOf(savedMode ?: AccessibilityModeType.NORMAL.name)
        } catch (e: Exception) {
            AccessibilityModeType.NORMAL
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                accessibilityMode = accessibilityMode,
                talkbackEnabled = securePreferences.getBoolean(KEY_TALKBACK, false),
                highContrastEnabled = securePreferences.getBoolean(KEY_HIGH_CONTRAST, false),
                largeTextEnabled = securePreferences.getBoolean(KEY_LARGE_TEXT, false),
                hapticFeedbackEnabled = securePreferences.getBoolean(KEY_HAPTIC_FEEDBACK, true),
                subtitlesEnabled = securePreferences.getBoolean(KEY_SUBTITLES, false),
                voiceNavigationEnabled = securePreferences.getBoolean(KEY_VOICE_NAVIGATION, false),
                contentMode = securePreferences.getString(KEY_CONTENT_MODE, "ऑडियो") ?: "ऑडियो",
                language = securePreferences.getString(KEY_LANGUAGE, "हिंदी") ?: "हिंदी",
                downloadQuality = securePreferences.getString(KEY_DOWNLOAD_QUALITY, "मध्यम") ?: "मध्यम",
                autoPlayEnabled = securePreferences.getBoolean(KEY_AUTO_PLAY, true),
                offlineModeEnabled = securePreferences.getBoolean(KEY_OFFLINE_MODE, false),
                notificationsEnabled = securePreferences.getBoolean(KEY_NOTIFICATIONS, true),
                dailyReminderEnabled = securePreferences.getBoolean(KEY_DAILY_REMINDER, true),
                appVersion = "1.0.0"
            )
        }
    }
    
    fun setAccessibilityMode(mode: AccessibilityModeType) {
        securePreferences.putString(KEY_ACCESSIBILITY_MODE, mode.name)
        
        // Auto-configure related settings based on mode
        when (mode) {
            AccessibilityModeType.BLIND -> {
                setTalkbackEnabled(true)
                setVoiceNavigationEnabled(true)
                setHapticFeedbackEnabled(true)
            }
            AccessibilityModeType.DEAF -> {
                setSubtitlesEnabled(true)
                setHapticFeedbackEnabled(true)
            }
            AccessibilityModeType.LOW_VISION -> {
                setHighContrastEnabled(true)
                setLargeTextEnabled(true)
            }
            AccessibilityModeType.SLOW_LEARNER -> {
                // Keep defaults but slower pace handled elsewhere
            }
            AccessibilityModeType.NORMAL -> {
                // Keep user preferences
            }
        }
        
        _uiState.update { it.copy(accessibilityMode = mode) }
    }

    fun setTalkbackEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_TALKBACK, enabled)
        _uiState.update { it.copy(talkbackEnabled = enabled) }
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_HIGH_CONTRAST, enabled)
        _uiState.update { it.copy(highContrastEnabled = enabled) }
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_LARGE_TEXT, enabled)
        _uiState.update { it.copy(largeTextEnabled = enabled) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_HAPTIC_FEEDBACK, enabled)
        _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
    }
    
    fun setSubtitlesEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_SUBTITLES, enabled)
        _uiState.update { it.copy(subtitlesEnabled = enabled) }
    }
    
    fun setVoiceNavigationEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_VOICE_NAVIGATION, enabled)
        _uiState.update { it.copy(voiceNavigationEnabled = enabled) }
    }

    fun setContentMode(mode: String) {
        securePreferences.putString(KEY_CONTENT_MODE, mode)
        _uiState.update { it.copy(contentMode = mode) }
    }

    fun setLanguage(language: String) {
        securePreferences.putString(KEY_LANGUAGE, language)
        _uiState.update { it.copy(language = language) }
    }

    fun setDownloadQuality(quality: String) {
        securePreferences.putString(KEY_DOWNLOAD_QUALITY, quality)
        _uiState.update { it.copy(downloadQuality = quality) }
    }

    fun setAutoPlayEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_AUTO_PLAY, enabled)
        _uiState.update { it.copy(autoPlayEnabled = enabled) }
    }

    fun setOfflineModeEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_OFFLINE_MODE, enabled)
        _uiState.update { it.copy(offlineModeEnabled = enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_NOTIFICATIONS, enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        
        if (!enabled) {
            setDailyReminderEnabled(false)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        securePreferences.putBoolean(KEY_DAILY_REMINDER, enabled)
        _uiState.update { it.copy(dailyReminderEnabled = enabled) }
    }

    fun changeClass(classLevel: Int) {
        viewModelScope.launch {
            userRepository.updateUserClass(classLevel)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: Implement cache clearing
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            securePreferences.clearAll()
        }
    }

    companion object {
        private const val KEY_ACCESSIBILITY_MODE = "accessibility_mode"
        private const val KEY_TALKBACK = "talkback_enabled"
        private const val KEY_HIGH_CONTRAST = "high_contrast_enabled"
        private const val KEY_LARGE_TEXT = "large_text_enabled"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback_enabled"
        private const val KEY_SUBTITLES = "subtitles_enabled"
        private const val KEY_VOICE_NAVIGATION = "voice_navigation_enabled"
        private const val KEY_CONTENT_MODE = "content_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DOWNLOAD_QUALITY = "download_quality"
        private const val KEY_AUTO_PLAY = "auto_play_enabled"
        private const val KEY_OFFLINE_MODE = "offline_mode_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_DAILY_REMINDER = "daily_reminder_enabled"
    }
}

data class SettingsUiState(
    val accessibilityMode: AccessibilityModeType = AccessibilityModeType.NORMAL,
    val talkbackEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val subtitlesEnabled: Boolean = false,
    val voiceNavigationEnabled: Boolean = false,
    val contentMode: String = "ऑडियो",
    val language: String = "हिंदी",
    val downloadQuality: String = "मध्यम",
    val autoPlayEnabled: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val appVersion: String = ""
)
