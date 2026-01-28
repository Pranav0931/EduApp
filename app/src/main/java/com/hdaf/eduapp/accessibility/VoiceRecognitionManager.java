package com.hdaf.eduapp.accessibility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manager for speech recognition functionality.
 * Handles voice input for EduAI commands.
 * Supports English and Hindi/Hinglish speech.
 */
public class VoiceRecognitionManager {

    private static final String TAG = "VoiceRecognitionManager";
    private static VoiceRecognitionManager instance;

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private VoiceCallback callback;
    private boolean isListening = false;
    private Context context;

    public interface VoiceCallback {
        void onReadyForSpeech();
        void onBeginningOfSpeech();
        void onEndOfSpeech();
        void onResults(String text);
        void onPartialResults(String text);
        void onError(int errorCode, String errorMessage);
    }

    private VoiceRecognitionManager() {
        // Private constructor for singleton
    }

    public static synchronized VoiceRecognitionManager getInstance() {
        if (instance == null) {
            instance = new VoiceRecognitionManager();
        }
        return instance;
    }

    /**
     * Initialize the speech recognizer.
     * Must be called before using other methods.
     */
    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        
        if (SpeechRecognizer.isRecognitionAvailable(this.context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.context);
            speechRecognizer.setRecognitionListener(createRecognitionListener());
            
            // Create intent for speech recognition
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            
            // Support for Hindi and English
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
            
            Log.d(TAG, "Speech recognizer initialized");
        } else {
            Log.e(TAG, "Speech recognition not available on this device");
        }
    }

    /**
     * Set the language for recognition.
     * Supports "en" for English and "hi" for Hindi.
     */
    public void setLanguage(String languageCode) {
        if (recognizerIntent != null) {
            String locale;
            switch (languageCode.toLowerCase()) {
                case "hi":
                case "hindi":
                    locale = "hi-IN";
                    break;
                case "mr":
                case "marathi":
                    locale = "mr-IN";
                    break;
                default:
                    locale = "en-IN";
            }
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
            Log.d(TAG, "Language set to: " + locale);
        }
    }

    /**
     * Start listening for voice input.
     */
    public void startListening(VoiceCallback callback) {
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized");
            if (callback != null) {
                callback.onError(SpeechRecognizer.ERROR_CLIENT, "Speech recognizer not initialized");
            }
            return;
        }

        this.callback = callback;
        isListening = true;
        
        try {
            speechRecognizer.startListening(recognizerIntent);
            Log.d(TAG, "Started listening");
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
            isListening = false;
            if (callback != null) {
                callback.onError(SpeechRecognizer.ERROR_CLIENT, e.getMessage());
            }
        }
    }

    /**
     * Stop listening for voice input.
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.d(TAG, "Stopped listening");
        }
    }

    /**
     * Cancel current recognition.
     */
    public void cancel() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            isListening = false;
            Log.d(TAG, "Recognition cancelled");
        }
    }

    /**
     * Check if currently listening.
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * Check if speech recognition is available.
     */
    public boolean isAvailable() {
        return context != null && SpeechRecognizer.isRecognitionAvailable(context);
    }

    /**
     * Release resources.
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        isListening = false;
        callback = null;
    }

    /**
     * Create recognition listener for handling speech events.
     */
    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
                if (callback != null) {
                    callback.onReadyForSpeech();
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
                if (callback != null) {
                    callback.onBeginningOfSpeech();
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Can be used for visual feedback of voice level
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not typically used
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
                isListening = false;
                if (callback != null) {
                    callback.onEndOfSpeech();
                }
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Recognition error: " + error);
                isListening = false;
                
                String errorMessage = getErrorMessage(error);
                if (callback != null) {
                    callback.onError(error, errorMessage);
                }
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    Log.d(TAG, "Recognition result: " + result);
                    if (callback != null) {
                        callback.onResults(result);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                
                if (matches != null && !matches.isEmpty()) {
                    String partial = matches.get(0);
                    Log.d(TAG, "Partial result: " + partial);
                    if (callback != null) {
                        callback.onPartialResults(partial);
                    }
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not typically used
            }
        };
    }

    /**
     * Get human-readable error message.
     */
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech detected";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Speech timeout";
            default:
                return "Unknown error";
        }
    }
}
