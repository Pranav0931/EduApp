package com.hdaf.eduapp.analytics;

/**
 * Aggregated performance metrics for a specific subject.
 */
public class SubjectPerformance {
    
    private String subjectName;
    private int quizzesTaken;
    private int totalQuestions;
    private int correctAnswers;
    private long totalTimeSpentMs;
    private int strongTopicsCount;
    private int weakTopicsCount;

    public SubjectPerformance(String subjectName) {
        this.subjectName = subjectName;
        this.quizzesTaken = 0;
        this.totalQuestions = 0;
        this.correctAnswers = 0;
        this.totalTimeSpentMs = 0;
    }

    public void addQuizResult(int total, int correct, long timeMs) {
        this.quizzesTaken++;
        this.totalQuestions += total;
        this.correctAnswers += correct;
        this.totalTimeSpentMs += timeMs;
    }

    public float getAccuracy() {
        if (totalQuestions == 0) return 0;
        return (float) correctAnswers / totalQuestions * 100;
    }

    public String getSubjectName() { return subjectName; }
    public int getQuizzesTaken() { return quizzesTaken; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public long getTotalTimeSpentMs() { return totalTimeSpentMs; }
    
    // Getters/Setters for topics if needed later
}
