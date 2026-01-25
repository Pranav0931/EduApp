package com.hdaf.eduapp.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Audio Player screen for blind users.
 * Uses Text-to-Speech to read lesson content aloud.
 * Supports voice commands and variable speech rate.
 */
public class AudioPlayerActivity extends AppCompatActivity implements TTSManager.TTSListener {

    private TextView headerTitle;
    private TextView unitBadge;
    private TextView speedLabel;
    private SeekBar speedSeekBar;
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private View visualizationCircle;

    private TTSManager ttsManager;
    private PreferenceManager prefManager;

    private String lessonContent;
    private String chapterName;
    private String bookName;

    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        prefManager = PreferenceManager.getInstance(this);
        ttsManager = TTSManager.getInstance();
        ttsManager.initialize(this);
        ttsManager.setListener(this);

        // Get extras from intent
        lessonContent = getIntent().getStringExtra(Constants.EXTRA_LESSON_CONTENT);
        chapterName = getIntent().getStringExtra(Constants.EXTRA_CHAPTER_NAME);
        bookName = getIntent().getStringExtra(Constants.EXTRA_BOOK_NAME);

        if (lessonContent == null) {
            lessonContent = getString(R.string.sample_text_content);
        }

        initializeViews();
        setupListeners();

        // Load saved speed preference
        currentSpeed = prefManager.getTtsSpeed();
        updateSpeedUI();
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        headerTitle = findViewById(R.id.headerTitle);
        unitBadge = findViewById(R.id.unitBadge);
        speedLabel = findViewById(R.id.speedLabel);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        visualizationCircle = findViewById(R.id.visualizationCircle);

        // Set header and badge text
        if (bookName != null && bookName.contains("ENGLISH")) {
            headerTitle.setText("ENGLISH");
        }
        if (chapterName != null) {
            unitBadge.setText(chapterName);
        }

        menuButton.setOnClickListener(v -> onBackPressed());

        // Initial button state
        updatePlayPauseButton();
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        previousButton.setOnClickListener(v -> {
            // Stop current playback and go back
            stopPlayback();
            announceForAccessibility("Going to previous chapter");
            onBackPressed();
        });

        nextButton.setOnClickListener(v -> {
            // Stop current playback (in full app, would go to next chapter)
            stopPlayback();
            announceForAccessibility("Going to next chapter");
            // TODO: Navigate to next chapter
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Map 0-150 to 0.5-2.0
                currentSpeed = 0.5f + (progress / 100f);
                updateSpeedUI();

                if (ttsManager.isReady()) {
                    ttsManager.setSpeechRate(currentSpeed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefManager.setTtsSpeed(currentSpeed);
            }
        });
    }

    private void togglePlayPause() {
        if (isPlaying) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (!ttsManager.isReady()) {
            announceForAccessibility("Please wait, initializing...");
            return;
        }

        isPlaying = true;
        updatePlayPauseButton();

        ttsManager.setSpeechRate(currentSpeed);
        ttsManager.speak(lessonContent);

        announceForAccessibility(getString(R.string.talkback_audio_playing));

        // Animate visualization
        animateVisualization(true);
    }

    private void pausePlayback() {
        isPlaying = false;
        updatePlayPauseButton();
        ttsManager.stop();

        announceForAccessibility(getString(R.string.talkback_audio_paused));

        // Stop animation
        animateVisualization(false);
    }

    private void stopPlayback() {
        isPlaying = false;
        updatePlayPauseButton();
        ttsManager.stop();
        animateVisualization(false);
    }

    private void updatePlayPauseButton() {
        if (isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            playPauseButton.setContentDescription(getString(R.string.pause_button_description));
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play);
            playPauseButton.setContentDescription(getString(R.string.play_button_description));
        }
    }

    private void updateSpeedUI() {
        speedLabel.setText(String.format("Speed: %.1fx", currentSpeed));
        int progress = Math.round((currentSpeed - 0.5f) * 100);
        speedSeekBar.setProgress(progress);
    }

    private void animateVisualization(boolean animate) {
        if (animate) {
            // Simple scale animation for visualization
            visualizationCircle.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        if (isPlaying) {
                            visualizationCircle.animate()
                                    .scaleX(0.95f)
                                    .scaleY(0.95f)
                                    .setDuration(500)
                                    .withEndAction(() -> animateVisualization(isPlaying))
                                    .start();
                        }
                    })
                    .start();
        } else {
            visualizationCircle.animate().cancel();
            visualizationCircle.setScaleX(1f);
            visualizationCircle.setScaleY(1f);
        }
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }

    // TTSManager.TTSListener callbacks
    @Override
    public void onTTSReady() {
        runOnUiThread(() -> {
            ttsManager.setSpeechRate(currentSpeed);
        });
    }

    @Override
    public void onTTSStart(String utteranceId) {
        runOnUiThread(() -> {
            isPlaying = true;
            updatePlayPauseButton();
            animateVisualization(true);
        });
    }

    @Override
    public void onTTSDone(String utteranceId) {
        runOnUiThread(() -> {
            isPlaying = false;
            updatePlayPauseButton();
            animateVisualization(false);
        });
    }

    @Override
    public void onTTSError(String utteranceId) {
        runOnUiThread(() -> {
            isPlaying = false;
            updatePlayPauseButton();
            animateVisualization(false);
            announceForAccessibility(getString(R.string.error_audio_playback));
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.setListener(null);
    }
}
