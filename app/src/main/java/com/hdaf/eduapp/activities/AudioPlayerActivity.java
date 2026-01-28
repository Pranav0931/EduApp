package com.hdaf.eduapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.analytics.AnalyticsManager;
import com.hdaf.eduapp.analytics.LearningSession;
import com.hdaf.eduapp.utils.Constants;

/**
 * Audio Player screen for blind users.
 * Uses Text-to-Speech to read lesson content aloud.
 */
public class AudioPlayerActivity extends AppCompatActivity {

    private TextView headerTitle;
    private TextView chapterTitle;
    private SeekBar speedSeekBar;
    private ImageButton playPauseButton;
    private ImageButton backButton;

    private TTSManager ttsManager;
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

        lessonContent = getIntent().getStringExtra(Constants.EXTRA_LESSON_CONTENT);
        chapterName = getIntent().getStringExtra(Constants.EXTRA_CHAPTER_NAME);

        if (lessonContent == null || lessonContent.isEmpty()) {
            lessonContent = "Welcome! Audio lesson content is not available yet.";
        }

        initializeViews();
        setupControls();
    }

    private void initializeViews() {
        headerTitle = findViewById(R.id.headerTitle);
        chapterTitle = findViewById(R.id.unitBadge);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        backButton = findViewById(R.id.menuButton);

        headerTitle.setText("Audio Lesson");
        chapterTitle.setText(chapterName != null ? chapterName : "Lesson");

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupControls() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        speedSeekBar.setMax(150);
        speedSeekBar.setProgress(50);

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSpeed = 0.5f + (progress / 100f);
                ttsManager.setSpeechRate(currentSpeed);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
            Toast.makeText(this, "TTS initializing, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        isPlaying = true;
        playPauseButton.setImageResource(R.drawable.ic_pause);

        ttsManager.setSpeechRate(currentSpeed);
        ttsManager.speak(lessonContent);
    }

    private void pauseAudio() {
        isPlaying = false;
        playPauseButton.setImageResource(R.drawable.ic_play);
        ttsManager.stop();
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
