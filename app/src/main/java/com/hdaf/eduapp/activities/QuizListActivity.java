package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.VoiceGuidanceManager;
import com.hdaf.eduapp.adapters.QuizListAdapter;
import com.hdaf.eduapp.models.QuizModel;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz list screen - displays available quizzes.
 * Supports AI quiz generation and full TalkBack accessibility.
 */
public class QuizListActivity extends AppCompatActivity implements QuizListAdapter.OnQuizClickListener {

    private RecyclerView quizRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;
    private FloatingActionButton fabAiChat;
    private FloatingActionButton fabCreateQuiz;
    private BottomNavigationView bottomNavigation;
    private ImageButton btnSpeaker;

    private PreferenceManager prefManager;
    private QuizListAdapter adapter;
    private VoiceGuidanceManager voiceGuidance;
    private List<QuizModel> currentQuizzes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        prefManager = PreferenceManager.getInstance(this);
        voiceGuidance = VoiceGuidanceManager.getInstance(this);

        initializeViews();
        setupBottomNavigation();
        setupFabs();
        setupAccessibility();
        loadQuizzes();
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        TextView headerTitle = findViewById(R.id.headerTitle);
        quizRecyclerView = findViewById(R.id.quizRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyText = findViewById(R.id.emptyText);
        fabAiChat = findViewById(R.id.fabAiChat);
        fabCreateQuiz = findViewById(R.id.fabCreateQuiz);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnSpeaker = findViewById(R.id.btnSpeaker);

        headerTitle.setText(R.string.nav_quizzes);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuButton.setOnClickListener(v -> {
            voiceGuidance.announce("Going back", VoiceGuidanceManager.AnnouncementType.NAVIGATION);
            onBackPressed();
        });
    }

    private void setupAccessibility() {
        // Speaker button to read all quizzes
        if (btnSpeaker != null) {
            btnSpeaker.setOnClickListener(v -> readAllQuizzes());
        }
        
        // Welcome announcement when screen opens
        voiceGuidance.announceDelayed("Quiz list. " + currentQuizzes.size() + " quizzes available. Tap speaker to hear all options.", 500);
    }
    
    private void readAllQuizzes() {
        if (currentQuizzes.isEmpty()) {
            voiceGuidance.announce("No quizzes available. Tap the AI Quiz button to generate a new quiz.", 
                VoiceGuidanceManager.AnnouncementType.INFORMATION);
            return;
        }
        
        StringBuilder announcement = new StringBuilder();
        announcement.append(currentQuizzes.size()).append(" quizzes available. ");
        
        for (int i = 0; i < currentQuizzes.size(); i++) {
            QuizModel quiz = currentQuizzes.get(i);
            announcement.append("Quiz ").append(i + 1).append(": ")
                .append(quiz.getTitle()).append(". ")
                .append(quiz.getQuestionCount()).append(" questions. ")
                .append(quiz.getDurationMinutes()).append(" minutes. ");
        }
        
        announcement.append("Double tap any quiz to start.");
        voiceGuidance.announce(announcement.toString(), VoiceGuidanceManager.AnnouncementType.INFORMATION);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_quiz);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, ClassSelectionActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_quiz) {
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

    private void setupFabs() {
        fabAiChat.setOnClickListener(v -> {
            voiceGuidance.announce("Opening AI chat assistant", VoiceGuidanceManager.AnnouncementType.ACTION);
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });

        fabCreateQuiz.setOnClickListener(v -> {
            voiceGuidance.announce("Creating new AI quiz", VoiceGuidanceManager.AnnouncementType.ACTION);
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });
    }

    private void loadQuizzes() {
        loadingProgress.setVisibility(View.VISIBLE);
        quizRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Load sample quizzes for now
        currentQuizzes = getSampleQuizzes();
        
        loadingProgress.setVisibility(View.GONE);
        if (currentQuizzes.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.no_quizzes_available);
            voiceGuidance.announce("No quizzes available. Create one with AI.", VoiceGuidanceManager.AnnouncementType.INFORMATION);
        } else {
            quizRecyclerView.setVisibility(View.VISIBLE);
            adapter = new QuizListAdapter(currentQuizzes, this);
            quizRecyclerView.setAdapter(adapter);
        }
    }

    private List<QuizModel> getSampleQuizzes() {
        List<QuizModel> quizzes = new ArrayList<>();
        quizzes.add(new QuizModel("1", "English Grammar Quiz", "Test your grammar skills", 10, 15, false));
        quizzes.add(new QuizModel("2", "Math Practice", "Basic mathematics", 15, 20, false));
        quizzes.add(new QuizModel("3", "Science Quiz", "General science questions", 10, 15, true));
        quizzes.add(new QuizModel("4", "Hindi Vocabulary", "Learn new words", 20, 25, false));
        return quizzes;
    }

    @Override
    public void onQuizClick(QuizModel quiz, int position) {
        // Voice feedback before opening quiz
        String announcement = "Starting " + quiz.getTitle() + " quiz. " + 
            quiz.getQuestionCount() + " questions. " + quiz.getDurationMinutes() + " minutes.";
        voiceGuidance.announce(announcement, VoiceGuidanceManager.AnnouncementType.ACTION);
        voiceGuidance.vibrate(VoiceGuidanceManager.HapticPattern.SELECTION);
        
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("quiz_id", quiz.getId());
        intent.putExtra("quiz_title", quiz.getTitle());
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceGuidance != null) {
            voiceGuidance.stop();
        }
    }
}
