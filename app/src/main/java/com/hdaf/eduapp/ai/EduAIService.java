package com.hdaf.eduapp.ai;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Main service for EduAI - the voice-enabled AI tutor.
 * Handles conversation, intent extraction, and response generation.
 * 
 * Uses Google Gemini API for AI responses.
 */
public class EduAIService {

    private static final String TAG = "EduAIService";
    private static EduAIService instance;

    // Gemini API configuration
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private String apiKey;

    private final OkHttpClient httpClient;
    private final IntentParser intentParser;
    private final NavigationController navigationController;
    private final List<ConversationMessage> conversationHistory;

    // System prompt for EduAI - Ultra-advanced, emotionally intelligent AI tutor
    private static final String SYSTEM_PROMPT = 
        "You are EduAI, an ultra-advanced, fully accessible, multimodal AI education system. " +
        "You are the student's best friend, teacher, guide, and mentor inside EduApp.\\n\\n" +
        
        "PRIMARY MISSION: Make education accessible, inclusive, personalized, adaptive, motivating, " +
        "emotionally supportive, voice-driven, visually rich, and affordable for every student.\\n\\n" +
        
        "YOUR ROLES: Personal AI teacher, accessibility assistant, study planner, emotional support coach, " +
        "career guide, quiz generator, learning gap detector, and intelligent learning companion.\\n\\n" +
        
        "APP CONTEXT:\\n" +
        "- Classes: 1st to 10th\\n" +
        "- Subjects: English, Marathi, Hindi, Math, Science, Social Science\\n" +
        "- Features: Audio/Video/Text lessons, Diagrams, AI quizzes, Progress tracking, Gamification\\n\\n" +
        
        "PERSONALITY RULES:\\n" +
        "- Always friendly, motivating, never scold or shame\\n" +
        "- Use simple Hinglish (Hindi + English mix)\\n" +
        "- Short, clear, positive responses\\n" +
        "- Be emotionally supportive and empathetic\\n" +
        "- Use emojis: 📚✨🎧😄💪🎯\\n\\n" +
        
        "EMOTIONAL INTELLIGENCE - Detect and respond to:\\n" +
        "- Confusion: 'Koi baat nahi, step by step samjhte hain 😊'\\n" +
        "- Frustration: 'Relax karo, hum ek-ek step mein samajh lenge'\\n" +
        "- Exam stress: 'Darr mat, tu kar lega! Main help karunga 💪'\\n" +
        "- Low motivation: 'Chal ek chhota step lete hain, tu champion hai!'\\n\\n" +
        
        "VOICE COMMANDS - Understand conversational Hinglish:\\n" +
        "- 'Mujhe kuch bhi samajh nahi aa raha'\\n" +
        "- 'Kal exam hai darr lag raha hai'\\n" +
        "- 'Sirf important questions chahiye'\\n" +
        "- 'Is topic ko story bana ke samjha'\\n" +
        "- '8th class ka English chapter 2 khol do'\\n\\n" +
        
        "INTENT EXTRACTION - From every query extract:\\n" +
        "Class, Subject, Chapter, Topic, Emotion, Urgency, Action, Mode\\n\\n" +
        
        "NAVIGATION JSON FORMAT:\\n" +
        "{\\\"intent\\\": \\\"NAVIGATION\\\", \\\"action\\\": \\\"OPEN_CHAPTER\\\", " +
        "\\\"class\\\": \\\"8\\\", \\\"subject\\\": \\\"English\\\", \\\"chapter\\\": \\\"2\\\"}\\n\\n" +
        
        "AVAILABLE ACTIONS:\\n" +
        "OPEN_CLASS, OPEN_SUBJECT, OPEN_CHAPTER, PLAY_AUDIO, START_QUIZ, " +
        "EXPLAIN_CONCEPT, DAILY_CHALLENGE, STUDY_PLAN, CAREER_GUIDE\\n\\n" +
        
        "TEACHING STRATEGIES - Adapt based on student:\\n" +
        "- Story-based teaching for concepts\\n" +
        "- Analogy-based for difficult topics\\n" +
        "- Micro-learning chunks\\n" +
        "- Memory tricks and mnemonics\\n" +
        "- Visual-first for deaf students\\n" +
        "- Audio-first for blind students\\n\\n" +
        
        "QUIZ MODE:\\n" +
        "- 5-10 adaptive questions, 70% concept, 30% memory\\n" +
        "- ONE question at a time, wait for answer\\n" +
        "- Explain wrong answers kindly\\n" +
        "- For blind: Read aloud, accept A/B/C/D voice answers\\n" +
        "- Start: 'Great! Chalo smart quiz start karte hain 😄'\\n\\n" +
        
        "GAMIFICATION:\\n" +
        "- Daily streaks, XP points, badges, levels\\n" +
        "- 'Wah! 5 din ka streak! 🔥'\\n" +
        "- Study quests and challenges\\n\\n" +
        
        "STUDY PLANNER:\\n" +
        "- Generate daily/weekly study plans\\n" +
        "- Exam countdown schedules\\n" +
        "- Spaced repetition reminders\\n" +
        "- Most important questions before exams\\n\\n" +
        
        "BLIND-FIRST MODE:\\n" +
        "- Complete voice navigation\\n" +
        "- Audio descriptions of diagrams/images\\n" +
        "- Voice hints: 'Swipe right for next'\\n" +
        "- OCR-based book reading\\n\\n" +
        
        "DEAF-FIRST MODE:\\n" +
        "- Full captions, visual breakdowns\\n" +
        "- Animated explanations\\n" +
        "- Sign language video support\\n" +
        "- Strong visual cues\\n\\n" +
        
        "MENTAL WELLNESS:\\n" +
        "- Motivation and positive affirmations\\n" +
        "- Exam stress relief\\n" +
        "- Focus exercises\\n" +
        "- Confidence building\\n\\n" +
        
        "CAREER GUIDANCE:\\n" +
        "- Stream suggestions\\n" +
        "- Skill recommendations\\n" +
        "- Higher studies planning\\n\\n" +
        
        "HOMEWORK HELP:\\n" +
        "- Guide step-by-step, NEVER give direct answers\\n" +
        "- Teach HOW to solve\\n\\n" +
        
        "Remember: You are the student's best AI learning companion. " +
        "Make them feel confident, supported, and happy! 💪📖✨";

    public interface ResponseCallback {
        void onResponse(String message, EduIntent intent);
        void onError(String error);
    }

    private static class ConversationMessage {
        String role; // "user" or "model"
        String content;

        ConversationMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private EduAIService() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        intentParser = new IntentParser();
        navigationController = new NavigationController();
        conversationHistory = new ArrayList<>();
        
        // Initialize with system prompt
        conversationHistory.add(new ConversationMessage("user", "You are EduAI. Follow these instructions:\n" + SYSTEM_PROMPT));
        conversationHistory.add(new ConversationMessage("model", "Samajh gaya! Main EduAI hoon, tumhara study buddy 😄📚 Batao, aaj kya padhna hai?"));
    }

    public static synchronized EduAIService getInstance() {
        if (instance == null) {
            instance = new EduAIService();
        }
        return instance;
    }

    /**
     * Initialize with API key.
     */
    public void initialize(Context context, String apiKey) {
        this.apiKey = apiKey;
        com.hdaf.eduapp.quiz.QuizGenerator.getInstance(context).setApiKey(apiKey);
        Log.d(TAG, "EduAIService initialized");
    }

    /**
     * Check if service is configured and ready.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Send a message to EduAI and get a response.
     */
    public void sendMessage(String userMessage, ResponseCallback callback) {
        if (!isConfigured()) {
            // Fallback to local intent parsing
            handleLocalResponse(userMessage, callback);
            return;
        }

        // Add user message to history
        conversationHistory.add(new ConversationMessage("user", userMessage));

        // Build request body
        String requestBody = buildGeminiRequestBody(userMessage);
        
        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed", e);
                // Fallback to local response
                handleLocalResponse(userMessage, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "API error: " + response.code());
                    handleLocalResponse(userMessage, callback);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    String aiMessage = parseGeminiResponse(responseBody);
                    
                    // Add AI response to history
                    conversationHistory.add(new ConversationMessage("model", aiMessage));
                    
                    // Extract intent from response
                    EduIntent intent = extractIntentFromResponse(aiMessage, userMessage);
                    
                    // Clean message (remove JSON blocks)
                    String cleanMessage = cleanResponseMessage(aiMessage);
                    
                    callback.onResponse(cleanMessage, intent);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    handleLocalResponse(userMessage, callback);
                }
            }
        });
    }

    /**
     * Handle response locally without API (fallback mode).
     */
    private void handleLocalResponse(String userMessage, ResponseCallback callback) {
        // Parse intent locally
        EduIntent intent = intentParser.parse(userMessage);
        
        String response;
        
        if (intent.isNavigationIntent()) {
            response = navigationController.getNavigationMessage(intent);
        } else if (intent.isQuizIntent()) {
            response = "Great! Chalo ek quiz start karte hain 😄📝 Pehla question aa raha hai...";
        } else if (intent.isExplanationIntent()) {
            response = "Koi baat nahi! 😊 Batao exactly kaunsa part samajh nahi aaya? Main simple words mein samjhata hoon.";
        } else {
            response = "Haan, batao! Main tumhari help karne ke liye ready hoon 😄📚";
        }
        
        callback.onResponse(response, intent);
    }

    /**
     * Build Gemini API request body.
     */
    private String buildGeminiRequestBody(String userMessage) {
        try {
            JSONObject requestJson = new JSONObject();
            
            // Build contents array with conversation history
            StringBuilder contentsBuilder = new StringBuilder();
            contentsBuilder.append("{\"contents\":[");
            
            for (int i = 0; i < conversationHistory.size(); i++) {
                ConversationMessage msg = conversationHistory.get(i);
                if (i > 0) contentsBuilder.append(",");
                contentsBuilder.append("{\"role\":\"").append(msg.role)
                        .append("\",\"parts\":[{\"text\":\"")
                        .append(escapeJson(msg.content))
                        .append("\"}]}");
            }
            
            contentsBuilder.append("],\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":1024}}");
            
            return contentsBuilder.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error building request body", e);
            return "{}";
        }
    }

    /**
     * Parse Gemini API response.
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Gemini response", e);
            return "Oops! Kuch gadbad ho gayi. Phir se try karo! 😅";
        }
    }

    /**
     * Extract intent from AI response.
     */
    private EduIntent extractIntentFromResponse(String aiMessage, String userMessage) {
        // Try to find JSON in response
        int jsonStart = aiMessage.indexOf("{");
        int jsonEnd = aiMessage.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String jsonStr = aiMessage.substring(jsonStart, jsonEnd + 1);
            EduIntent intent = EduIntent.fromJson(jsonStr);
            if (intent != null) {
                intent.setRawQuery(userMessage);
                return intent;
            }
        }
        
        // Fallback to local parsing
        return intentParser.parse(userMessage);
    }

    /**
     * Clean response message by removing JSON blocks.
     */
    private String cleanResponseMessage(String message) {
        // Remove JSON blocks
        String cleaned = message.replaceAll("```json[\\s\\S]*?```", "").trim();
        cleaned = cleaned.replaceAll("\\{[\\s\\S]*?\\}", "").trim();
        
        // Remove extra whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned.isEmpty() ? message : cleaned;
    }

    /**
     * Escape special characters for JSON.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Clear conversation history.
     */
    public void clearHistory() {
        conversationHistory.clear();
        // Re-add system prompt
        conversationHistory.add(new ConversationMessage("user", "You are EduAI. Follow these instructions:\n" + SYSTEM_PROMPT));
        conversationHistory.add(new ConversationMessage("model", "Samajh gaya! Main EduAI hoon, tumhara study buddy 😄📚 Batao, aaj kya padhna hai?"));
    }

    /**
     * Get a greeting message.
     */
    public String getGreeting() {
        return "Hey! Main EduAI hoon, tumhara personal study buddy 😄📚\n\nBatao, kya sikhna hai aaj?";
    }

    /**
     * Get the intent parser for direct use.
     */
    public IntentParser getIntentParser() {
        return intentParser;
    }

    /**
     * Get the navigation controller for direct use.
     */
    public NavigationController getNavigationController() {
        return navigationController;
    }
}
