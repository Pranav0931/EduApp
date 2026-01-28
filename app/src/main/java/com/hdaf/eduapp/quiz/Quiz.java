package com.hdaf.eduapp.quiz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a complete quiz.
 * Contains metadata and list of questions.
 */
public class Quiz {
    
    private String id;
    private String title;
    private String classId;
    private String subject;
    private String chapter;
    private String difficulty;
    private List<QuizQuestion> questions;
    private int totalQuestions;
    private int duration; // in minutes
    private int passingScore; // percentage
    private String createdBy; // "AI" or userId
    private Date createdAt;
    
    public Quiz() {
        this.questions = new ArrayList<>();
        this.createdAt = new Date();
    }
    
    public Quiz(String title, String subject, String chapter, List<QuizQuestion> questions) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.subject = subject;
        this.chapter = chapter;
        this.questions = questions != null ? questions : new ArrayList<>();
        this.totalQuestions = this.questions.size();
        this.duration = calculateDuration();
        this.passingScore = 60; // default 60%
        this.createdBy = "AI";
        this.createdAt = new Date();
    }
    
    /**
     * Calculate quiz duration based on number of questions.
     * Estimated 1 minute per question.
     */
    private int calculateDuration() {
        return totalQuestions > 0 ? totalQuestions : 10;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getClassId() { return classId; }
    public String getSubject() { return subject; }
    public String getChapter() { return chapter; }
    public String getDifficulty() { return difficulty; }
    public List<QuizQuestion> getQuestions() { return questions; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getDuration() { return duration; }
    public int getPassingScore() { return passingScore; }
    public String getCreatedBy() { return createdBy; }
    public Date getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setClassId(String classId) { this.classId = classId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestions(List<QuizQuestion> questions) { 
        this.questions = questions;
        this.totalQuestions = questions != null ? questions.size() : 0;
    }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    /**
     * Add a question to the quiz.
     */
    public void addQuestion(QuizQuestion question) {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        questions.add(question);
        totalQuestions = questions.size();
    }
    
    /**
     * Get question by index.
     */
    public QuizQuestion getQuestion(int index) {
        return questions != null && index >= 0 && index < questions.size() 
            ? questions.get(index) : null;
    }
}
