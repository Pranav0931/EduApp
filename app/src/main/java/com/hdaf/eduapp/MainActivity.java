package com.hdaf.eduapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button audioModeButton = findViewById(R.id.audioModeButton);
        Button videoModeButton = findViewById(R.id.videoModeButton);

        // Launch Audio Mode (for blind users)
        audioModeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AudioModeActivity.class);
            startActivity(intent);
        });

        // Launch Video Mode (for deaf users)
        videoModeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoModeActivity.class);
            startActivity(intent);
        });
    }
}
