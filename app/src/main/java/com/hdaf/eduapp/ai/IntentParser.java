package com.hdaf.eduapp.ai;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting structured intents from natural language input.
 * Supports Hinglish (Hindi + English) and pure English commands.
 * 
 * Example inputs:
 * - "8th class ka English ka 2nd chapter khol do"
 * - "Mujhe 7th class ka science ka chapter 3 sunna hai"
 * - "Play 9th standard maths chapter 5"
 * - "Is chapter ka quiz banao"
 */
public class IntentParser {

    private static final String TAG = "IntentParser";

    // Class number patterns (supports various formats)
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?:class|standard|std|kaksha)\\s*(?:ka|ki|ke)?\\s*(\\d+)(?:st|nd|rd|th)?|" +
            "(\\d+)(?:st|nd|rd|th)?\\s*(?:class|standard|std|kaksha)",
            Pattern.CASE_INSENSITIVE
    );

    // Subject patterns with Hindi/English variations
    private static final Map<String, String[]> SUBJECT_PATTERNS = new HashMap<>();
    static {
        SUBJECT_PATTERNS.put("English", new String[]{"english", "angrezi", "angrez"});
        SUBJECT_PATTERNS.put("Hindi", new String[]{"hindi"});
        SUBJECT_PATTERNS.put("Marathi", new String[]{"marathi"});
        SUBJECT_PATTERNS.put("Math", new String[]{"math", "maths", "mathematics", "ganit"});
        SUBJECT_PATTERNS.put("Science", new String[]{"science", "vigyan"});
        SUBJECT_PATTERNS.put("Social Science", new String[]{"social", "social science", "sst", "samajik vigyan", "history", "geography", "civics"});
    }

    // Chapter number patterns
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "(?:chapter|unit|paath|adhyay)\\s*(?:no\\.?|number)?\\s*(\\d+)|" +
            "(\\d+)(?:st|nd|rd|th)?\\s*(?:chapter|unit|paath|adhyay)",
            Pattern.CASE_INSENSITIVE
    );

    // Action patterns
    private static final Map<EduIntent.ActionType, String[]> ACTION_PATTERNS = new HashMap<>();
    static {
        ACTION_PATTERNS.put(EduIntent.ActionType.OPEN_CHAPTER, new String[]{
                "khol", "open", "dikhao", "show", "le jao", "take me", "jao"
        });
        ACTION_PATTERNS.put(EduIntent.ActionType.PLAY_AUDIO, new String[]{
                "play", "chalao", "sunao", "sunna", "audio", "bolo", "padho", "read"
        });
        ACTION_PATTERNS.put(EduIntent.ActionType.START_QUIZ, new String[]{
                "quiz", "test", "practice", "abhyas", "pariksha", "sawaal", "question"
        });
        ACTION_PATTERNS.put(EduIntent.ActionType.EXPLAIN_CONCEPT, new String[]{
                "explain", "samjhao", "batao", "kya hai", "what is", "samajh nahi", 
                "understand", "meaning", "matlab", "define"
        });
        ACTION_PATTERNS.put(EduIntent.ActionType.DAILY_CHALLENGE, new String[]{
                "daily", "challenge", "aaj ka", "today"
        });
        ACTION_PATTERNS.put(EduIntent.ActionType.STUDY_PLAN, new String[]{
                "study plan", "schedule", "timetable", "plan banao"
        });
    }

    /**
     * Parse user input and extract structured intent.
     * 
     * @param input Natural language input from user
     * @return EduIntent with extracted information
     */
    public EduIntent parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new EduIntent.Builder()
                    .setAction(EduIntent.ActionType.UNKNOWN)
                    .build();
        }

        String normalizedInput = input.toLowerCase().trim();
        Log.d(TAG, "Parsing input: " + normalizedInput);

        EduIntent.Builder builder = new EduIntent.Builder();
        builder.setRawQuery(input);

        // Extract class number
        Integer classNumber = extractClassNumber(normalizedInput);
        if (classNumber != null) {
            builder.setClassNumber(classNumber);
            Log.d(TAG, "Extracted class: " + classNumber);
        }

        // Extract subject
        String subject = extractSubject(normalizedInput);
        if (subject != null) {
            builder.setSubject(subject);
            Log.d(TAG, "Extracted subject: " + subject);
        }

        // Extract chapter number
        Integer chapterNumber = extractChapterNumber(normalizedInput);
        if (chapterNumber != null) {
            builder.setChapterNumber(chapterNumber);
            Log.d(TAG, "Extracted chapter: " + chapterNumber);
        }

        // Determine action
        EduIntent.ActionType action = determineAction(normalizedInput);
        builder.setAction(action);
        Log.d(TAG, "Determined action: " + action);

        // Extract topic for explanation requests
        if (action == EduIntent.ActionType.EXPLAIN_CONCEPT) {
            String topic = extractTopic(normalizedInput);
            if (topic != null) {
                builder.setTopic(topic);
            }
        }

        return builder.build();
    }

    /**
     * Extract class number from input.
     */
    private Integer extractClassNumber(String input) {
        Matcher matcher = CLASS_PATTERN.matcher(input);
        if (matcher.find()) {
            String classStr = matcher.group(1);
            if (classStr == null) {
                classStr = matcher.group(2);
            }
            if (classStr != null) {
                try {
                    int classNum = Integer.parseInt(classStr);
                    if (classNum >= 1 && classNum <= 10) {
                        return classNum;
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse class number: " + classStr);
                }
            }
        }

        // Try simple number extraction with context
        Pattern simplePattern = Pattern.compile("(\\d+)(?:st|nd|rd|th)?\\s*(?:ka|ki|ke)", Pattern.CASE_INSENSITIVE);
        matcher = simplePattern.matcher(input);
        if (matcher.find()) {
            try {
                int classNum = Integer.parseInt(matcher.group(1));
                if (classNum >= 1 && classNum <= 10) {
                    return classNum;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return null;
    }

    /**
     * Extract subject from input.
     */
    private String extractSubject(String input) {
        for (Map.Entry<String, String[]> entry : SUBJECT_PATTERNS.entrySet()) {
            String subject = entry.getKey();
            for (String pattern : entry.getValue()) {
                if (input.contains(pattern)) {
                    return subject;
                }
            }
        }
        return null;
    }

    /**
     * Extract chapter number from input.
     */
    private Integer extractChapterNumber(String input) {
        Matcher matcher = CHAPTER_PATTERN.matcher(input);
        if (matcher.find()) {
            String chapterStr = matcher.group(1);
            if (chapterStr == null) {
                chapterStr = matcher.group(2);
            }
            if (chapterStr != null) {
                try {
                    return Integer.parseInt(chapterStr);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse chapter number: " + chapterStr);
                }
            }
        }
        return null;
    }

    /**
     * Determine the action type from input.
     */
    private EduIntent.ActionType determineAction(String input) {
        // Check action patterns in priority order
        EduIntent.ActionType[] priorityOrder = {
                EduIntent.ActionType.START_QUIZ,
                EduIntent.ActionType.PLAY_AUDIO,
                EduIntent.ActionType.EXPLAIN_CONCEPT,
                EduIntent.ActionType.DAILY_CHALLENGE,
                EduIntent.ActionType.STUDY_PLAN,
                EduIntent.ActionType.OPEN_CHAPTER
        };

        for (EduIntent.ActionType actionType : priorityOrder) {
            String[] patterns = ACTION_PATTERNS.get(actionType);
            if (patterns != null) {
                for (String pattern : patterns) {
                    if (input.contains(pattern)) {
                        return actionType;
                    }
                }
            }
        }

        // Default action based on extracted info
        // If we have class/subject/chapter info, assume navigation
        return EduIntent.ActionType.CHAT;
    }

    /**
     * Extract topic for explanation requests.
     */
    private String extractTopic(String input) {
        // Common patterns for topic extraction
        String[] topicPatterns = {
                "(?:explain|samjhao|batao)\\s+(?:mujhe\\s+)?(?:about\\s+)?(.+?)(?:\\s+ko)?$",
                "(.+?)\\s+(?:kya hai|what is|meaning|matlab)",
                "(?:kya hai|what is)\\s+(.+)"
        };

        for (String patternStr : topicPatterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String topic = matcher.group(1);
                if (topic != null && !topic.isEmpty()) {
                    return topic.trim();
                }
            }
        }

        return null;
    }

    /**
     * Check if input contains a navigation request.
     */
    public boolean isNavigationRequest(String input) {
        if (input == null) return false;
        String normalized = input.toLowerCase();
        
        for (String pattern : ACTION_PATTERNS.get(EduIntent.ActionType.OPEN_CHAPTER)) {
            if (normalized.contains(pattern)) return true;
        }
        for (String pattern : ACTION_PATTERNS.get(EduIntent.ActionType.PLAY_AUDIO)) {
            if (normalized.contains(pattern)) return true;
        }
        
        return false;
    }

    /**
     * Check if input is a quiz request.
     */
    public boolean isQuizRequest(String input) {
        if (input == null) return false;
        String normalized = input.toLowerCase();
        
        String[] patterns = ACTION_PATTERNS.get(EduIntent.ActionType.START_QUIZ);
        if (patterns != null) {
            for (String pattern : patterns) {
                if (normalized.contains(pattern)) return true;
            }
        }
        
        return false;
    }

    /**
     * Check if input is asking for explanation.
     */
    public boolean isExplanationRequest(String input) {
        if (input == null) return false;
        String normalized = input.toLowerCase();
        
        // Check for confusion indicators
        if (normalized.contains("samajh nahi") || 
            normalized.contains("nahi samjha") ||
            normalized.contains("don't understand") ||
            normalized.contains("confused")) {
            return true;
        }
        
        String[] patterns = ACTION_PATTERNS.get(EduIntent.ActionType.EXPLAIN_CONCEPT);
        if (patterns != null) {
            for (String pattern : patterns) {
                if (normalized.contains(pattern)) return true;
            }
        }
        
        return false;
    }
}
