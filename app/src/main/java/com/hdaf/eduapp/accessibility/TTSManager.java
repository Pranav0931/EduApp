package com.hdaf.eduapp.accessibility;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * Singleton manager for Text-to-Speech functionality.
 * Provides consistent TTS experience across the app for blind users.
 */
public class TTSManager {

    private static final String TAG = "TTSManager";
    private static TTSManager instance;

    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private float speechRate = 1.0f;
    private TTSListener listener;

    public interface TTSListener {
        void onTTSReady();

        void onTTSStart(String utteranceId);

        void onTTSDone(String utteranceId);

        void onTTSError(String utteranceId);
    }

    private TTSManager() {
        // Private constructor for singleton
    }

    public static synchronized TTSManager getInstance() {
        if (instance == null) {
            instance = new TTSManager();
        }
        return instance;
    }

    /**
     * Initialize the TTS engine. Call this early in the app lifecycle.
     */
    public void initialize(Context context) {
        if (textToSpeech != null) {
            return; // Already initialized
        }

        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "English language not supported");
                } else {
                    isInitialized = true;
                    textToSpeech.setSpeechRate(speechRate);
                    setupUtteranceListener();
                    if (listener != null) {
                        listener.onTTSReady();
                    }
                    Log.d(TAG, "TTS initialized successfully");
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }

    private void setupUtteranceListener() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (listener != null) {
                    listener.onTTSStart(utteranceId);
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (listener != null) {
                    listener.onTTSDone(utteranceId);
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (listener != null) {
                    listener.onTTSError(utteranceId);
                }
            }
        });
    }

    /**
     * Speak the given text. Clears any pending speech.
     */
    public void speak(String text) {
        speak(text, TextToSpeech.QUEUE_FLUSH, "default");
    }

    /**
     * Speak the given text with specific queue mode.
     */
    public void speak(String text, int queueMode, String utteranceId) {
        if (!isInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not initialized");
            return;
        }

        android.os.Bundle params = new android.os.Bundle();
        textToSpeech.speak(text, queueMode, params, utteranceId);
    }

    /**
     * Add text to the speech queue without clearing existing speech.
     */
    public void speakAdd(String text, String utteranceId) {
        speak(text, TextToSpeech.QUEUE_ADD, utteranceId);
    }

    /**
     * Stop any current speech.
     */
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    /**
     * Check if TTS is currently speaking.
     */
    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    /**
     * Set the speech rate (0.5 to 2.0, 1.0 is normal).
     */
    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.5f, Math.min(2.0f, rate));
        if (textToSpeech != null && isInitialized) {
            textToSpeech.setSpeechRate(this.speechRate);
        }
    }

    public float getSpeechRate() {
        return speechRate;
    }

    /**
     * Set the language for TTS.
     */
    public boolean setLanguage(Locale locale) {
        if (textToSpeech != null && isInitialized) {
            int result = textToSpeech.setLanguage(locale);
            return result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED;
        }
        return false;
    }

    /**
     * Set listener for TTS events.
     */
    public void setListener(TTSListener listener) {
        this.listener = listener;
    }

    /**
     * Check if TTS is ready for use.
     */
    public boolean isReady() {
        return isInitialized && textToSpeech != null;
    }

    /**
     * Shutdown TTS engine. Call when app is closing.
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            isInitialized = false;
        }
    }
}
