package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Screen Reader Helper for enhanced accessibility support.
 * Provides optimized accessibility labels and navigation hints for blind users.
 * 
 * This helper ensures the app works seamlessly with TalkBack and other screen readers,
 * providing meaningful context and navigation instructions in Hindi and English.
 */
@Singleton
class ScreenReaderHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val accessibilityManager: AccessibilityManager? by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    }
    
    /**
     * Check if a screen reader (like TalkBack) is enabled.
     */
    val isScreenReaderEnabled: Boolean
        get() = accessibilityManager?.isEnabled == true &&
                accessibilityManager?.isTouchExplorationEnabled == true
    
    /**
     * Announce a message to screen reader users.
     */
    fun announce(view: View, message: String) {
        view.announceForAccessibility(message)
    }
    
    /**
     * Announce with priority (interrupts current speech).
     */
    fun announceUrgent(view: View, message: String) {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(message)
        accessibilityManager?.sendAccessibilityEvent(event)
    }
    
    // ==================== Card Accessibility ====================
    
    /**
     * Set up accessibility for a book card.
     */
    fun setupBookCard(
        card: MaterialCardView,
        title: String,
        subject: String,
        progressPercent: Int,
        isDownloaded: Boolean,
        position: Int,
        totalItems: Int
    ) {
        val accessibilityText = buildString {
            append("पुस्तक $position में से $totalItems: ")
            append("$title, विषय $subject, ")
            append("$progressPercent प्रतिशत पूर्ण")
            if (isDownloaded) append(", ऑफ़लाइन उपलब्ध")
            append(". खोलने के लिए डबल टैप करें.")
        }
        
        card.contentDescription = accessibilityText
        
        ViewCompat.setAccessibilityDelegate(card, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        "पुस्तक खोलें"
                    )
                )
            }
        })
    }
    
    /**
     * Set up accessibility for a quiz card.
     */
    fun setupQuizCard(
        card: MaterialCardView,
        title: String,
        questionCount: Int,
        durationMinutes: Int,
        difficulty: String,
        position: Int,
        totalItems: Int
    ) {
        val difficultyHindi = when (difficulty.lowercase()) {
            "easy" -> "आसान"
            "medium" -> "मध्यम"
            "hard" -> "कठिन"
            else -> difficulty
        }
        
        val accessibilityText = buildString {
            append("प्रश्नोत्तरी $position में से $totalItems: ")
            append("$title, ")
            append("$questionCount प्रश्न, ")
            append("$durationMinutes मिनट, ")
            append("कठिनाई: $difficultyHindi. ")
            append("शुरू करने के लिए डबल टैप करें.")
        }
        
        card.contentDescription = accessibilityText
        
        ViewCompat.setAccessibilityDelegate(card, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        "प्रश्नोत्तरी शुरू करें"
                    )
                )
            }
        })
    }
    
    /**
     * Set up accessibility for a chapter card.
     */
    fun setupChapterCard(
        card: MaterialCardView,
        title: String,
        chapterNumber: Int,
        isCompleted: Boolean,
        isLocked: Boolean,
        position: Int,
        totalItems: Int
    ) {
        val accessibilityText = buildString {
            append("अध्याय $position में से $totalItems: ")
            append("अध्याय $chapterNumber, $title")
            if (isCompleted) {
                append(", पूर्ण")
            } else if (isLocked) {
                append(", लॉक है")
            }
            
            if (!isLocked) {
                append(". पढ़ने के लिए डबल टैप करें.")
            }
        }
        
        card.contentDescription = accessibilityText
        card.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        
        if (isLocked) {
            // Already set above, but ensure accessibility delegate
            ViewCompat.setAccessibilityDelegate(card, object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isEnabled = false
                    info.stateDescription = "लॉक"
                }
            })
        }
    }
    
    // ==================== Navigation Elements ====================
    
    /**
     * Set up accessibility for navigation buttons.
     */
    fun setupNavigationButton(
        button: View,
        actionDescription: String,
        contextHint: String? = null
    ) {
        val fullDescription = if (contextHint != null) {
            "$actionDescription। $contextHint"
        } else {
            actionDescription
        }
        
        button.contentDescription = fullDescription
        
        ViewCompat.setAccessibilityDelegate(button, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        actionDescription
                    )
                )
            }
        })
    }
    
    /**
     * Set up accessibility for back button.
     */
    fun setupBackButton(button: ImageButton, destinationHint: String? = null) {
        val description = if (destinationHint != null) {
            "वापस जाएं, $destinationHint पर"
        } else {
            "वापस जाएं"
        }
        button.contentDescription = description
    }
    
    /**
     * Set up accessibility for close button.
     */
    fun setupCloseButton(button: ImageButton, screenName: String? = null) {
        val description = if (screenName != null) {
            "$screenName बंद करें"
        } else {
            "बंद करें"
        }
        button.contentDescription = description
    }
    
    // ==================== Progress Indicators ====================
    
    /**
     * Set up accessibility for progress indicator.
     */
    fun setupProgressIndicator(
        view: View,
        progressPercent: Int,
        contextDescription: String
    ) {
        view.contentDescription = "$contextDescription: $progressPercent प्रतिशत पूर्ण"
        
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.ProgressBar"
                info.rangeInfo = AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(
                    AccessibilityNodeInfoCompat.RangeInfoCompat.RANGE_TYPE_PERCENT,
                    0f, 100f, progressPercent.toFloat()
                )
            }
        })
    }
    
    // ==================== Quiz Accessibility ====================
    
    /**
     * Announce quiz question for screen reader.
     */
    fun announceQuizQuestion(
        view: View,
        questionNumber: Int,
        totalQuestions: Int,
        questionText: String,
        options: List<String>,
        selectedOption: Int? = null
    ) {
        val announcement = buildString {
            append("प्रश्न $questionNumber में से $totalQuestions: ")
            append(questionText)
            append(". विकल्प हैं: ")
            options.forEachIndexed { index, option ->
                val optionLabel = when (index) {
                    0 -> "ए"
                    1 -> "बी"
                    2 -> "सी"
                    3 -> "डी"
                    else -> "${index + 1}"
                }
                append("$optionLabel, $option")
                if (selectedOption == index) {
                    append(", चयनित")
                }
                append(". ")
            }
        }
        
        announce(view, announcement)
    }
    
    /**
     * Announce quiz result for screen reader.
     */
    fun announceQuizResult(
        view: View,
        correctAnswers: Int,
        totalQuestions: Int,
        scorePercent: Int,
        xpEarned: Int
    ) {
        val resultMessage = buildString {
            append("प्रश्नोत्तरी परिणाम: ")
            append("$totalQuestions में से $correctAnswers सही, ")
            append("$scorePercent प्रतिशत अंक, ")
            append("$xpEarned XP अर्जित। ")
            
            when {
                scorePercent >= 90 -> append("उत्कृष्ट! बहुत अच्छा प्रदर्शन!")
                scorePercent >= 70 -> append("बहुत अच्छा! अच्छा प्रदर्शन!")
                scorePercent >= 50 -> append("ठीक है! और अभ्यास करें।")
                else -> append("अभ्यास जारी रखें, आप बेहतर करेंगे!")
            }
        }
        
        announce(view, resultMessage)
    }
    
    // ==================== List Navigation ====================
    
    /**
     * Set up accessibility for RecyclerView with position announcements.
     */
    fun setupAccessibleList(
        recyclerView: RecyclerView,
        listName: String,
        itemType: String
    ) {
        recyclerView.contentDescription = "$listName, $itemType की सूची"
        
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isScreenReaderEnabled && (dx != 0 || dy != 0)) {
                    val layoutManager = recyclerView.layoutManager
                    val firstVisible = when (layoutManager) {
                        is androidx.recyclerview.widget.LinearLayoutManager -> 
                            layoutManager.findFirstCompletelyVisibleItemPosition()
                        is androidx.recyclerview.widget.GridLayoutManager -> 
                            layoutManager.findFirstCompletelyVisibleItemPosition()
                        else -> RecyclerView.NO_POSITION
                    }
                    
                    val lastVisible = when (layoutManager) {
                        is androidx.recyclerview.widget.LinearLayoutManager -> 
                            layoutManager.findLastCompletelyVisibleItemPosition()
                        is androidx.recyclerview.widget.GridLayoutManager -> 
                            layoutManager.findLastCompletelyVisibleItemPosition()
                        else -> RecyclerView.NO_POSITION
                    }
                    
                    val totalItems = recyclerView.adapter?.itemCount ?: 0
                    
                    if (firstVisible != RecyclerView.NO_POSITION && totalItems > 0) {
                        // Announce position only for significant scroll changes
                        if (firstVisible == 0 || lastVisible == totalItems - 1) {
                            val position = if (lastVisible == totalItems - 1) "अंतिम" else "पहला"
                            announce(recyclerView, "$position $itemType दिख रहा है")
                        }
                    }
                }
            }
        })
    }
    
    // ==================== Screen Transitions ====================
    
    /**
     * Announce screen change for screen reader.
     */
    fun announceScreenChange(view: View, screenName: String, contextInfo: String? = null) {
        val announcement = if (contextInfo != null) {
            "$screenName स्क्रीन। $contextInfo"
        } else {
            "$screenName स्क्रीन"
        }
        
        // Small delay to let screen settle
        view.postDelayed({
            announce(view, announcement)
        }, 300)
    }
    
    /**
     * Announce loading state.
     */
    fun announceLoading(view: View, itemName: String) {
        announce(view, "$itemName लोड हो रहा है। कृपया प्रतीक्षा करें।")
    }
    
    /**
     * Announce loaded state.
     */
    fun announceLoaded(view: View, itemName: String, count: Int? = null) {
        val message = if (count != null) {
            "$itemName लोड हो गया। $count आइटम उपलब्ध।"
        } else {
            "$itemName लोड हो गया।"
        }
        announce(view, message)
    }
    
    /**
     * Announce error state.
     */
    fun announceError(view: View, errorMessage: String) {
        announce(view, "त्रुटि: $errorMessage। पुनः प्रयास करने के लिए पुनः प्रयास बटन पर डबल टैप करें।")
    }
    
    /**
     * Announce empty state.
     */
    fun announceEmpty(view: View, itemType: String) {
        announce(view, "कोई $itemType नहीं मिला। ")
    }
}
