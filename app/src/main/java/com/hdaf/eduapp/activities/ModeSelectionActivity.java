package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.ai.EduAIService;
import com.hdaf.eduapp.gamification.GamificationManager;
import com.hdaf.eduapp.parent.ParentDashboardActivity;
import com.hdaf.eduapp.parent.ParentLoginDialog;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Mode selection screen - choose between Audio Mode (blind) and Video Mode (deaf).
 * Optimized for TalkBack accessibility.
 * Includes EduAI FAB for AI assistant and Profile FAB for gamification.
 */
public class ModeSelectionActivity extends AppCompatActivity {

    private Button audioModeButton;
    private Button videoModeButton;
    private ImageButton parentZoneButton;
    private FloatingActionButton eduaiFab;
    private FloatingActionButton profileFab;
    private PreferenceManager prefManager;
    private TTSManager ttsManager;
    private GamificationManager gamificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        prefManager = PreferenceManager.getInstance(this);
        ttsManager = TTSManager.getInstance();
        gamificationManager = GamificationManager.getInstance(this);

        // Initialize TTS
        if (!ttsManager.isReady()) {
            ttsManager.initialize(this);
        }

        // Initialize EduAI (Gemini)
        EduAIService.getInstance().initialize(
                this,
                "AIzaSyBrB5PlfjdNm4fwMBmDWSt8oAkc0fEWb4Y"
        );

        initializeViews();
        setupClickListeners();

        // Accessibility announcement
        announceForAccessibility(getString(R.string.talkback_mode_selection));
    }

    private void initializeViews() {
        audioModeButton = findViewById(R.id.audioModeButton);
        videoModeButton = findViewById(R.id.videoModeButton);
        parentZoneButton = findViewById(R.id.btn_parent_zone);
        eduaiFab = findViewById(R.id.eduaiFab);
        profileFab = findViewById(R.id.profileFab);
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

        // EduAI FAB
        eduaiFab.setOnClickListener(v -> openEduAIChat());

        // Profile FAB
        profileFab.setOnClickListener(v -> openProfile());

        // Parent Zone
        if (parentZoneButton != null) {
            parentZoneButton.setOnClickListener(this::onParentZoneClicked);
        }
    }

    public void onParentZoneClicked(View v) {
        ParentLoginDialog dialog = new ParentLoginDialog();
        dialog.setOnSuccessListener(() -> {
            Intent intent = new Intent(this, ParentDashboardActivity.class);
            startActivity(intent);
        });
        dialog.show(getSupportFragmentManager(), "ParentLoginDialog");
    }

    private void navigateToClassSelection(String mode) {
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, mode);
        startActivity(intent);
    }

    /**
     * Opens EduAI Chat Bottom Sheet
     */
    private void openEduAIChat() {
        EduAIChatBottomSheet sheet = EduAIChatBottomSheet.newInstance();
        sheet.show(getSupportFragmentManager(), "EduAIChatBottomSheet");
    }

    /**
     * Opens Profile Activity
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamificationManager.updateStreak();
    }
}
