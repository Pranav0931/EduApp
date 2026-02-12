package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Gesture Navigation System for Blind Users.
 * 
 * Provides custom gestures optimized for blind navigation:
 * - Simple swipe patterns (left, right, up, down)
 * - Multi-finger gestures for quick actions
 * - Edge swipes for special commands
 * - Custom gesture patterns for frequently used actions
 * 
 * Works alongside TalkBack but provides app-specific shortcuts.
 */
@Singleton
class BlindGestureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _gestureEvents = MutableSharedFlow<GestureEvent>(replay = 0)
    val gestureEvents: SharedFlow<GestureEvent> = _gestureEvents.asSharedFlow()
    
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _gestureHints = MutableStateFlow<GestureHint?>(null)
    val gestureHints: StateFlow<GestureHint?> = _gestureHints.asStateFlow()
    
    // Screen dimensions for edge detection
    private var screenWidth = 0
    private var screenHeight = 0
    
    // Gesture thresholds
    private val swipeThreshold = 100f
    private val swipeVelocityThreshold = 100f
    private val edgeThreshold = 50f
    private val longPressThreshold = 500L
    
    // Custom gesture patterns
    private val customGestures = mutableMapOf<String, GestureAction>()
    
    init {
        registerDefaultGestures()
    }
    
    // ========== Configuration ==========
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
    
    fun setScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
    
    // ========== Gesture Detection ==========
    
    /**
     * Create a gesture detector for a view.
     */
    fun createGestureDetector(view: View): GestureDetector {
        return GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            
            override fun onDown(e: MotionEvent): Boolean = true
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (!_isEnabled.value || e1 == null) return false
                
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                
                // Determine primary direction
                return when {
                    abs(diffX) > abs(diffY) && abs(diffX) > swipeThreshold -> {
                        handleHorizontalSwipe(e1, diffX, velocityX)
                        true
                    }
                    abs(diffY) > abs(diffX) && abs(diffY) > swipeThreshold -> {
                        handleVerticalSwipe(e1, diffY, velocityY)
                        true
                    }
                    else -> false
                }
            }
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (!_isEnabled.value) return false
                emitGesture(GestureEvent.DoubleTap(e.x, e.y))
                return true
            }
            
            override fun onLongPress(e: MotionEvent) {
                if (!_isEnabled.value) return
                emitGesture(GestureEvent.LongPress(e.x, e.y))
            }
        })
    }
    
    /**
     * Handle multi-touch gestures.
     */
    fun handleMultiTouch(event: MotionEvent) {
        if (!_isEnabled.value) return
        
        when (event.pointerCount) {
            2 -> handleTwoFingerGesture(event)
            3 -> handleThreeFingerGesture(event)
        }
    }
    
    private fun handleHorizontalSwipe(startEvent: MotionEvent, diffX: Float, velocityX: Float) {
        val isEdge = startEvent.x < edgeThreshold || startEvent.x > screenWidth - edgeThreshold
        
        val gesture = if (diffX > 0) {
            if (isEdge && startEvent.x < edgeThreshold) {
                GestureEvent.EdgeSwipeRight
            } else {
                GestureEvent.SwipeRight
            }
        } else {
            if (isEdge && startEvent.x > screenWidth - edgeThreshold) {
                GestureEvent.EdgeSwipeLeft
            } else {
                GestureEvent.SwipeLeft
            }
        }
        
        emitGesture(gesture)
    }
    
    private fun handleVerticalSwipe(startEvent: MotionEvent, diffY: Float, velocityY: Float) {
        val isEdge = startEvent.y < edgeThreshold || startEvent.y > screenHeight - edgeThreshold
        
        val gesture = if (diffY > 0) {
            if (isEdge && startEvent.y < edgeThreshold) {
                GestureEvent.EdgeSwipeDown
            } else {
                GestureEvent.SwipeDown
            }
        } else {
            if (isEdge && startEvent.y > screenHeight - edgeThreshold) {
                GestureEvent.EdgeSwipeUp
            } else {
                GestureEvent.SwipeUp
            }
        }
        
        emitGesture(gesture)
    }
    
    private fun handleTwoFingerGesture(event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_UP -> {
                emitGesture(GestureEvent.TwoFingerTap)
            }
        }
    }
    
    private fun handleThreeFingerGesture(event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_UP -> {
                emitGesture(GestureEvent.ThreeFingerTap)
            }
        }
    }
    
    private fun emitGesture(event: GestureEvent) {
        // This would use a coroutine to emit
        // For now, trigger the action directly
        handleGestureAction(event)
    }
    
    // ========== Gesture Actions ==========
    
    private fun handleGestureAction(event: GestureEvent) {
        val actionKey = event.actionKey
        customGestures[actionKey]?.let { action ->
            action.execute()
        }
    }
    
    private fun registerDefaultGestures() {
        // Navigation gestures
        customGestures["swipe_right"] = GestureAction(
            nameHindi = "अगला आइटम",
            nameEnglish = "Next item",
            action = { /* Navigation handled by listener */ }
        )
        
        customGestures["swipe_left"] = GestureAction(
            nameHindi = "पिछला आइटम",
            nameEnglish = "Previous item",
            action = { /* Navigation handled by listener */ }
        )
        
        customGestures["swipe_up"] = GestureAction(
            nameHindi = "ऊपर स्क्रॉल करें",
            nameEnglish = "Scroll up",
            action = { }
        )
        
        customGestures["swipe_down"] = GestureAction(
            nameHindi = "नीचे स्क्रॉल करें",
            nameEnglish = "Scroll down",
            action = { }
        )
        
        // Quick actions
        customGestures["double_tap"] = GestureAction(
            nameHindi = "चुनें/सक्रिय करें",
            nameEnglish = "Select/Activate",
            action = { }
        )
        
        customGestures["long_press"] = GestureAction(
            nameHindi = "विकल्प मेनू",
            nameEnglish = "Options menu",
            action = { }
        )
        
        // Two finger gestures
        customGestures["two_finger_tap"] = GestureAction(
            nameHindi = "पढ़ना रोकें/शुरू करें",
            nameEnglish = "Pause/Resume reading",
            action = { }
        )
        
        // Three finger gestures
        customGestures["three_finger_tap"] = GestureAction(
            nameHindi = "मुख्य मेनू",
            nameEnglish = "Main menu",
            action = { }
        )
        
        // Edge gestures
        customGestures["edge_swipe_left"] = GestureAction(
            nameHindi = "पीछे जाएं",
            nameEnglish = "Go back",
            action = { }
        )
        
        customGestures["edge_swipe_right"] = GestureAction(
            nameHindi = "साइड मेनू खोलें",
            nameEnglish = "Open side menu",
            action = { }
        )
        
        customGestures["edge_swipe_up"] = GestureAction(
            nameHindi = "त्वरित सेटिंग्स",
            nameEnglish = "Quick settings",
            action = { }
        )
        
        customGestures["edge_swipe_down"] = GestureAction(
            nameHindi = "सूचनाएं",
            nameEnglish = "Notifications",
            action = { }
        )
    }
    
    // ========== Gesture Hints ==========
    
    /**
     * Get gesture hints for current screen.
     */
    fun getGestureHintsForScreen(screenType: ScreenType): List<GestureHint> {
        return when (screenType) {
            ScreenType.HOME -> listOf(
                GestureHint(
                    gesture = "swipe_right",
                    hindiDescription = "दाएं स्वाइप करें - विषय चुनें",
                    englishDescription = "Swipe right - Select subject"
                ),
                GestureHint(
                    gesture = "double_tap",
                    hindiDescription = "डबल टैप - खोलें",
                    englishDescription = "Double tap - Open"
                ),
                GestureHint(
                    gesture = "three_finger_tap",
                    hindiDescription = "तीन उंगली से टैप - प्रोफाइल",
                    englishDescription = "Three finger tap - Profile"
                )
            )
            
            ScreenType.CHAPTER_LIST -> listOf(
                GestureHint(
                    gesture = "swipe_up",
                    hindiDescription = "ऊपर स्वाइप - अगला अध्याय",
                    englishDescription = "Swipe up - Next chapter"
                ),
                GestureHint(
                    gesture = "swipe_down",
                    hindiDescription = "नीचे स्वाइप - पिछला अध्याय",
                    englishDescription = "Swipe down - Previous chapter"
                ),
                GestureHint(
                    gesture = "double_tap",
                    hindiDescription = "डबल टैप - अध्याय शुरू करें",
                    englishDescription = "Double tap - Start chapter"
                )
            )
            
            ScreenType.AUDIO_PLAYER -> listOf(
                GestureHint(
                    gesture = "two_finger_tap",
                    hindiDescription = "दो उंगली टैप - रोकें/चलाएं",
                    englishDescription = "Two finger tap - Play/Pause"
                ),
                GestureHint(
                    gesture = "swipe_right",
                    hindiDescription = "दाएं स्वाइप - 10 सेकंड आगे",
                    englishDescription = "Swipe right - Forward 10 seconds"
                ),
                GestureHint(
                    gesture = "swipe_left",
                    hindiDescription = "बाएं स्वाइप - 10 सेकंड पीछे",
                    englishDescription = "Swipe left - Rewind 10 seconds"
                )
            )
            
            ScreenType.QUIZ -> listOf(
                GestureHint(
                    gesture = "swipe_up",
                    hindiDescription = "ऊपर स्वाइप - अगला विकल्प",
                    englishDescription = "Swipe up - Next option"
                ),
                GestureHint(
                    gesture = "swipe_down",
                    hindiDescription = "नीचे स्वाइप - पिछला विकल्प",
                    englishDescription = "Swipe down - Previous option"
                ),
                GestureHint(
                    gesture = "double_tap",
                    hindiDescription = "डबल टैप - उत्तर सबमिट करें",
                    englishDescription = "Double tap - Submit answer"
                ),
                GestureHint(
                    gesture = "three_finger_tap",
                    hindiDescription = "तीन उंगली - प्रश्न दोहराएं",
                    englishDescription = "Three finger - Repeat question"
                )
            )
            
            ScreenType.READING -> listOf(
                GestureHint(
                    gesture = "swipe_right",
                    hindiDescription = "दाएं स्वाइप - अगला पैराग्राफ",
                    englishDescription = "Swipe right - Next paragraph"
                ),
                GestureHint(
                    gesture = "swipe_left",
                    hindiDescription = "बाएं स्वाइप - पिछला पैराग्राफ",
                    englishDescription = "Swipe left - Previous paragraph"
                ),
                GestureHint(
                    gesture = "two_finger_tap",
                    hindiDescription = "दो उंगली - पढ़ना रोकें",
                    englishDescription = "Two finger - Pause reading"
                ),
                GestureHint(
                    gesture = "long_press",
                    hindiDescription = "लंबा दबाएं - बुकमार्क करें",
                    englishDescription = "Long press - Bookmark"
                )
            )
        }
    }
    
    /**
     * Announce gesture hints via TTS.
     */
    fun announceGestureHints(
        screenType: ScreenType,
        isHindi: Boolean,
        ttsCallback: (String) -> Unit
    ) {
        val hints = getGestureHintsForScreen(screenType)
        val announcement = buildString {
            append(if (isHindi) "उपलब्ध जेस्चर: " else "Available gestures: ")
            hints.forEachIndexed { index, hint ->
                append(if (isHindi) hint.hindiDescription else hint.englishDescription)
                if (index < hints.size - 1) append(". ")
            }
        }
        ttsCallback(announcement)
    }
}

/**
 * Gesture events.
 */
sealed class GestureEvent(val actionKey: String) {
    // Basic swipes
    data object SwipeLeft : GestureEvent("swipe_left")
    data object SwipeRight : GestureEvent("swipe_right")
    data object SwipeUp : GestureEvent("swipe_up")
    data object SwipeDown : GestureEvent("swipe_down")
    
    // Edge swipes
    data object EdgeSwipeLeft : GestureEvent("edge_swipe_left")
    data object EdgeSwipeRight : GestureEvent("edge_swipe_right")
    data object EdgeSwipeUp : GestureEvent("edge_swipe_up")
    data object EdgeSwipeDown : GestureEvent("edge_swipe_down")
    
    // Taps
    data class DoubleTap(val x: Float, val y: Float) : GestureEvent("double_tap")
    data class LongPress(val x: Float, val y: Float) : GestureEvent("long_press")
    
    // Multi-finger
    data object TwoFingerTap : GestureEvent("two_finger_tap")
    data object ThreeFingerTap : GestureEvent("three_finger_tap")
}

/**
 * Gesture action configuration.
 */
data class GestureAction(
    val nameHindi: String,
    val nameEnglish: String,
    val action: () -> Unit
) {
    fun execute() = action()
}

/**
 * Gesture hint for user guidance.
 */
data class GestureHint(
    val gesture: String,
    val hindiDescription: String,
    val englishDescription: String
)

/**
 * Screen types for gesture hints.
 */
enum class ScreenType {
    HOME, CHAPTER_LIST, AUDIO_PLAYER, QUIZ, READING
}
