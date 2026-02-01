package com.hdaf.eduapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.LocaleHelper;
import com.hdaf.eduapp.utils.PreferenceManager;

/**
 * Class selection screen - modern card grid for standards 1st to 9th.
 * Supports voice navigation for accessibility.
 */
public class ClassSelectionActivity extends AppCompatActivity {

    private String currentMode;
    private PreferenceManager prefManager;

    // Class cards
    private MaterialCardView card1st, card2nd, card3rd, card4th, card5th, card6th, card7th, card8th, card9th;
    private ExtendedFloatingActionButton fabAiChat;
    private BottomNavigationView bottomNavigation;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.INSTANCE.applyLocale(newBase));
    }

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
        fabAiChat = findViewById(R.id.fabAiChat);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        card1st = findViewById(R.id.card1st);
        card2nd = findViewById(R.id.card2nd);
        card3rd = findViewById(R.id.card3rd);
        card4th = findViewById(R.id.card4th);
        card5th = findViewById(R.id.card5th);
        card6th = findViewById(R.id.card6th);
        card7th = findViewById(R.id.card7th);
        card8th = findViewById(R.id.card8th);
        card9th = findViewById(R.id.card9th);
    }

    private void setupClickListeners() {
        card1st.setOnClickListener(v -> selectClass("class_1", "1st"));
        card2nd.setOnClickListener(v -> selectClass("class_2", "2nd"));
        card3rd.setOnClickListener(v -> selectClass("class_3", "3rd"));
        card4th.setOnClickListener(v -> selectClass("class_4", "4th"));
        card5th.setOnClickListener(v -> selectClass("class_5", "5th"));
        card6th.setOnClickListener(v -> selectClass("class_6", "6th"));
        card7th.setOnClickListener(v -> selectClass("class_7", "7th"));
        card8th.setOnClickListener(v -> selectClass("class_8", "8th"));
        card9th.setOnClickListener(v -> selectClass("class_9", "9th"));
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
