package com.hdaf.eduapp.models;

/**
 * Model for quiz data.
 */
public class QuizModel {
    private String id;
    private String title;
    private String description;
    private int questionCount;
    private int durationMinutes;
    private boolean isAiGenerated;

    public QuizModel(String id, String title, String description, int questionCount, int durationMinutes, boolean isAiGenerated) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questionCount = questionCount;
        this.durationMinutes = durationMinutes;
        this.isAiGenerated = isAiGenerated;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getQuestionCount() { return questionCount; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isAiGenerated() { return isAiGenerated; }
}
