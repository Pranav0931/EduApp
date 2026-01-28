package com.hdaf.eduapp.ai;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Model class representing a parsed intent from user input.
 * Contains class, subject, chapter information and the action to perform.
 */
public class EduIntent {

    public enum ActionType {
        @SerializedName("OPEN_CLASS")
        OPEN_CLASS,
        
        @SerializedName("OPEN_SUBJECT")
        OPEN_SUBJECT,
        
        @SerializedName("OPEN_CHAPTER")
        OPEN_CHAPTER,
        
        @SerializedName("PLAY_AUDIO")
        PLAY_AUDIO,
        
        @SerializedName("START_QUIZ")
        START_QUIZ,
        
        @SerializedName("EXPLAIN_CONCEPT")
        EXPLAIN_CONCEPT,
        
        @SerializedName("NAVIGATION")
        NAVIGATION,
        
        @SerializedName("CHAT")
        CHAT,
        
        @SerializedName("DAILY_CHALLENGE")
        DAILY_CHALLENGE,
        
        @SerializedName("STUDY_PLAN")
        STUDY_PLAN,
        
        @SerializedName("UNKNOWN")
        UNKNOWN
    }

    @SerializedName("intent")
    private String intentType;
    
    @SerializedName("class")
    private String classNumber;
    
    @SerializedName("subject")
    private String subject;
    
    @SerializedName("chapter")
    private String chapterNumber;
    
    @SerializedName("action")
    private String action;
    
    @SerializedName("topic")
    private String topic;
    
    private String rawQuery;
    private ActionType actionType;

    public EduIntent() {
        this.actionType = ActionType.UNKNOWN;
    }

    // Builder pattern for easy construction
    public static class Builder {
        private EduIntent intent;

        public Builder() {
            intent = new EduIntent();
        }

        public Builder setClassNumber(String classNumber) {
            intent.classNumber = classNumber;
            return this;
        }

        public Builder setClassNumber(int classNumber) {
            intent.classNumber = String.valueOf(classNumber);
            return this;
        }

        public Builder setSubject(String subject) {
            intent.subject = subject;
            return this;
        }

        public Builder setChapterNumber(String chapterNumber) {
            intent.chapterNumber = chapterNumber;
            return this;
        }

        public Builder setChapterNumber(int chapterNumber) {
            intent.chapterNumber = String.valueOf(chapterNumber);
            return this;
        }

        public Builder setAction(ActionType action) {
            intent.actionType = action;
            intent.action = action.name();
            return this;
        }

        public Builder setTopic(String topic) {
            intent.topic = topic;
            return this;
        }

        public Builder setRawQuery(String rawQuery) {
            intent.rawQuery = rawQuery;
            return this;
        }

        public EduIntent build() {
            return intent;
        }
    }

    // Getters
    public String getClassNumber() {
        return classNumber;
    }

    public int getClassNumberInt() {
        try {
            return Integer.parseInt(classNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getSubject() {
        return subject;
    }

    public String getChapterNumber() {
        return chapterNumber;
    }

    public int getChapterNumberInt() {
        try {
            return Integer.parseInt(chapterNumber);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public ActionType getActionType() {
        if (actionType == null && action != null) {
            try {
                actionType = ActionType.valueOf(action);
            } catch (IllegalArgumentException e) {
                actionType = ActionType.UNKNOWN;
            }
        }
        return actionType != null ? actionType : ActionType.UNKNOWN;
    }

    public String getTopic() {
        return topic;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    // Validation methods
    public boolean hasClass() {
        return classNumber != null && !classNumber.isEmpty();
    }

    public boolean hasSubject() {
        return subject != null && !subject.isEmpty();
    }

    public boolean hasChapter() {
        return chapterNumber != null && !chapterNumber.isEmpty();
    }

    public boolean hasTopic() {
        return topic != null && !topic.isEmpty();
    }

    public boolean isNavigationIntent() {
        ActionType type = getActionType();
        return type == ActionType.OPEN_CLASS ||
               type == ActionType.OPEN_SUBJECT ||
               type == ActionType.OPEN_CHAPTER ||
               type == ActionType.PLAY_AUDIO ||
               type == ActionType.NAVIGATION ||
               type == ActionType.START_QUIZ ||
               type == ActionType.DAILY_CHALLENGE;
    }

    public boolean isQuizIntent() {
        return getActionType() == ActionType.START_QUIZ ||
               getActionType() == ActionType.DAILY_CHALLENGE;
    }

    public boolean isExplanationIntent() {
        return getActionType() == ActionType.EXPLAIN_CONCEPT;
    }

    /**
     * Convert intent to JSON for structured output
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Parse intent from JSON string
     */
    public static EduIntent fromJson(String json) {
        try {
            Gson gson = new Gson();
            EduIntent intent = gson.fromJson(json, EduIntent.class);
            if (intent != null && intent.action != null) {
                try {
                    intent.actionType = ActionType.valueOf(intent.action);
                } catch (IllegalArgumentException e) {
                    intent.actionType = ActionType.UNKNOWN;
                }
            }
            return intent;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "EduIntent{" +
                "classNumber='" + classNumber + '\'' +
                ", subject='" + subject + '\'' +
                ", chapterNumber='" + chapterNumber + '\'' +
                ", action=" + getActionType() +
                ", topic='" + topic + '\'' +
                '}';
    }
}
