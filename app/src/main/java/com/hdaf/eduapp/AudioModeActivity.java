package com.hdaf.eduapp;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AudioModeActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private EditText editText;
    private SeekBar seekBar;
    private TextView timeStamp;
    private TextToSpeech textToSpeech;
    private String selectedLanguage = "en";
    private String selectedChapter = "Chapter 1"; // Default chapter
    private final Handler handler = new Handler();
    private boolean isSeeking = false; // To track seekbar movement

    // Mapping chapter names to raw audio files
    private Map<String, Integer> chapterAudioMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_mode);

        // Initialize UI components
        Button playAudioButton = findViewById(R.id.playAudioButton);
        Button stopAudioButton = findViewById(R.id.stopAudioButton);
        Button ttsButton = findViewById(R.id.ttsButton);
        Button clearTextButton = findViewById(R.id.clearTextButton);
        editText = findViewById(R.id.editText);
        seekBar = findViewById(R.id.seekBar);
        Spinner languageSpinner = findViewById(R.id.languageSpinner);
        Spinner chapterSpinner = findViewById(R.id.chapterSpinner);
        timeStamp = findViewById(R.id.timeStamp);

        // Initialize chapter-to-audio mapping
        chapterAudioMap = new HashMap<>();
        chapterAudioMap.put("Chapter 1", R.raw.chapter1);
        chapterAudioMap.put("Chapter 2", R.raw.chapter2);
        chapterAudioMap.put("Chapter 3", R.raw.chapter3);
        // Add more chapters here...

        // Populate Chapter Spinner
        ArrayAdapter<String> chapterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Chapter 1", "Chapter 2", "Chapter 3"});
        chapterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chapterSpinner.setAdapter(chapterAdapter);

        chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChapter = parent.getItemAtPosition(position).toString();
                changeAudioFile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Populate Language Spinner
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"English", "Hindi", "Marathi"});
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) selectedLanguage = "en";
                else if (position == 1) selectedLanguage = "hi";
                else selectedLanguage = "mr";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // Text-to-Speech Button
        ttsButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                Locale locale;
                switch (selectedLanguage) {
                    case "hi":
                        locale = new Locale("hi", "IN");
                        break;
                    case "mr":
                        locale = new Locale("mr", "IN");
                        break;
                    default:
                        locale = Locale.ENGLISH;
                }
                int result = textToSpeech.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported!", Toast.LENGTH_SHORT).show();
                } else {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        // Clear Text Button
        clearTextButton.setOnClickListener(v -> editText.setText(""));

        // Play Audio Button
        playAudioButton.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                Toast.makeText(this, "Please select a valid chapter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateSeekBar();
            }
        });

        // Stop Audio Button
        stopAudioButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                seekBar.setProgress(0);
                timeStamp.setText(formatTime(0, mediaPlayer.getDuration()));
            }
        });

        // SeekBar Change Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    timeStamp.setText(formatTime(progress, mediaPlayer.getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        // Initialize MediaPlayer
        changeAudioFile();
    }

    // Method to change audio file when a different chapter is selected
    private void changeAudioFile() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Integer audioResId = chapterAudioMap.get(selectedChapter);

        if (audioResId == null) {  // Prevent crash if chapter is missing
            Toast.makeText(this, "Audio file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = MediaPlayer.create(this, audioResId);

        if (mediaPlayer == null) {  // Prevent crash if mediaPlayer fails
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer.setOnPreparedListener(mp -> seekBar.setMax(mp.getDuration()));

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            timeStamp.setText(formatTime(0, mediaPlayer.getDuration()));
            handler.removeCallbacksAndMessages(null);
        });
    }

    // Method to update SeekBar and Timestamp
    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && !isSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    timeStamp.setText(formatTime(currentPosition, mediaPlayer.getDuration()));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    // Convert milliseconds to MM:SS format
    @SuppressLint("DefaultLocale")
    private String formatTime(int currentMillis, int totalMillis) {
        return String.format("%02d:%02d / %02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentMillis),
                TimeUnit.MILLISECONDS.toSeconds(currentMillis) % 60,
                TimeUnit.MILLISECONDS.toMinutes(totalMillis),
                TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60);
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
