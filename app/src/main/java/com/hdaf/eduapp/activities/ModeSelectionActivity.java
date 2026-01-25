package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Mode selection screen - choose between Audio Mode (blind) and Video Mode
 * (deaf).
 * Optimized for TalkBack accessibility.
 */
public class ModeSelectionActivity extends AppCompatActivity {

    private Button audioModeButton;
    private Button videoModeButton;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        prefManager = PreferenceManager.getInstance(this);

        initializeViews();
        setupClickListeners();

        // Announce screen for TalkBack
        announceForAccessibility(getString(R.string.talkback_mode_selection));
    }

    private void initializeViews() {
        audioModeButton = findViewById(R.id.audioModeButton);
        videoModeButton = findViewById(R.id.videoModeButton);
    }

    private void setupClickListeners() {
        audioModeButton.setOnClickListener(v -> {
            prefManager.setLastMode(Constants.MODE_AUDIO);
            navigateToClassSelection(Constants.MODE_AUDIO);
        });

        videoModeButton.setOnClickListener(v -> {
            prefManager.setLastMode(Constants.MODE_VIDEO);
            navigateToClassSelection(Constants.MODE_VIDEO);
        });
    }

    private void navigateToClassSelection(String mode) {
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-announce when returning to this screen
    }
}
