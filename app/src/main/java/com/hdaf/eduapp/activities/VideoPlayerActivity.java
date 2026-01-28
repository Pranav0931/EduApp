package com.hdaf.eduapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.analytics.AnalyticsManager;
import com.hdaf.eduapp.analytics.LearningSession;
import com.hdaf.eduapp.utils.Constants;

/**
 * Video Player screen for deaf users.
 * Plays ISL (Indian Sign Language) video lessons with captions.
 */
public class VideoPlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar loadingProgress;
    private View captionContainer;
    private TextView captionText;
    private ImageButton backButton;

    private String videoUrl;
    private String chapterName;
    private String transcript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra(Constants.EXTRA_VIDEO_URL);
        chapterName = getIntent().getStringExtra(Constants.EXTRA_CHAPTER_NAME);
        transcript = getIntent().getStringExtra(Constants.EXTRA_LESSON_CONTENT);

        if (videoUrl == null || videoUrl.isEmpty()) {
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        }

        initializeViews();
        initializePlayer();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        captionContainer = findViewById(R.id.captionContainer);
        captionText = findViewById(R.id.captionText);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> onBackPressed());

        if (transcript != null && !transcript.isEmpty()) {
            captionText.setText(transcript);
        }

        setupGestureControls();
    }

    private void initializePlayer() {
        loadingProgress.setVisibility(View.VISIBLE);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    loadingProgress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    loadingProgress.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_ENDED) {
                    announceForAccessibility("Video finished");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                loadingProgress.setVisibility(View.GONE);
                announceForAccessibility(getString(R.string.error_video_playback));
            }
        });

        player.prepare();
        player.play();
    }

    private void setupGestureControls() {
        playerView.setOnClickListener(v -> {
            if (playerView.isControllerFullyVisible()) {
                playerView.hideController();
                captionContainer.setVisibility(View.GONE);
            } else {
                playerView.showController();
                if (transcript != null && !transcript.isEmpty()) {
                    captionContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setPlaybackSpeed(float speed) {
        if (player != null) {
            player.setPlaybackSpeed(speed);
        }
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
        AnalyticsManager.getInstance(this).endSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
        AnalyticsManager.getInstance(this)
                .startSession(LearningSession.ActivityType.VIDEO_LESSON, chapterName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
