package com.hdaf.eduapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button audioModeButton = findViewById(R.id.audioModeButton);
        audioModeButton.setEnabled(true); // Enable when ready

        Button videoModeButton = findViewById(R.id.videoModeButton);

        // Launch Audio Mode (for blind users)
        audioModeButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, AudioModeActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Audio Mode Activity Not Found!", Toast.LENGTH_SHORT).show();
            }
        });

        // Launch Video Mode (for deaf users)
        videoModeButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, VideoModeActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Video Mode Activity Not Found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
