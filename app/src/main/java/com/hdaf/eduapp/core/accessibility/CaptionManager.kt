package com.hdaf.eduapp.core.accessibility

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real-time Caption Manager for Deaf Students.
 * 
 * Features:
 * - Live captions for audio/video content
 * - Auto-captions for speech-to-text
 * - Caption styling customization (size, color, position)
 * - Caption history and search
 * - Multi-language support (Hindi/English)
 * - Speaker identification in group scenarios
 */
@Singleton
class CaptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _captionsEnabled = MutableStateFlow(false)
    val captionsEnabled: StateFlow<Boolean> = _captionsEnabled.asStateFlow()
    
    private val _currentCaption = MutableStateFlow<Caption?>(null)
    val currentCaption: StateFlow<Caption?> = _currentCaption.asStateFlow()
    
    private val _captionHistory = MutableStateFlow<List<Caption>>(emptyList())
    val captionHistory: StateFlow<List<Caption>> = _captionHistory.asStateFlow()
    
    private val _captionStyle = MutableStateFlow(CaptionStyle.default())
    val captionStyle: StateFlow<CaptionStyle> = _captionStyle.asStateFlow()
    
    private val _preferredLanguage = MutableStateFlow(CaptionLanguage.HINDI)
    val preferredLanguage: StateFlow<CaptionLanguage> = _preferredLanguage.asStateFlow()
    
    // ========== Configuration ==========
    
    fun setEnabled(enabled: Boolean) {
        _captionsEnabled.value = enabled
    }
    
    fun setStyle(style: CaptionStyle) {
        _captionStyle.value = style
    }
    
    fun setPreferredLanguage(language: CaptionLanguage) {
        _preferredLanguage.value = language
    }
    
    // ========== Caption Display ==========
    
    /**
     * Display a new caption.
     */
    fun showCaption(
        text: String,
        speaker: String? = null,
        soundEffect: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val caption = Caption(
            id = generateCaptionId(),
            text = text,
            speaker = speaker,
            soundEffect = soundEffect,
            timestamp = timestamp,
            language = _preferredLanguage.value
        )
        
        _currentCaption.value = caption
        addToHistory(caption)
    }
    
    /**
     * Show timed captions for video content.
     */
    fun showTimedCaption(
        text: String,
        startTime: Long,
        endTime: Long,
        speaker: String? = null
    ) {
        val caption = Caption(
            id = generateCaptionId(),
            text = text,
            speaker = speaker,
            soundEffect = null,
            timestamp = startTime,
            endTime = endTime,
            language = _preferredLanguage.value
        )
        
        _currentCaption.value = caption
        addToHistory(caption)
    }
    
    /**
     * Show sound effect caption (for non-speech audio).
     */
    fun showSoundEffect(effect: SoundEffectType) {
        val description = getSoundEffectDescription(effect)
        showCaption(
            text = description.text,
            soundEffect = effect.name
        )
    }
    
    /**
     * Clear current caption.
     */
    fun clearCaption() {
        _currentCaption.value = null
    }
    
    // ========== Caption History ==========
    
    private fun addToHistory(caption: Caption) {
        val current = _captionHistory.value.toMutableList()
        current.add(caption)
        
        // Keep only last 100 captions
        if (current.size > 100) {
            current.removeAt(0)
        }
        
        _captionHistory.value = current
    }
    
    /**
     * Search caption history.
     */
    fun searchHistory(query: String): List<Caption> {
        return _captionHistory.value.filter {
            it.text.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Get recent captions.
     */
    fun getRecentCaptions(count: Int = 10): List<Caption> {
        return _captionHistory.value.takeLast(count)
    }
    
    /**
     * Clear caption history.
     */
    fun clearHistory() {
        _captionHistory.value = emptyList()
    }
    
    // ========== Video Caption Support ==========
    
    /**
     * Load captions for a video.
     */
    fun loadVideoCaptions(videoId: String): List<TimedCaption> {
        // In production, would load from database or CDN
        return emptyList()
    }
    
    /**
     * Get caption for current video position.
     */
    fun getCaptionForPosition(
        captions: List<TimedCaption>,
        positionMs: Long
    ): TimedCaption? {
        return captions.find { caption ->
            positionMs >= caption.startTimeMs && positionMs <= caption.endTimeMs
        }
    }
    
    // ========== Speech-to-Text Integration ==========
    
    /**
     * Process speech-to-text result as caption.
     */
    fun processSpeechResult(
        text: String,
        confidence: Float,
        isFinal: Boolean
    ) {
        if (text.isBlank()) return
        
        val caption = Caption(
            id = generateCaptionId(),
            text = text,
            speaker = null,
            soundEffect = null,
            timestamp = System.currentTimeMillis(),
            confidence = confidence,
            isFinal = isFinal,
            language = _preferredLanguage.value
        )
        
        _currentCaption.value = caption
        
        if (isFinal) {
            addToHistory(caption)
        }
    }
    
    // ========== Sound Effect Descriptions ==========
    
    private fun getSoundEffectDescription(effect: SoundEffectType): SoundEffectDescription {
        return when (effect) {
            SoundEffectType.BELL -> SoundEffectDescription(
                "🔔 [घंटी की आवाज़ / Bell sound]",
                "[Bell ringing]"
            )
            SoundEffectType.APPLAUSE -> SoundEffectDescription(
                "👏 [तालियाँ / Applause]",
                "[Applause]"
            )
            SoundEffectType.MUSIC -> SoundEffectDescription(
                "🎵 [संगीत बज रहा है / Music playing]",
                "[Music playing]"
            )
            SoundEffectType.CHEER -> SoundEffectDescription(
                "🎉 [खुशी की आवाज़ / Cheering]",
                "[Cheering]"
            )
            SoundEffectType.BUZZER -> SoundEffectDescription(
                "⏰ [बजर की आवाज़ / Buzzer sound]",
                "[Buzzer]"
            )
            SoundEffectType.CORRECT_ANSWER -> SoundEffectDescription(
                "✅ [सही उत्तर की आवाज़ / Correct answer sound]",
                "[Correct answer chime]"
            )
            SoundEffectType.WRONG_ANSWER -> SoundEffectDescription(
                "❌ [गलत उत्तर की आवाज़ / Wrong answer sound]",
                "[Wrong answer sound]"
            )
            SoundEffectType.PAGE_TURN -> SoundEffectDescription(
                "📖 [पेज पलटने की आवाज़ / Page turning]",
                "[Page turning]"
            )
            SoundEffectType.NOTIFICATION -> SoundEffectDescription(
                "🔔 [नोटिफिकेशन / Notification]",
                "[Notification sound]"
            )
            SoundEffectType.LOADING -> SoundEffectDescription(
                "⏳ [लोड हो रहा है / Loading]",
                "[Loading...]"
            )
            SoundEffectType.SUCCESS -> SoundEffectDescription(
                "✨ [सफलता की आवाज़ / Success sound]",
                "[Success!]"
            )
            SoundEffectType.ERROR -> SoundEffectDescription(
                "⚠️ [त्रुटि की आवाज़ / Error sound]",
                "[Error sound]"
            )
            SoundEffectType.CHAPTER_COMPLETE -> SoundEffectDescription(
                "🏆 [अध्याय पूरा / Chapter complete]",
                "[Chapter complete fanfare]"
            )
            SoundEffectType.BADGE_EARNED -> SoundEffectDescription(
                "🎖️ [बैज मिला / Badge earned]",
                "[Badge earned celebration]"
            )
            SoundEffectType.TIMER_TICK -> SoundEffectDescription(
                "⏱️ [टाइमर टिक / Timer ticking]",
                "[Timer ticking]"
            )
            SoundEffectType.TIMER_WARNING -> SoundEffectDescription(
                "⚡ [समय समाप्त हो रहा है / Time running out]",
                "[Time running out warning]"
            )
        }
    }
    
    // ========== Utility ==========
    
    private var captionIdCounter = 0L
    
    private fun generateCaptionId(): String {
        return "caption_${++captionIdCounter}"
    }
}

/**
 * Caption data.
 */
data class Caption(
    val id: String,
    val text: String,
    val speaker: String? = null,
    val soundEffect: String? = null,
    val timestamp: Long,
    val endTime: Long? = null,
    val language: CaptionLanguage = CaptionLanguage.HINDI,
    val confidence: Float = 1.0f,
    val isFinal: Boolean = true
)

/**
 * Timed caption for video content.
 */
data class TimedCaption(
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val speaker: String? = null,
    val language: CaptionLanguage = CaptionLanguage.HINDI
)

/**
 * Caption style configuration.
 */
data class CaptionStyle(
    val fontSize: CaptionFontSize,
    val fontColor: Int,
    val backgroundColor: Int,
    val backgroundOpacity: Float,
    val position: CaptionPosition,
    val fontFamily: String
) {
    companion object {
        fun default() = CaptionStyle(
            fontSize = CaptionFontSize.MEDIUM,
            fontColor = 0xFFFFFFFF.toInt(),  // White
            backgroundColor = 0xFF000000.toInt(),  // Black
            backgroundOpacity = 0.75f,
            position = CaptionPosition.BOTTOM,
            fontFamily = "sans-serif"
        )
        
        fun highContrast() = CaptionStyle(
            fontSize = CaptionFontSize.LARGE,
            fontColor = 0xFFFFFF00.toInt(),  // Yellow
            backgroundColor = 0xFF000000.toInt(),  // Black
            backgroundOpacity = 1.0f,
            position = CaptionPosition.BOTTOM,
            fontFamily = "sans-serif"
        )
    }
}

/**
 * Caption font sizes.
 */
enum class CaptionFontSize(val sp: Int) {
    SMALL(14),
    MEDIUM(18),
    LARGE(24),
    EXTRA_LARGE(32)
}

/**
 * Caption position on screen.
 */
enum class CaptionPosition {
    TOP, BOTTOM, CENTER
}

/**
 * Caption languages.
 */
enum class CaptionLanguage(val code: String, val displayName: String) {
    HINDI("hi", "हिंदी"),
    ENGLISH("en", "English"),
    BILINGUAL("hi-en", "हिंदी + English")
}

/**
 * Sound effect types.
 */
enum class SoundEffectType {
    BELL,
    APPLAUSE,
    MUSIC,
    CHEER,
    BUZZER,
    CORRECT_ANSWER,
    WRONG_ANSWER,
    PAGE_TURN,
    NOTIFICATION,
    LOADING,
    SUCCESS,
    ERROR,
    CHAPTER_COMPLETE,
    BADGE_EARNED,
    TIMER_TICK,
    TIMER_WARNING
}

/**
 * Sound effect description.
 */
data class SoundEffectDescription(
    val text: String,
    val englishText: String
)
