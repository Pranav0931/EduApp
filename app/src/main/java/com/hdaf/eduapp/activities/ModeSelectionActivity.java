package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.ai.EduAIService;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Mode selection screen - choose between Audio Mode (blind) and Video Mode
 * (deaf).
 * Optimized for TalkBack accessibility.
 * Now includes EduAI FAB for AI assistant access.
 */
public class ModeSelectionActivity extends AppCompatActivity {

    private Button audioModeButton;
    private Button videoModeButton;
    private FloatingActionButton eduaiFab;
    private PreferenceManager prefManager;
    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        prefManager = PreferenceManager.getInstance(this);
        ttsManager = TTSManager.getInstance();
        
        // Initialize TTS if not already done
        if (!ttsManager.isReady()) {
            ttsManager.initialize(this);
        }
        
        // Initialize EduAI with Gemini API key
        EduAIService.getInstance().initialize(this, "AIzaSyBrB5PlfjdNm4fwMBmDWSt8oAkc0fEWb4Y");

        initializeViews();
        setupClickListeners();

        // Announce screen for TalkBack
        announceForAccessibility(getString(R.string.talkback_mode_selection));
    }

    private void initializeViews() {
        audioModeButton = findViewById(R.id.audioModeButton);
        videoModeButton = findViewById(R.id.videoModeButton);
        eduaiFab = findViewById(R.id.eduaiFab);
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
        
        // EduAI FAB click - open chat bottom sheet
        eduaiFab.setOnClickListener(v -> openEduAIChat());
    }

    private void navigateToClassSelection(String mode) {
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, mode);
        startActivity(intent);
    }
    
    /**
     * Opens the EduAI chat bottom sheet.
     */
    private void openEduAIChat() {
        EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
        chatSheet.show(getSupportFragmentManager(), "EduAIChatBottomSheet");
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

