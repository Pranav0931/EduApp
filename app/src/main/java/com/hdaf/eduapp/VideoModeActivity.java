package com.hdaf.eduapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VideoModeActivity extends AppCompatActivity {
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_mode);

        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // Set your video URI dynamically (replace with actual source)
        Uri videoUri = getVideoUri();

        if (videoUri != null) {
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> videoView.start());
        } else {
            Toast.makeText(this, "Video source not found!", Toast.LENGTH_SHORT).show();
        }

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Error playing video!", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private Uri getVideoUri() {
        // Change this method to dynamically set the video source.
        // Example 1: From res/raw (Ensure you have a video in res/raw/)
        // return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.your_video_file);

        // Example 2: From assets (Ensure the file exists in assets folder)
        // return Uri.parse("file:///android_asset/your_video_file.mp4");

        // Example 3: From an online URL
        // return Uri.parse("https://www.example.com/your_video.mp4");

        return null; // Change this to return the correct Uri
    }
}
