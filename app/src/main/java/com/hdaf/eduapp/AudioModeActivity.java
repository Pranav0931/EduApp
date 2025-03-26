package com.hdaf.eduapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioModeActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playAudioButton, stopAudioButton, ttsButton, clearTextButton;
    private EditText editText;
    private SeekBar seekBar;
    private Spinner languageSpinner;
    private TextView timeStamp;
    private TextToSpeech textToSpeech;
    private String selectedLanguage = "en";
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_mode);

        playAudioButton = findViewById(R.id.playAudioButton);
        stopAudioButton = findViewById(R.id.stopAudioButton);
        ttsButton = findViewById(R.id.ttsButton);
        clearTextButton = findViewById(R.id.clearTextButton);
        editText = findViewById(R.id.editText);
        seekBar = findViewById(R.id.seekBar);
        languageSpinner = findViewById(R.id.languageSpinner);
        timeStamp = findViewById(R.id.timeStamp);

        // Initialize MediaPlayer with the MP3 file from raw folder
        mediaPlayer = MediaPlayer.create(this, R.raw.chapter1);

        // Set SeekBar max value to audio duration
        seekBar.setMax(mediaPlayer.getDuration());

        // Populate language options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"English", "Hindi", "Marathi"});
        languageSpinner.setAdapter(adapter);

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

        ttsButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                textToSpeech.setLanguage(new Locale(selectedLanguage));
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Clear Text Button
        clearTextButton.setOnClickListener(v -> editText.setText(""));

        // Play Audio Button
        playAudioButton.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateSeekBar();
            }
        });

        // Stop Audio Button
        stopAudioButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                seekBar.setProgress(0);
                timeStamp.setText(formatTime(0, mediaPlayer.getDuration())); // Reset timestamp
            }
        });

        // Update SeekBar and Timestamp while playing
        mediaPlayer.setOnCompletionListener(mp -> {
            seekBar.setProgress(0);
            timeStamp.setText(formatTime(0, mediaPlayer.getDuration()));
        });

        // SeekBar Change Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    timeStamp.setText(formatTime(progress, mediaPlayer.getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Method to update SeekBar and Timestamp
    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    timeStamp.setText(formatTime(currentPosition, mediaPlayer.getDuration()));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    // Convert milliseconds to MM:SS format
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
        handler.removeCallbacksAndMessages(null); // Stop updating seekBar
        super.onDestroy();
    }
}
