package com.hdaf.eduapp.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing quiz results.
 * Contains score, performance breakdown, and recommendations.
 */
public class QuizResult {
    
    private QuizAttempt attempt;
    private Quiz quiz;
    
    private int score;
    private int correctAnswers;
    private int incorrectAnswers;
    private int totalQuestions;
    
    private boolean passed;
    private String performance; // Excellent, Good, Average, Needs Improvement
    private List<String> weakTopics;
    private List<String> strongTopics;
    private String motivationalMessage;
    
    public QuizResult(QuizAttempt attempt, Quiz quiz) {
        this.attempt = attempt;
        this.quiz = quiz;
        
        this.score = attempt.getScore();
        this.correctAnswers = attempt.getCorrectAnswers();
        this.incorrectAnswers = attempt.getIncorrectAnswers();
        this.totalQuestions = attempt.getTotalQuestions();
        
        this.passed = attempt.hasPassed(quiz.getPassingScore());
        this.performance = calculatePerformance();
        this.weakTopics = attempt.getWeakTopics();
        this.strongTopics = identifyStrongTopics();
        this.motivationalMessage = generateMotivationalMessage();
    }
    
    // Getters
    public QuizAttempt getAttempt() { return attempt; }
    public Quiz getQuiz() { return quiz; }
    public int getScore() { return score; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getIncorrectAnswers() { return incorrectAnswers; }
    public int getTotalQuestions() { return totalQuestions; }
    public boolean isPassed() { return passed; }
    public String getPerformance() { return performance; }
    public List<String> getWeakTopics() { return weakTopics; }
    public List<String> getStrongTopics() { return strongTopics; }
    public String getMotivationalMessage() { return motivationalMessage; }
    
    /**
     * Calculate performance rating based on score.
     */
    private String calculatePerformance() {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 60) return "Average";
        return "Needs Improvement";
    }
    
    /**
     * Identify strong topics (correct answers).
     */
    private List<String> identifyStrongTopics() {
        List<String> strong = new ArrayList<>();
        Map<String, Integer> topicCorrectCount = new HashMap<>();
        
        if (quiz == null || quiz.getQuestions() == null) {
            return strong;
        }
        
        for (QuizQuestion question : quiz.getQuestions()) {
            Integer userAnswer = attempt.getAnswer(question.getId());
            if (userAnswer != null && question.isCorrect(userAnswer)) {
                String topic = question.getTopic();
                if (topic != null && !topic.isEmpty()) {
                    topicCorrectCount.put(topic, 
                        topicCorrectCount.getOrDefault(topic, 0) + 1);
                }
            }
        }
        
        // Topics with multiple correct answers are strong
        for (Map.Entry<String, Integer> entry : topicCorrectCount.entrySet()) {
            if (entry.getValue() >= 2) {
                strong.add(entry.getKey());
            }
        }
        
        return strong;
    }
    
    /**
     * Generate motivational message based on performance.
     */
    private String generateMotivationalMessage() {
        if (score >= 90) {
            return "Excellent work! 🎉 Tumne bahut achha performance kiya!";
        } else if (score >= 75) {
            return "Great job! 👏 Tum achha kar rahe ho, keep it up!";
        } else if (score >= 60) {
            return "Good effort! 💪 Thoda aur practice karo, tum improve karoge!";
        } else {
            return "Don't worry! 😊 Practice karte raho, tum zaroor improve karoge!";
        }
    }
    
    /**
     * Get accuracy percentage.
     */
    public double getAccuracy() {
        return totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0;
    }
    
    /**
     * Get time taken in minutes.
     */
    public double getTimeTakenMinutes() {
        return attempt.getTimeTaken() / 60.0;
    }
    
    /**
     * Get formatted time taken string.
     */
    public String getFormattedTimeTaken() {
        long seconds = attempt.getTimeTaken();
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    /**
     * Should suggest revision?
     */
    public boolean shouldSuggestRevision() {
        return !weakTopics.isEmpty() || score < 75;
    }
    
    /**
     * Get revision recommendation message.
     */
    public String getRevisionRecommendation() {
        if (weakTopics.isEmpty()) {
            return "Great! Saare topics clear hain 📚";
        }
        
        StringBuilder sb = new StringBuilder("In topics pe dhyan do:\n");
        for (int i = 0; i < weakTopics.size(); i++) {
            sb.append("• ").append(weakTopics.get(i));
            if (i < weakTopics.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
