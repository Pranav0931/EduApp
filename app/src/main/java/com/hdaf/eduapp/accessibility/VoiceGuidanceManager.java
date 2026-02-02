package com.hdaf.eduapp.accessibility;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;

import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Comprehensive Voice Guidance Manager for blind users.
 * Provides TalkBack-style voice feedback for all app interactions.
 * 
 * Features:
 * - Voice announcements for screen changes
 * - Voice feedback for button presses
 * - Voice guidance for navigation
 * - Haptic feedback for confirmations
 * - Speed and pitch controls
 */
public class VoiceGuidanceManager {

    private static final String TAG = "VoiceGuidanceManager";
    private static VoiceGuidanceManager instance;

    private final Context context;
    private final TTSManager ttsManager;
    private final PreferenceManager prefManager;
    private Vibrator vibrator;
    private AccessibilityManager accessibilityManager;

    // Settings
    private boolean isEnabled = true;
    private boolean hapticEnabled = true;
    private float speechPitch = 1.0f;

    // Announcement types
    public enum AnnouncementType {
        SCREEN_CHANGE,      // When navigating to a new screen
        BUTTON_PRESS,       // When a button is clicked
        SELECTION,          // When an item is selected
        CONFIRMATION,       // When an action is confirmed
        ERROR,              // When an error occurs
        HINT,               // Helpful hints
        PROGRESS,           // Progress updates
        NAVIGATION,         // Navigation actions
        INFORMATION,        // General information
        ACTION              // User actions
    }
    
    // Haptic patterns
    public enum HapticPattern {
        LIGHT,              // Light tap
        SELECTION,          // Selection feedback
        SUCCESS,            // Success confirmation
        ERROR,              // Error feedback
        NAVIGATION          // Navigation feedback
    }

    private VoiceGuidanceManager(Context context) {
        this.context = context.getApplicationContext();
        this.ttsManager = TTSManager.getInstance();
        this.prefManager = PreferenceManager.getInstance(context);
        
        // Initialize vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        // Initialize accessibility manager
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        
        // Load settings
        loadSettings();
    }

    public static synchronized VoiceGuidanceManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceGuidanceManager(context);
        }
        return instance;
    }

    /**
     * Load settings from preferences
     */
    private void loadSettings() {
        isEnabled = prefManager.isVoiceGuidanceEnabled();
        hapticEnabled = prefManager.isHapticFeedbackEnabled();
        speechPitch = prefManager.getVoicePitch();
    }

    /**
     * Announce a message with specified type
     */
    public void announce(@NonNull String message, AnnouncementType type) {
        if (!isEnabled || message.isEmpty()) {
            return;
        }

        // Provide haptic feedback based on type
        if (hapticEnabled) {
            provideHapticFeedback(type);
        }

        // Speak the announcement
        switch (type) {
            case SCREEN_CHANGE:
                // Add a brief pause before screen change announcements
                ttsManager.speak(message);
                break;
            case BUTTON_PRESS:
                // Quick feedback for buttons
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "button");
                break;
            case SELECTION:
                ttsManager.speak("Selected: " + message, TextToSpeech.QUEUE_FLUSH, "selection");
                break;
            case CONFIRMATION:
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "confirm");
                break;
            case ERROR:
                ttsManager.speak("Error: " + message, TextToSpeech.QUEUE_FLUSH, "error");
                break;
            case HINT:
                ttsManager.speakAdd(message, "hint");
                break;
            case PROGRESS:
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "progress");
                break;
            case NAVIGATION:
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "navigation");
                break;
            case INFORMATION:
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "info");
                break;
            case ACTION:
                ttsManager.speak(message, TextToSpeech.QUEUE_FLUSH, "action");
                break;
        }
    }
    
    /**
     * Vibrate with haptic pattern
     */
    public void vibrate(HapticPattern pattern) {
        if (!hapticEnabled || vibrator == null) return;
        
        long duration;
        switch (pattern) {
            case LIGHT:
                duration = 20;
                break;
            case SELECTION:
                duration = 50;
                break;
            case SUCCESS:
                duration = 100;
                break;
            case ERROR:
                duration = 200;
                break;
            case NAVIGATION:
                duration = 30;
                break;
            default:
                duration = 50;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }
    
    /**
     * Announce with delay
     */
    public void announceDelayed(@NonNull String message, long delayMs) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            announce(message, AnnouncementType.INFORMATION);
        }, delayMs);
    }

    /**
     * Announce screen change with context
     */
    public void announceScreen(@NonNull String screenName, String description) {
        if (!isEnabled) return;
        
        StringBuilder announcement = new StringBuilder();
        announcement.append(screenName);
        
        if (description != null && !description.isEmpty()) {
            announcement.append(". ").append(description);
        }
        
        announce(announcement.toString(), AnnouncementType.SCREEN_CHANGE);
    }

    /**
     * Announce button click
     */
    public void announceButtonPress(@NonNull String buttonName) {
        announce(buttonName, AnnouncementType.BUTTON_PRESS);
    }

    /**
     * Announce item selection
     */
    public void announceSelection(@NonNull String itemName) {
        announce(itemName, AnnouncementType.SELECTION);
    }

    /**
     * Announce error
     */
    public void announceError(@NonNull String errorMessage) {
        announce(errorMessage, AnnouncementType.ERROR);
    }

    /**
     * Announce progress
     */
    public void announceProgress(int percent) {
        announce(percent + "% complete", AnnouncementType.PROGRESS);
    }

    /**
     * Speak navigation hint
     */
    public void speakNavigationHint(@NonNull String hint) {
        if (!isEnabled) return;
        
        // Delay hint slightly so it comes after main announcement
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            announce(hint, AnnouncementType.HINT);
        }, 1000);
    }

    /**
     * Provide haptic feedback based on announcement type
     */
    private void provideHapticFeedback(AnnouncementType type) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        long[] pattern;
        int amplitude = VibrationEffect.DEFAULT_AMPLITUDE;

        switch (type) {
            case BUTTON_PRESS:
                // Short single vibration
                pattern = new long[]{0, 30};
                break;
            case SELECTION:
                // Two short vibrations
                pattern = new long[]{0, 30, 50, 30};
                break;
            case CONFIRMATION:
                // One long vibration
                pattern = new long[]{0, 100};
                break;
            case ERROR:
                // Three quick vibrations
                pattern = new long[]{0, 50, 50, 50, 50, 50};
                break;
            case SCREEN_CHANGE:
                // Medium vibration
                pattern = new long[]{0, 50};
                break;
            default:
                pattern = new long[]{0, 20};
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    /**
     * Set up accessibility for a view with voice feedback
     */
    public void setupAccessibleView(@NonNull View view, @NonNull String description) {
        view.setContentDescription(description);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        
        // Add focus change listener for TalkBack
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isEnabled) {
                ttsManager.speak(description);
            }
        });
    }

    /**
     * Add click listener with voice feedback
     */
    public void setAccessibleClickListener(@NonNull View view, @NonNull String clickAnnouncement, 
                                           @NonNull View.OnClickListener listener) {
        view.setOnClickListener(v -> {
            announceButtonPress(clickAnnouncement);
            listener.onClick(v);
        });
    }

    /**
     * Enable or disable voice guidance
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        prefManager.setVoiceGuidanceEnabled(enabled);
    }

    /**
     * Enable or disable haptic feedback
     */
    public void setHapticEnabled(boolean enabled) {
        this.hapticEnabled = enabled;
        prefManager.setHapticFeedbackEnabled(enabled);
    }

    /**
     * Set speech pitch (0.5 to 2.0)
     */
    public void setSpeechPitch(float pitch) {
        this.speechPitch = Math.max(0.5f, Math.min(2.0f, pitch));
        prefManager.setVoicePitch(this.speechPitch);
        // Note: TTSManager would need pitch support added
    }

    /**
     * Set speech rate
     */
    public void setSpeechRate(float rate) {
        ttsManager.setSpeechRate(rate);
    }

    /**
     * Check if system TalkBack is enabled
     */
    public boolean isSystemTalkBackEnabled() {
        return accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled();
    }

    /**
     * Check if voice guidance is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Stop current speech
     */
    public void stop() {
        ttsManager.stop();
    }

    /**
     * Speak text directly
     */
    public void speak(@NonNull String text) {
        if (isEnabled) {
            ttsManager.speak(text);
        }
    }

    /**
     * Read a list of items
     */
    public void readItemList(String listName, String[] items) {
        if (!isEnabled || items == null || items.length == 0) return;
        
        StringBuilder announcement = new StringBuilder();
        announcement.append(listName).append(". ");
        announcement.append(items.length).append(" items. ");
        
        for (int i = 0; i < Math.min(items.length, 5); i++) {
            announcement.append(i + 1).append(": ").append(items[i]).append(". ");
        }
        
        if (items.length > 5) {
            announcement.append("And ").append(items.length - 5).append(" more.");
        }
        
        ttsManager.speak(announcement.toString());
    }
}
