package com.hdaf.eduapp.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.accessibility.VoiceGuidanceManager;
import com.hdaf.eduapp.adapters.BadgeAdapter;
import com.hdaf.eduapp.gamification.Badge;
import com.hdaf.eduapp.gamification.BadgeRegistry;
import com.hdaf.eduapp.gamification.DailyChallenge;
import com.hdaf.eduapp.gamification.GamificationManager;
import com.hdaf.eduapp.gamification.LevelSystem;
import com.hdaf.eduapp.gamification.UserProgress;

import java.util.List;

/**
 * Profile activity showing user's gamification progress.
 * Displays XP, level, streaks, badges, and daily challenge.
 * Full TalkBack voice guidance support for blind users.
 */
public class ProfileActivity extends AppCompatActivity implements BadgeAdapter.OnBadgeClickListener {

    private TextView txtLevel;
    private TextView txtLevelTitle;
    private TextView txtXP;
    private TextView txtXPNext;
    private ProgressBar progressXP;
    private TextView txtStreak;
    private TextView txtQuizzes;
    private TextView txtBadgesCount;
    private MaterialCardView cardDailyChallenge;
    private TextView txtChallengeXP;
    private TextView txtChallengeDescription;
    private TextView txtChallengeStatus;
    private RecyclerView recyclerBadges;

    private GamificationManager gamificationManager;
    private BadgeAdapter badgeAdapter;
    private TTSManager ttsManager;
    private VoiceGuidanceManager voiceGuidance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gamificationManager = GamificationManager.getInstance(this);
        ttsManager = TTSManager.getInstance();
        voiceGuidance = VoiceGuidanceManager.getInstance(this);

        initializeViews();
        setupBadgesRecycler();
        setupAccessibility();
        loadUserData();
        loadDailyChallenge();

        // Announce screen for accessibility
        announceForAccessibility(getString(R.string.profile_accessibility_intro));
    }

    private void initializeViews() {
        txtLevel = findViewById(R.id.txt_level);
        txtLevelTitle = findViewById(R.id.txt_level_title);
        txtXP = findViewById(R.id.txt_xp);
        txtXPNext = findViewById(R.id.txt_xp_next);
        progressXP = findViewById(R.id.progress_xp);
        txtStreak = findViewById(R.id.txt_streak);
        txtQuizzes = findViewById(R.id.txt_quizzes);
        txtBadgesCount = findViewById(R.id.txt_badges_count);
        cardDailyChallenge = findViewById(R.id.card_daily_challenge);
        txtChallengeXP = findViewById(R.id.txt_challenge_xp);
        txtChallengeDescription = findViewById(R.id.txt_challenge_description);
        txtChallengeStatus = findViewById(R.id.txt_challenge_status);
        recyclerBadges = findViewById(R.id.recycler_badges);
    }
    
    private void setupAccessibility() {
        // Make card_daily_challenge speak its full content
        if (cardDailyChallenge != null) {
            cardDailyChallenge.setOnClickListener(v -> {
                DailyChallenge challenge = gamificationManager.getDailyChallenge();
                if (challenge != null) {
                    String status = challenge.isCompleted() ? "Completed!" : "Not completed yet.";
                    String announcement = "Daily Challenge. " + challenge.getDisplayDescription() + 
                        ". Reward: " + challenge.getXpReward() + " XP. Status: " + status;
                    voiceGuidance.announce(announcement, VoiceGuidanceManager.AnnouncementType.INFORMATION);
                    voiceGuidance.vibrate(VoiceGuidanceManager.HapticPattern.LIGHT);
                }
            });
        }
    }

    private void setupBadgesRecycler() {
        List<GamificationManager.BadgeStatus> badges = gamificationManager.getAllBadgesWithStatus();
        badgeAdapter = new BadgeAdapter(this, badges);
        badgeAdapter.setOnBadgeClickListener(this);
        
        recyclerBadges.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBadges.setAdapter(badgeAdapter);
    }

    private void loadUserData() {
        UserProgress progress = gamificationManager.getUserProgress();

        // Level display
        txtLevel.setText(LevelSystem.getFormattedLevel(progress.getCurrentLevel()));
        txtLevelTitle.setText(LevelSystem.getLevelTitleEnglish(progress.getCurrentLevel()));

        // XP display
        txtXP.setText(progress.getTotalXP() + " XP");
        int xpToNext = progress.getXPToNextLevel();
        txtXPNext.setText(xpToNext + " XP to next level");

        // XP progress bar
        int progressPercent = LevelSystem.getProgressPercentage(progress.getTotalXP());
        progressXP.setProgress(progressPercent);

        // Stats
        txtStreak.setText(String.valueOf(progress.getCurrentStreak()));
        txtQuizzes.setText(String.valueOf(progress.getQuizzesCompleted()));

        // Badges count
        int earnedCount = progress.getEarnedBadgeIds().size();
        int totalBadges = BadgeRegistry.getInstance().getBadgeCount();
        txtBadgesCount.setText(earnedCount + "/" + totalBadges);

        // Update badges adapter
        badgeAdapter.updateBadges(gamificationManager.getAllBadgesWithStatus());
    }

    private void loadDailyChallenge() {
        DailyChallenge challenge = gamificationManager.getDailyChallenge();
        
        if (challenge != null) {
            txtChallengeDescription.setText(challenge.getDisplayDescription());
            txtChallengeXP.setText("+" + challenge.getXpReward() + " XP");
            
            if (challenge.isCompleted()) {
                txtChallengeStatus.setVisibility(android.view.View.VISIBLE);
                txtChallengeStatus.setText("✅ " + getString(R.string.challenge_completed));
                cardDailyChallenge.setAlpha(0.7f);
            } else {
                txtChallengeStatus.setVisibility(android.view.View.GONE);
                cardDailyChallenge.setAlpha(1.0f);
            }
        }
    }

    @Override
    public void onBadgeClick(Badge badge, boolean isEarned) {
        // Show badge details dialog
        showBadgeDialog(badge, isEarned);
    }

    private void showBadgeDialog(Badge badge, boolean isEarned) {
        String title = badge.getName();
        String message;
        
        if (isEarned) {
            message = badge.getDescription() + "\n\n🎉 " + getString(R.string.badge_earned_message);
        } else {
            message = badge.getDescription() + "\n\n🔒 " + badge.getRequirement() + 
                      "\n\n" + getString(R.string.badge_reward_prefix) + " +" + badge.getXpReward() + " XP";
        }

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show();

        // Use VoiceGuidanceManager for TalkBack
        String voiceAnnouncement = isEarned ? 
            badge.getName() + ". Badge earned! " + badge.getDescription() :
            badge.getName() + ". Badge locked. " + badge.getRequirement() + ". Reward: " + badge.getXpReward() + " XP.";
        voiceGuidance.announce(voiceAnnouncement, VoiceGuidanceManager.AnnouncementType.INFORMATION);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
        // Also use voice guidance for additional support
        voiceGuidance.announceDelayed(message, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadUserData();
        loadDailyChallenge();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceGuidance != null) {
            voiceGuidance.stop();
        }
    }
}
