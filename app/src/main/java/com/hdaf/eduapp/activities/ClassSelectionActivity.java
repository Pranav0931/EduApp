package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Class selection screen - grid of circular buttons for standards 1st to 9th.
 * Supports voice navigation for accessibility.
 */
public class ClassSelectionActivity extends AppCompatActivity {

    private String currentMode;
    private PreferenceManager prefManager;

    // Class buttons
    private Button btn1st, btn2nd, btn3rd, btn4th, btn5th, btn6th, btn7th, btn8th, btn9th;
    private FloatingActionButton fabAiChat;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        prefManager = PreferenceManager.getInstance(this);

        // Get mode from intent
        currentMode = getIntent().getStringExtra(Constants.EXTRA_MODE);
        if (currentMode == null) {
            currentMode = prefManager.getLastMode();
        }

        initializeViews();
        setupClickListeners();
        setupBottomNavigation();
        setupAiChat();

        // Announce screen
        announceForAccessibility(getString(R.string.class_selection_title));
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        fabAiChat = findViewById(R.id.fabAiChat);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btn1st = findViewById(R.id.btn1st);
        btn2nd = findViewById(R.id.btn2nd);
        btn3rd = findViewById(R.id.btn3rd);
        btn4th = findViewById(R.id.btn4th);
        btn5th = findViewById(R.id.btn5th);
        btn6th = findViewById(R.id.btn6th);
        btn7th = findViewById(R.id.btn7th);
        btn8th = findViewById(R.id.btn8th);
        btn9th = findViewById(R.id.btn9th);

        menuButton.setOnClickListener(v -> {
            // TODO: Open navigation drawer
            onBackPressed();
        });
    }

    private void setupClickListeners() {
        btn1st.setOnClickListener(v -> selectClass("class_1", "1st"));
        btn2nd.setOnClickListener(v -> selectClass("class_2", "2nd"));
        btn3rd.setOnClickListener(v -> selectClass("class_3", "3rd"));
        btn4th.setOnClickListener(v -> selectClass("class_4", "4th"));
        btn5th.setOnClickListener(v -> selectClass("class_5", "5th"));
        btn6th.setOnClickListener(v -> selectClass("class_6", "6th"));
        btn7th.setOnClickListener(v -> selectClass("class_7", "7th"));
        btn8th.setOnClickListener(v -> selectClass("class_8", "8th"));
        btn9th.setOnClickListener(v -> selectClass("class_9", "9th"));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_quiz) {
                startActivity(new Intent(this, QuizListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupAiChat() {
        fabAiChat.setOnClickListener(v -> {
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });
    }

    private void selectClass(String classId, String className) {
        prefManager.setLastClass(classId);

        // Announce selection for accessibility
        announceForAccessibility(getString(R.string.talkback_class_selected, className));

        Intent intent = new Intent(this, BooksActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, currentMode);
        intent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        intent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        startActivity(intent);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }
}
