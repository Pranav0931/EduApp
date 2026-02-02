package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
 * Optimized for TalkBack accessibility with voice guidance.
 * Includes EduAI FAB for AI assistant.
 */
public class ModeSelectionActivity extends AppCompatActivity {

    private View audioModeButton; // LinearLayout inside card
    private View videoModeButton; // LinearLayout inside card
    private MaterialCardView cardAudioMode;
    private MaterialCardView cardVideoMode;
    private MaterialButton btnBothModes;
    private MaterialButton btnStandard;
    private ImageButton parentZoneButton;
    private ImageButton btnSpeak;
    private FloatingActionButton eduaiFab;
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
        setupAccessibility();

        // Accessibility announcement with voice guidance
        announceForAccessibility(getString(R.string.talkback_mode_selection));
        speakWelcome();
    }

    private void initializeViews() {
        cardAudioMode = findViewById(R.id.cardAudioMode);
        cardVideoMode = findViewById(R.id.cardVideoMode);
        audioModeButton = findViewById(R.id.audioModeButton);
        videoModeButton = findViewById(R.id.videoModeButton);
        btnBothModes = findViewById(R.id.btnBothModes);
        btnStandard = findViewById(R.id.btnStandard);
        parentZoneButton = findViewById(R.id.btn_parent_zone);
        btnSpeak = findViewById(R.id.btnSpeak);
        eduaiFab = findViewById(R.id.eduaiFab);
    }

    private void setupClickListeners() {
        // Audio mode card click
        if (audioModeButton != null) {
            audioModeButton.setOnClickListener(v -> {
                ttsManager.speak(getString(R.string.audio_mode_title) + " " + getString(R.string.for_blind_users));
                prefManager.setLastMode(Constants.MODE_AUDIO);
                navigateToClassSelection(Constants.MODE_AUDIO);
            });
        }

        // Video mode card click
        if (videoModeButton != null) {
            videoModeButton.setOnClickListener(v -> {
                ttsManager.speak(getString(R.string.video_mode_title) + " " + getString(R.string.for_deaf_users));
                prefManager.setLastMode(Constants.MODE_VIDEO);
                navigateToClassSelection(Constants.MODE_VIDEO);
            });
        }

        // Both modes button
        if (btnBothModes != null) {
            btnBothModes.setOnClickListener(v -> {
                ttsManager.speak(getString(R.string.both_modes_desc));
                prefManager.setLastMode(Constants.MODE_BOTH);
                navigateToClassSelection(Constants.MODE_BOTH);
            });
        }

        // Standard mode button
        if (btnStandard != null) {
            btnStandard.setOnClickListener(v -> {
                ttsManager.speak(getString(R.string.standard_mode_desc));
                prefManager.setLastMode(Constants.MODE_STANDARD);
                navigateToClassSelection(Constants.MODE_STANDARD);
            });
        }

        // Speak button - reads all options aloud
        if (btnSpeak != null) {
            btnSpeak.setOnClickListener(v -> speakAllOptions());
        }

        // EduAI FAB
        eduaiFab.setOnClickListener(v -> openEduAIChat());

        // Parent Zone
        if (parentZoneButton != null) {
            parentZoneButton.setOnClickListener(this::onParentZoneClicked);
        }
    }

    private void setupAccessibility() {
        // Set up focus change listeners for TalkBack voice feedback
        if (audioModeButton != null) {
            audioModeButton.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    ttsManager.speak(getString(R.string.audio_mode_full_desc));
                }
            });
        }

        if (videoModeButton != null) {
            videoModeButton.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    ttsManager.speak(getString(R.string.video_mode_full_desc));
                }
            });
        }
    }

    /**
     * Speaks welcome message when screen opens
     */
    private void speakWelcome() {
        String welcome = getString(R.string.how_do_you_learn).replace("\n", " ");
        ttsManager.speak(welcome);
    }

    /**
     * Speaks all available options for blind users
     */
    private void speakAllOptions() {
        String options = getString(R.string.how_do_you_learn).replace("\n", " ") + ". " +
                getString(R.string.audio_mode_full_desc) + " " +
                getString(R.string.video_mode_full_desc) + " " +
                getString(R.string.both_modes_desc) + " " +
                getString(R.string.standard_mode_desc);
        ttsManager.speak(options);
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

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamificationManager.updateStreak();
    }
}
