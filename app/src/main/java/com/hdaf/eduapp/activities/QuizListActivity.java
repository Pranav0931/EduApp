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
import com.hdaf.eduapp.adapters.QuizListAdapter;
import com.hdaf.eduapp.models.QuizModel;
import com.hdaf.eduapp.ui.EduAIChatBottomSheet;
import com.hdaf.eduapp.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz list screen - displays available quizzes.
 * Supports AI quiz generation.
 */
public class QuizListActivity extends AppCompatActivity implements QuizListAdapter.OnQuizClickListener {

    private RecyclerView quizRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;
    private FloatingActionButton fabAiChat;
    private FloatingActionButton fabCreateQuiz;
    private BottomNavigationView bottomNavigation;

    private PreferenceManager prefManager;
    private QuizListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        prefManager = PreferenceManager.getInstance(this);

        initializeViews();
        setupBottomNavigation();
        setupFabs();
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

        headerTitle.setText(R.string.nav_quizzes);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuButton.setOnClickListener(v -> onBackPressed());
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
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });

        fabCreateQuiz.setOnClickListener(v -> {
            // TODO: Open AI Quiz generation dialog
            EduAIChatBottomSheet chatSheet = EduAIChatBottomSheet.newInstance();
            chatSheet.show(getSupportFragmentManager(), "EduAIChat");
        });
    }

    private void loadQuizzes() {
        loadingProgress.setVisibility(View.VISIBLE);
        quizRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Load sample quizzes for now
        List<QuizModel> quizzes = getSampleQuizzes();
        
        loadingProgress.setVisibility(View.GONE);
        if (quizzes.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.no_quizzes_available);
        } else {
            quizRecyclerView.setVisibility(View.VISIBLE);
            adapter = new QuizListAdapter(quizzes, this);
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
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("quiz_id", quiz.getId());
        intent.putExtra("quiz_title", quiz.getTitle());
        startActivity(intent);
    }
}
