package com.hdaf.eduapp.quiz;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hdaf.eduapp.analytics.AnalyticsManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for quiz flow and state management.
 * Handles quiz session, answer submission, and result calculation.
 */
public class QuizManager {

    private static final String PREFS_NAME = "quiz_prefs";
    private static final String KEY_ATTEMPTS = "quiz_attempts";
    private static final String KEY_CURRENT_QUIZ = "current_quiz";

    private static QuizManager instance;

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;

    private Quiz currentQuiz;
    private QuizAttempt currentAttempt;
    private int currentQuestionIndex = 0;

    private QuizManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized QuizManager getInstance(Context context) {
        if (instance == null) {
            instance = new QuizManager(context);
        }
        return instance;
    }

    /**
     * Start a new quiz session.
     */
    public void startQuiz(Quiz quiz, String userId) {
        this.currentQuiz = quiz;
        this.currentQuestionIndex = 0;
        this.currentAttempt = new QuizAttempt(
                userId,
                quiz.getId(),
                quiz.getTotalQuestions()
        );

        saveCurrentQuiz();
    }

    public Quiz getCurrentQuiz() {
        return currentQuiz;
    }

    public QuizAttempt getCurrentAttempt() {
        return currentAttempt;
    }

    public QuizQuestion getCurrentQuestion() {
        if (currentQuiz == null || currentQuestionIndex >= currentQuiz.getTotalQuestions()) {
            return null;
        }
        return currentQuiz.getQuestion(currentQuestionIndex);
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }

    /**
     * Submit answer for current question.
     */
    public boolean submitAnswer(int answerIndex) {
        if (currentQuiz == null || currentAttempt == null) {
            return false;
        }

        QuizQuestion currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return false;
        }

        currentAttempt.recordAnswer(currentQuestion.getId(), answerIndex);

        boolean isCorrect = currentQuestion.isCorrect(answerIndex);

        if (isCorrect) {
            currentAttempt.setCorrectAnswers(currentAttempt.getCorrectAnswers() + 1);
        } else {
            currentAttempt.setIncorrectAnswers(currentAttempt.getIncorrectAnswers() + 1);
            if (currentQuestion.getTopic() != null) {
                currentAttempt.addWeakTopic(currentQuestion.getTopic());
            }
        }

        return isCorrect;
    }

    public boolean nextQuestion() {
        if (currentQuestionIndex < currentQuiz.getTotalQuestions() - 1) {
            currentQuestionIndex++;
            saveCurrentQuiz();
            return true;
        }
        return false;
    }

    public boolean isQuizFinished() {
        return currentQuestionIndex >= currentQuiz.getTotalQuestions() - 1;
    }

    /**
     * Finish quiz and calculate results.
     */
    public QuizResult finishQuiz() {
        if (currentAttempt == null) {
            return null;
        }

        currentAttempt.complete();
        saveAttempt(currentAttempt);

        QuizResult result = new QuizResult(currentAttempt, currentQuiz);

        // ✅ Correct analytics logging
        try {
            AnalyticsManager.getInstance(context)
                    .logQuizResult(
                            currentQuiz.getSubject(),
                            result.getScore(),
                            result.getWeakTopics()
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

        clearCurrentQuiz();
        return result;
    }

    private void saveAttempt(QuizAttempt attempt) {
        List<QuizAttempt> attempts = getAttempts();
        attempts.add(attempt);

        String json = gson.toJson(attempts);
        prefs.edit().putString(KEY_ATTEMPTS, json).apply();
    }

    public List<QuizAttempt> getAttempts() {
        String json = prefs.getString(KEY_ATTEMPTS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<QuizAttempt>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public List<QuizAttempt> getAttemptsByUser(String userId) {
        List<QuizAttempt> allAttempts = getAttempts();
        List<QuizAttempt> userAttempts = new ArrayList<>();

        for (QuizAttempt attempt : allAttempts) {
            if (attempt.getUserId().equals(userId)) {
                userAttempts.add(attempt);
            }
        }

        return userAttempts;
    }

    private void saveCurrentQuiz() {
        if (currentQuiz != null) {
            String json = gson.toJson(currentQuiz);
            prefs.edit()
                    .putString(KEY_CURRENT_QUIZ, json)
                    .putInt("current_question_index", currentQuestionIndex)
                    .apply();
        }
    }

    private void clearCurrentQuiz() {
        currentQuiz = null;
        currentAttempt = null;
        currentQuestionIndex = 0;

        prefs.edit()
                .remove(KEY_CURRENT_QUIZ)
                .remove("current_question_index")
                .apply();
    }

    public boolean restoreQuizSession() {
        String json = prefs.getString(KEY_CURRENT_QUIZ, null);
        if (json != null) {
            currentQuiz = gson.fromJson(json, Quiz.class);
            currentQuestionIndex = prefs.getInt("current_question_index", 0);
            return true;
        }
        return false;
    }

    public double getAverageScore(String userId) {
        List<QuizAttempt> attempts = getAttemptsByUser(userId);
        if (attempts.isEmpty()) {
            return 0.0;
        }

        int totalScore = 0;
        for (QuizAttempt attempt : attempts) {
            totalScore += attempt.getScore();
        }

        return (double) totalScore / attempts.size();
    }
}
