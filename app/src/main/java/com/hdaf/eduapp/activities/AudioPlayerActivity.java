package com.hdaf.eduapp.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.accessibility.VoiceGuidanceManager;
import com.hdaf.eduapp.analytics.AnalyticsManager;
import com.hdaf.eduapp.analytics.LearningSession;
import com.hdaf.eduapp.utils.Constants;

/**
 * Audio Player screen for blind users.
 * Uses Text-to-Speech to read lesson content aloud.
 * Features modern gradient UI with large accessible controls.
 */
public class AudioPlayerActivity extends AppCompatActivity {

    private TextView headerTitle;
    private TextView chapterTitle;
    private TextView speedLabel;
    private Slider speedSlider;
    private ImageButton playPauseButton;
    private ImageButton backButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton rewindButton;
    private ImageButton forwardButton;

    private TTSManager ttsManager;
    private VoiceGuidanceManager voiceGuidance;
    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;

    private String lessonContent;
    private String chapterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        ttsManager = TTSManager.getInstance();
        ttsManager.initialize(this);
        voiceGuidance = VoiceGuidanceManager.getInstance(this);

        lessonContent = getIntent().getStringExtra(Constants.EXTRA_LESSON_CONTENT);
        chapterName = getIntent().getStringExtra(Constants.EXTRA_CHAPTER_NAME);

        if (lessonContent == null || lessonContent.isEmpty()) {
            lessonContent = "Welcome! Audio lesson content is not available yet.";
        }

        initializeViews();
        setupControls();
        
        // Voice announcement for accessibility
        voiceGuidance.announceScreen(getString(R.string.audio_player_title), 
            chapterName != null ? chapterName : getString(R.string.lesson_title_placeholder));
    }

    private void initializeViews() {
        headerTitle = findViewById(R.id.headerTitle);
        chapterTitle = findViewById(R.id.unitBadge);
        speedLabel = findViewById(R.id.speedLabel);
        speedSlider = findViewById(R.id.speedSlider);
        playPauseButton = findViewById(R.id.playPauseButton);
        backButton = findViewById(R.id.menuButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        rewindButton = findViewById(R.id.rewindButton);
        forwardButton = findViewById(R.id.forwardButton);

        headerTitle.setText(R.string.audio_player_title);
        chapterTitle.setText(chapterName != null ? chapterName : getString(R.string.lesson_title_placeholder));

        backButton.setOnClickListener(v -> {
            voiceGuidance.announceButtonPress(getString(R.string.back));
            onBackPressed();
        });
    }

    private void setupControls() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Speed slider setup (0.5x to 2.0x)
        speedSlider.setValue(1.0f);
        speedSlider.addOnChangeListener((slider, value, fromUser) -> {
            currentSpeed = value;
            ttsManager.setSpeechRate(currentSpeed);
            speedLabel.setText(String.format("%.1fx", currentSpeed));
            if (fromUser) {
                voiceGuidance.announce("Speed " + String.format("%.1f", currentSpeed), 
                    VoiceGuidanceManager.AnnouncementType.SELECTION);
            }
        });

        // Previous button
        if (previousButton != null) {
            previousButton.setOnClickListener(v -> {
                voiceGuidance.announceButtonPress(getString(R.string.previous_chapter));
                // TODO: Navigate to previous chapter
            });
        }

        // Next button
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                voiceGuidance.announceButtonPress(getString(R.string.next_chapter));
                // TODO: Navigate to next chapter
            });
        }

        // Rewind button
        if (rewindButton != null) {
            rewindButton.setOnClickListener(v -> {
                voiceGuidance.announceButtonPress(getString(R.string.rewind_10_seconds));
                // Rewind functionality would go here
            });
        }

        // Forward button
        if (forwardButton != null) {
            forwardButton.setOnClickListener(v -> {
                voiceGuidance.announceButtonPress(getString(R.string.forward_10_seconds));
                // Forward functionality would go here
            });
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            pauseAudio();
        } else {
            playAudio();
        }
    }

    private void playAudio() {
        if (!ttsManager.isReady()) {
            voiceGuidance.announceError(getString(R.string.error_audio_playback));
            Toast.makeText(this, "TTS initializing, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        isPlaying = true;
        playPauseButton.setImageResource(R.drawable.ic_pause);
        voiceGuidance.announce(getString(R.string.talkback_audio_playing), 
            VoiceGuidanceManager.AnnouncementType.CONFIRMATION);

        ttsManager.setSpeechRate(currentSpeed);
        // Small delay to let announcement finish
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            ttsManager.speak(lessonContent);
        }, 500);
    }

    private void pauseAudio() {
        isPlaying = false;
        playPauseButton.setImageResource(R.drawable.ic_play);
        ttsManager.stop();
        voiceGuidance.announce(getString(R.string.talkback_audio_paused), 
            VoiceGuidanceManager.AnnouncementType.CONFIRMATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsManager.getInstance(this)
                .startSession(LearningSession.ActivityType.AUDIO_LESSON, chapterName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAudio();
        AnalyticsManager.getInstance(this).endSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
    }
}
