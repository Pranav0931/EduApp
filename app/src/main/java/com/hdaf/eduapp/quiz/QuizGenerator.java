package com.hdaf.eduapp.quiz;

import android.content.Context;
import android.util.Log;

import com.hdaf.eduapp.ai.EduAIService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AI-powered quiz generator using Gemini API.
 * Generates context-aware quizzes based on subject, chapter, and difficulty.
 */
public class QuizGenerator {
    
    private static final String TAG = "QuizGenerator";
    private static QuizGenerator instance;
    
    private final Context context;
    private final OkHttpClient httpClient;
    private String apiKey;
    
    private QuizGenerator(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient();
    }
    
    public static synchronized QuizGenerator getInstance(Context context) {
        if (instance == null) {
            instance = new QuizGenerator(context);
        }
        return instance;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Generate a quiz using Gemini API.
     */
    public void generateQuiz(String subject, String chapter, int questionCount,
                            String difficulty, QuizGenerationCallback callback) {
        
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not configured");
            return;
        }
        
        new Thread(() -> {
            try {
                String prompt = buildQuizPrompt(subject, chapter, questionCount, difficulty);
                String response = callGeminiAPI(prompt);
                Quiz quiz = parseQuizResponse(response, subject, chapter);
                
                if (quiz != null && quiz.getQuestions().size() > 0) {
                    callback.onQuizGenerated(quiz);
                } else {
                    callback.onError("Failed to generate quiz questions");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Quiz generation error", e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Build prompt for quiz generation.
     */
    private String buildQuizPrompt(String subject, String chapter, int count, String difficulty) {
        return String.format(
            "Generate a %s difficulty quiz for %s - %s with exactly %d multiple choice questions.\n\n" +
            "Requirements:\n" +
            "- 70%% concept-based questions, 30%% memory-based\n" +
            "- Each question has 4 options (A, B, C, D)\n" +
            "- Include explanation for each answer\n" +
            "- Questions should be in simple Hinglish\n" +
            "- Suitable for Indian students\n\n" +
            "Return ONLY a JSON array in this exact format:\n" +
            "[\n" +
            "  {\n" +
            "    \"question\": \"Question text here?\",\n" +
            "    \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
            "    \"correctAnswerIndex\": 0,\n" +
            "    \"explanation\": \"Explanation of why this answer is correct\",\n" +
            "    \"topic\": \"Topic name\",\n" +
            "    \"difficulty\": \"%s\"\n" +
            "  }\n" +
            "]\n\n" +
            "Generate %d questions now:",
            difficulty, subject, chapter, count, difficulty, count
        );
    }
    
    /**
     * Call Gemini API to generate quiz.
     */
    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;
        
        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestJson.put("contents", contents);
        
        RequestBody body = RequestBody.create(
            requestJson.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("API call failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject contentObj = firstCandidate.getJSONObject("content");
                JSONArray partsArray = contentObj.getJSONArray("parts");
                if (partsArray.length() > 0) {
                    return partsArray.getJSONObject(0).getString("text");
                }
            }
            
            throw new Exception("No response from AI");
        }
    }
    
    /**
     * Parse Gemini response into Quiz object.
     */
    private Quiz parseQuizResponse(String response, String subject, String chapter) {
        try {
            // Extract JSON array from response (AI might wrap it in markdown)
            String jsonStr = extractJSON(response);
            
            JSONArray questionsArray = new JSONArray(jsonStr);
            List<QuizQuestion> questions = new ArrayList<>();
            
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject qObj = questionsArray.getJSONObject(i);
                
                String question = qObj.getString("question");
                JSONArray optionsArray = qObj.getJSONArray("options");
                String[] options = new String[optionsArray.length()];
                for (int j = 0; j < optionsArray.length(); j++) {
                    options[j] = optionsArray.getString(j);
                }
                
                int correctIndex = qObj.getInt("correctAnswerIndex");
                String explanation = qObj.getString("explanation");
                String topic = qObj.optString("topic", chapter);
                String difficulty = qObj.optString("difficulty", "medium");
                
                QuizQuestion quizQuestion = new QuizQuestion(
                    question, options, correctIndex, explanation, topic, difficulty
                );
                questions.add(quizQuestion);
            }
            
            String title = subject + " - " + chapter + " Quiz";
            Quiz quiz = new Quiz(title, subject, chapter, questions);
            quiz.setDifficulty("medium");
            
            return quiz;
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing quiz response", e);
            return null;
        }
    }
    
    /**
     * Extract JSON array from response text.
     * Handles cases where AI wraps JSON in markdown code blocks.
     */
    private String extractJSON(String text) {
        // Remove markdown code blocks if present
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        
        text = text.trim();
        
        // Find JSON array boundaries
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }
    
    /**
     * Callback interface for quiz generation.
     */
    public interface QuizGenerationCallback {
        void onQuizGenerated(Quiz quiz);
        void onError(String error);
    }
}
