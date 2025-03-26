package com.hdaf.eduapp;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class VideoModeActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_mode);

        videoView = findViewById(R.id.videoView);
        playButton = findViewById(R.id.playButton);

        // Set video path from the raw folder
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_video);
        videoView.setVideoURI(videoUri);


        playButton.setOnClickListener(v -> {
            if (!videoView.isPlaying()) {
                videoView.start();
            }
        });

        // Restart video when completed
        videoView.setOnCompletionListener(MediaPlayer::start);
    }
}
