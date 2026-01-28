package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.quiz.Quiz;
import com.hdaf.eduapp.quiz.QuizManager;
import com.hdaf.eduapp.quiz.QuizQuestion;
import com.hdaf.eduapp.quiz.QuizResult;

/**
 * Activity for conducting quizzes with voice support for accessibility.
 */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ = "extra_quiz";
    public static final String EXTRA_USER_ID = "extra_user_id";

    private TextView txtQuizTitle, txtTimer, txtProgress, txtScore, txtQuestion;
    private ProgressBar progressBar;
    private MaterialCardView[] optionCards;
    private RadioButton[] optionButtons;
    private MaterialButton btnNext;
    private ImageButton btnClose;

    private QuizManager quizManager;
    private TTSManager ttsManager;
    private Quiz quiz;
    private String userId;

    private int selectedOptionIndex = -1;
    private int currentScore = 0;
    private CountDownTimer timer;
    private long timeRemainingMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initViews();
        initData();
        setupClickListeners();
    }

    private void initViews() {
        txtQuizTitle = findViewById(R.id.txt_quiz_title);
        txtTimer = findViewById(R.id.txt_timer);
        txtProgress = findViewById(R.id.txt_progress);
        txtScore = findViewById(R.id.txt_score);
        txtQuestion = findViewById(R.id.txt_question);
        progressBar = findViewById(R.id.progress_bar);
        btnNext = findViewById(R.id.btn_next);
        btnClose = findViewById(R.id.btn_close);

        optionCards = new MaterialCardView[]{
                findViewById(R.id.option_a_card),
                findViewById(R.id.option_b_card),
                findViewById(R.id.option_c_card),
                findViewById(R.id.option_d_card)
        };

        optionButtons = new RadioButton[]{
                findViewById(R.id.option_a),
                findViewById(R.id.option_b),
                findViewById(R.id.option_c),
                findViewById(R.id.option_d)
        };
    }

    private void initData() {
        quizManager = QuizManager.getInstance(this);

        ttsManager = TTSManager.getInstance();
        ttsManager.initialize(this);

        quiz = (Quiz) getIntent().getSerializableExtra(EXTRA_QUIZ);
        userId = getIntent().getStringExtra(EXTRA_USER_ID);

        if (userId == null) userId = "default_user";

        if (quiz != null) {
            quizManager.startQuiz(quiz, userId);
            startQuizSession();
        } else if (quizManager.restoreQuizSession()) {
            quiz = quizManager.getCurrentQuiz();
            startQuizSession();
        } else {
            Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startQuizSession() {
        txtQuizTitle.setText(quiz.getTitle());
        timeRemainingMs = quiz.getDuration() * 60 * 1000L;
        startTimer();
        displayQuestion();
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> showExitConfirmation());
        btnNext.setOnClickListener(v -> handleNextQuestion());

        for (int i = 0; i < optionCards.length; i++) {
            final int index = i;
            optionCards[i].setOnClickListener(v -> selectOption(index));
            optionButtons[i].setOnClickListener(v -> selectOption(index));
        }
    }

    private void selectOption(int index) {
        selectedOptionIndex = index;

        for (int i = 0; i < optionCards.length; i++) {
            if (i == index) {
                optionCards[i].setStrokeColor(getColor(R.color.primary));
                optionCards[i].setStrokeWidth(4);
            } else {
                optionCards[i].setStrokeColor(getColor(R.color.surface_variant));
                optionCards[i].setStrokeWidth(2);
            }
        }

        btnNext.setEnabled(true);

        QuizQuestion question = quizManager.getCurrentQuestion();
        if (question != null) {
            ttsManager.speak("Option " + QuizQuestion.getOptionLetter(index) + " selected");
        }
    }

    private void displayQuestion() {
        QuizQuestion question = quizManager.getCurrentQuestion();
        if (question == null) {
            finishQuiz();
            return;
        }

        int currentNum = quizManager.getCurrentQuestionNumber();
        int total = quiz.getTotalQuestions();

        txtProgress.setText("Question " + currentNum + " of " + total);
        progressBar.setProgress((currentNum * 100) / total);
        txtScore.setText("Score: " + currentScore);
        txtQuestion.setText(question.getQuestion());

        String[] options = question.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(QuizQuestion.getOptionLetter(i) + ". " + options[i]);
            optionCards[i].setVisibility(View.VISIBLE);
        }

        resetSelection();

        btnNext.setText(quizManager.isQuizFinished() ? R.string.finish : R.string.next);

        speakQuestion(question);
    }

    private void speakQuestion(QuizQuestion question) {
        StringBuilder speech = new StringBuilder();
        speech.append("Question ").append(quizManager.getCurrentQuestionNumber()).append(". ");
        speech.append(question.getQuestion()).append(". Options. ");

        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            speech.append(QuizQuestion.getOptionLetter(i)).append(". ").append(options[i]).append(". ");
        }

        ttsManager.speak(speech.toString());
    }

    private void resetSelection() {
        selectedOptionIndex = -1;
        btnNext.setEnabled(false);

        for (MaterialCardView card : optionCards) {
            card.setStrokeColor(getColor(R.color.surface_variant));
            card.setStrokeWidth(2);
        }
    }

    private void handleNextQuestion() {
        if (selectedOptionIndex == -1) return;

        boolean isCorrect = quizManager.submitAnswer(selectedOptionIndex);

        if (isCorrect) {
            currentScore += 10;
            ttsManager.speak("Correct! Well done.");
        } else {
            QuizQuestion question = quizManager.getCurrentQuestion();
            ttsManager.speak("Incorrect. Correct answer is " + question.getCorrectAnswer());
        }

        btnNext.postDelayed(() -> {
            if (quizManager.nextQuestion()) {
                displayQuestion();
            } else {
                finishQuiz();
            }
        }, 1200);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(timeRemainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
                long min = millisUntilFinished / 60000;
                long sec = (millisUntilFinished % 60000) / 1000;
                txtTimer.setText(String.format("%02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                ttsManager.speak("Time is up");
                finishQuiz();
            }
        }.start();
    }

    private void finishQuiz() {
        if (timer != null) timer.cancel();

        QuizResult result = quizManager.finishQuiz();

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_SCORE, result.getScore());
        intent.putExtra(QuizResultActivity.EXTRA_CORRECT, result.getCorrectAnswers());
        intent.putExtra(QuizResultActivity.EXTRA_TOTAL, result.getTotalQuestions());
        intent.putExtra(QuizResultActivity.EXTRA_TIME, result.getFormattedTimeTaken());
        intent.putExtra(QuizResultActivity.EXTRA_MESSAGE, result.getMotivationalMessage());
        intent.putExtra(QuizResultActivity.EXTRA_PASSED, result.isPassed());
        startActivity(intent);

        finish();
    }

    private void showExitConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Exit Quiz?")
                .setMessage("Your progress will be lost.")
                .setPositiveButton("Exit", (d, w) -> finish())
                .setNegativeButton("Continue", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
