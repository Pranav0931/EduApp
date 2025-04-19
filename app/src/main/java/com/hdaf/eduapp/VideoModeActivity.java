package com.hdaf.eduapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class VideoModeActivity extends AppCompatActivity {

    private VideoView videoView;
    private WebView webView;
    private Button playLocalButton, playYouTubeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_mode);

        // Initialize UI elements
        videoView = findViewById(R.id.videoView);
        webView = findViewById(R.id.videoWebView);
        playLocalButton = findViewById(R.id.playLocalButton);
        playYouTubeButton = findViewById(R.id.playYouTubeButton);

        // Set up Media Controller for video playback
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Play Local Video Button
        playLocalButton.setOnClickListener(v -> playLocalVideo());

        // Play YouTube Video Button (Updated with the new YouTube video ID)
        playYouTubeButton.setOnClickListener(v -> playYouTubeVideo("qcdivQfA41Y"));
    }

    // Play Local Video
    private void playLocalVideo() {
        webView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        // Set video path from res/raw folder
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_video);
        videoView.setVideoURI(videoUri);
        videoView.start();
    }

    // Play YouTube Video
    private void playYouTubeVideo(String videoId) {
        videoView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String videoUrl = "<html><body style='margin:0;padding:0;'>" +
                "<iframe width='100%' height='100%' src='https://www.youtube.com/embed/" + videoId +
                "?autoplay=1' frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>" +
                "</body></html>";

        webView.loadData(videoUrl, "text/html", "utf-8");
    }
}
