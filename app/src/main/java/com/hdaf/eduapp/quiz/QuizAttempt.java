package com.hdaf.eduapp.quiz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a quiz attempt/session.
 * Tracks user's answers, score, and performance.
 */
public class QuizAttempt {
    
    private String id;
    private String userId;
    private String quizId;
    private int score; // percentage
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private Map<String, Integer> userAnswers; // questionId -> answerIndex
    private List<String> weakTopics;
    private Date startTime;
    private Date endTime;
    private long timeTaken; // in seconds
    private boolean completed;
    
    public QuizAttempt() {
        this.userAnswers = new HashMap<>();
        this.weakTopics = new ArrayList<>();
        this.startTime = new Date();
        this.completed = false;
    }
    
    public QuizAttempt(String userId, String quizId, int totalQuestions) {
        this.id = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.userAnswers = new HashMap<>();
        this.weakTopics = new ArrayList<>();
        this.startTime = new Date();
        this.completed = false;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getQuizId() { return quizId; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getIncorrectAnswers() { return incorrectAnswers; }
    public Map<String, Integer> getUserAnswers() { return userAnswers; }
    public List<String> getWeakTopics() { return weakTopics; }
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public long getTimeTaken() { return timeTaken; }
    public boolean isCompleted() { return completed; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public void setScore(int score) { this.score = score; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public void setIncorrectAnswers(int incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }
    public void setUserAnswers(Map<String, Integer> userAnswers) { this.userAnswers = userAnswers; }
    public void setWeakTopics(List<String> weakTopics) { this.weakTopics = weakTopics; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    /**
     * Record a user's answer for a question.
     */
    public void recordAnswer(String questionId, int answerIndex) {
        if (userAnswers == null) {
            userAnswers = new HashMap<>();
        }
        userAnswers.put(questionId, answerIndex);
    }
    
    /**
     * Mark a topic as weak based on incorrect answers.
     */
    public void addWeakTopic(String topic) {
        if (weakTopics == null) {
            weakTopics = new ArrayList<>();
        }
        if (!weakTopics.contains(topic)) {
            weakTopics.add(topic);
        }
    }
    
    /**
     * Complete the quiz attempt and calculate final stats.
     */
    public void complete() {
        this.endTime = new Date();
        this.completed = true;
        
        if (startTime != null && endTime != null) {
            this.timeTaken = (endTime.getTime() - startTime.getTime()) / 1000; // convert to seconds
        }
        
        // Calculate score
        if (totalQuestions > 0) {
            this.score = (correctAnswers * 100) / totalQuestions;
        }
    }
    
    /**
     * Get user's answer for a specific question.
     */
    public Integer getAnswer(String questionId) {
        return userAnswers != null ? userAnswers.get(questionId) : null;
    }
    
    /**
     * Check if the attempt passed based on passing score.
     */
    public boolean hasPassed(int passingScore) {
        return score >= passingScore;
    }
    
    /**
     * Get accuracy percentage.
     */
    public double getAccuracy() {
        return totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0;
    }
}
