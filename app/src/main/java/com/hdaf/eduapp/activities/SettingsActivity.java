package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Settings screen - app preferences and accessibility options.
 */
public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    private FloatingActionButton fabAiChat;
    private BottomNavigationView bottomNavigation;

    private Switch switchTalkback;
    private Switch switchHighContrast;
    private Switch switchLargeText;
    private Switch switchHapticFeedback;
    private Switch switchNotifications;
    private TextView tvCurrentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefManager = PreferenceManager.getInstance(this);

        initializeViews();
        setupBottomNavigation();
        setupFab();
        loadSettings();
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        fabAiChat = findViewById(R.id.fabAiChat);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        tvCurrentMode = findViewById(R.id.tvCurrentMode);
        switchTalkback = findViewById(R.id.switchTalkback);
        switchHighContrast = findViewById(R.id.switchHighContrast);
        switchLargeText = findViewById(R.id.switchLargeText);
        switchHapticFeedback = findViewById(R.id.switchHapticFeedback);
        switchNotifications = findViewById(R.id.switchNotifications);

        menuButton.setOnClickListener(v -> onBackPressed());

        // Mode selection
        findViewById(R.id.layoutChangeMode).setOnClickListener(v -> showModeSelectionDialog());
        
        // About
        findViewById(R.id.layoutAbout).setOnClickListener(v -> {
            Toast.makeText(this, "EduApp v1.0\nAn accessibility-first learning app", Toast.LENGTH_LONG).show();
        });

        // Privacy Policy
        findViewById(R.id.layoutPrivacy).setOnClickListener(v -> {
            Toast.makeText(this, "Privacy Policy - Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(R.string.logout, (d, w) -> {
                    prefManager.clearAll();
                    Intent intent = new Intent(this, ModeSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, ClassSelectionActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_quiz) {
                startActivity(new Intent(this, QuizListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAiChat.setOnClickListener(v -> {
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });
    }

    private void loadSettings() {
        String mode = prefManager.getLastMode();
        tvCurrentMode.setText(getModeDisplayName(mode));

        switchTalkback.setChecked(prefManager.getBoolean("talkback_enabled", false));
        switchHighContrast.setChecked(prefManager.getBoolean("high_contrast", false));
        switchLargeText.setChecked(prefManager.getBoolean("large_text", false));
        switchHapticFeedback.setChecked(prefManager.getBoolean("haptic_feedback", true));
        switchNotifications.setChecked(prefManager.getBoolean("notifications_enabled", true));

        // Setup listeners
        switchTalkback.setOnCheckedChangeListener((b, checked) -> 
            prefManager.setBoolean("talkback_enabled", checked));
        switchHighContrast.setOnCheckedChangeListener((b, checked) -> 
            prefManager.setBoolean("high_contrast", checked));
        switchLargeText.setOnCheckedChangeListener((b, checked) -> 
            prefManager.setBoolean("large_text", checked));
        switchHapticFeedback.setOnCheckedChangeListener((b, checked) -> 
            prefManager.setBoolean("haptic_feedback", checked));
        switchNotifications.setOnCheckedChangeListener((b, checked) -> 
            prefManager.setBoolean("notifications_enabled", checked));
    }

    private void showModeSelectionDialog() {
        String[] modes = {"Regular Student", "Blind Student", "Deaf Student", "Low Vision", "Slow Learner"};
        new AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_mode)
            .setItems(modes, (dialog, which) -> {
                String selectedMode;
                switch (which) {
                    case 1: selectedMode = "BLIND"; break;
                    case 2: selectedMode = "DEAF"; break;
                    case 3: selectedMode = "LOW_VISION"; break;
                    case 4: selectedMode = "SLOW_LEARNER"; break;
                    default: selectedMode = "NORMAL"; break;
                }
                prefManager.setLastMode(selectedMode);
                tvCurrentMode.setText(getModeDisplayName(selectedMode));
                Toast.makeText(this, "Mode changed to " + modes[which], Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private String getModeDisplayName(String mode) {
        if (mode == null) return "Regular Student";
        switch (mode) {
            case "BLIND": return "Blind Student";
            case "DEAF": return "Deaf Student";
            case "LOW_VISION": return "Low Vision";
            case "SLOW_LEARNER": return "Slow Learner";
            default: return "Regular Student";
        }
    }
}
