package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;

/**
 * Activity to display quiz results with score, stats, and motivational feedback.
 */
public class QuizResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_CORRECT = "extra_correct";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_PASSED = "extra_passed";

    private ImageView imgResultIcon;
    private TextView txtScoreValue, txtPerformance, txtMessage;
    private TextView txtCorrect, txtIncorrect, txtTime, txtXp;
    private MaterialButton btnRetry, btnHome;

    private TTSManager ttsManager;
    private int score, correct, total;
    private String time, message;
    private boolean passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initViews();
        loadData();
        displayResults();
        setupClickListeners();
        speakResults();
    }

    private void initViews() {
        imgResultIcon = findViewById(R.id.img_result_icon);
        txtScoreValue = findViewById(R.id.txt_score_value);
        txtPerformance = findViewById(R.id.txt_performance);
        txtMessage = findViewById(R.id.txt_message);
        txtCorrect = findViewById(R.id.txt_correct);
        txtIncorrect = findViewById(R.id.txt_incorrect);
        txtTime = findViewById(R.id.txt_time);
        txtXp = findViewById(R.id.txt_xp);
        btnRetry = findViewById(R.id.btn_retry);
        btnHome = findViewById(R.id.btn_home);
        
        ttsManager = TTSManager.getInstance();
    }

    private void loadData() {
        Intent intent = getIntent();
        score = intent.getIntExtra(EXTRA_SCORE, 0);
        correct = intent.getIntExtra(EXTRA_CORRECT, 0);
        total = intent.getIntExtra(EXTRA_TOTAL, 0);
        time = intent.getStringExtra(EXTRA_TIME);
        message = intent.getStringExtra(EXTRA_MESSAGE);
        passed = intent.getBooleanExtra(EXTRA_PASSED, false);
        
        if (time == null) time = "0:00";
        if (message == null) message = "Great effort!";
    }

    private void displayResults() {
        // Score
        txtScoreValue.setText(score + "%");
        
        // Performance text
        String performance = getPerformanceText();
        txtPerformance.setText(performance);
        txtMessage.setText(message);
        
        // Stats
        txtCorrect.setText(String.valueOf(correct));
        txtIncorrect.setText(String.valueOf(total - correct));
        txtTime.setText(time);
        
        // Gamification Results
        com.hdaf.eduapp.gamification.AwardResult awardResult = 
            (com.hdaf.eduapp.gamification.AwardResult) getIntent().getSerializableExtra("extra_award_result");
            
        if (awardResult != null) {
            handleGamificationResult(awardResult);
        } else {
            // Fallback
            int xp = calculateXP();
            txtXp.setText("+" + xp + " XP Earned!");
        }
        
        // Icon based on performance
        if (score >= 90) {
            imgResultIcon.setImageResource(R.drawable.ic_trophy_gold);
        } else if (score >= 70) {
            imgResultIcon.setImageResource(R.drawable.ic_trophy);
        } else if (score >= 50) {
            imgResultIcon.setImageResource(R.drawable.ic_medal);
        } else {
            imgResultIcon.setImageResource(R.drawable.ic_try_again);
        }
    }
    
    private void handleGamificationResult(com.hdaf.eduapp.gamification.AwardResult result) {
        txtXp.setText("+" + result.getXpAwarded() + " XP Earned!");
        
        if (result.isLeveledUp()) {
            android.widget.Toast.makeText(this, result.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            ttsManager.speak(result.getMessage());
        }
        
        if (result.getNewBadgeIds() != null && !result.getNewBadgeIds().isEmpty()) {
            String badgeMsg = "You earned " + result.getNewBadgeIds().size() + " new badge(s)!";
            android.widget.Toast.makeText(this, badgeMsg, android.widget.Toast.LENGTH_SHORT).show();
            if (!result.isLeveledUp()) { // Avoid talking over level up
                 ttsManager.speak(badgeMsg);
            }
        }
    }

    private String getPerformanceText() {
        if (score >= 90) return "Excellent! 🎉";
        if (score >= 75) return "Great Job! 👏";
        if (score >= 60) return "Good Effort! 💪";
        return "Keep Trying! 😊";
    }

    private int calculateXP() {
        // Base XP for completing quiz
        int xp = 10;
        // Bonus for each correct answer
        xp += correct * 5;
        // Bonus for high scores
        if (score >= 90) xp += 20;
        else if (score >= 70) xp += 10;
        return xp;
    }

    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> {
            finish();
        });
        
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModeSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void speakResults() {
        // Delayed slightly to allow screen to load
        new android.os.Handler().postDelayed(() -> {
            StringBuilder speech = new StringBuilder();
            speech.append("Quiz Complete! ");
            speech.append("You scored ").append(score).append(" percent. ");
            
            // Gamification speech is handled in handleGamificationResult
            if (!getIntent().hasExtra("extra_award_result")) {
                speech.append(message);
                ttsManager.speak(speech.toString());
            } else {
                 // Speak basic results, gamification part will speak additionally
                 ttsManager.speak(speech.toString());
            }
        }, 500);
    }

    @Override
    public void onBackPressed() {
        // Go to home instead of back
        btnHome.performClick();
    }
}
