package com.hdaf.eduapp.quiz;

/**
 * Model class representing a single quiz question.
 * Used in AI-generated quizzes with multiple choice options.
 */
public class QuizQuestion {
    
    private String id;
    private String question;
    private String[] options;
    private int correctAnswerIndex;
    private String explanation;
    private String topic;
    private String difficulty; // easy, medium, hard
    private String conceptType; // concept, memory
    
    public QuizQuestion() {
        // Required empty constructor for Firebase/Gson
    }
    
    public QuizQuestion(String question, String[] options, int correctAnswerIndex, 
                       String explanation, String topic, String difficulty) {
        this.id = java.util.UUID.randomUUID().toString();
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
        this.topic = topic;
        this.difficulty = difficulty;
        this.conceptType = "concept"; // default
    }
    
    // Getters
    public String getId() { return id; }
    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getCorrectAnswer() { 
        return options != null && correctAnswerIndex < options.length 
            ? options[correctAnswerIndex] : null; 
    }
    public String getExplanation() { return explanation; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public String getConceptType() { return conceptType; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setOptions(String[] options) { this.options = options; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { 
        this.correctAnswerIndex = correctAnswerIndex; 
    }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setConceptType(String conceptType) { this.conceptType = conceptType; }
    
    /**
     * Check if the given answer index is correct.
     */
    public boolean isCorrect(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
    
    /**
     * Get option letter (A, B, C, D) for answer index.
     */
    public static String getOptionLetter(int index) {
        String[] letters = {"A", "B", "C", "D", "E"};
        return index >= 0 && index < letters.length ? letters[index] : "";
    }
}
