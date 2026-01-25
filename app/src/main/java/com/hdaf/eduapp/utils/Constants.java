package com.hdaf.eduapp.utils;

/**
 * Application-wide constants
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // Intent extras
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_CLASS_ID = "extra_class_id";
    public static final String EXTRA_CLASS_NAME = "extra_class_name";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_BOOK_NAME = "extra_book_name";
    public static final String EXTRA_CHAPTER_ID = "extra_chapter_id";
    public static final String EXTRA_CHAPTER_NAME = "extra_chapter_name";
    public static final String EXTRA_LESSON_CONTENT = "extra_lesson_content";
    public static final String EXTRA_VIDEO_URL = "extra_video_url";

    // Learning modes
    public static final String MODE_AUDIO = "audio";
    public static final String MODE_VIDEO = "video";

    // Shared preferences
    public static final String PREF_NAME = "eduapp_prefs";
    public static final String PREF_LAST_MODE = "last_mode";
    public static final String PREF_TTS_SPEED = "tts_speed";
    public static final String PREF_LAST_CLASS = "last_class";
    public static final String PREF_LAST_BOOK = "last_book";
    public static final String PREF_LAST_CHAPTER = "last_chapter";

    // Firebase collections
    public static final String COLLECTION_CLASSES = "classes";
    public static final String COLLECTION_BOOKS = "books";
    public static final String COLLECTION_CHAPTERS = "chapters";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PROGRESS = "progress";

    // Default values
    public static final float DEFAULT_TTS_SPEED = 1.0f;
    public static final float MIN_TTS_SPEED = 0.5f;
    public static final float MAX_TTS_SPEED = 2.0f;

    // Animation durations
    public static final int SPLASH_DURATION_MS = 2000;
    public static final int ANIMATION_DURATION_MS = 300;

    // Voice commands
    public static final String VOICE_CMD_PLAY = "play";
    public static final String VOICE_CMD_PAUSE = "pause";
    public static final String VOICE_CMD_STOP = "stop";
    public static final String VOICE_CMD_NEXT = "next";
    public static final String VOICE_CMD_PREVIOUS = "previous";
    public static final String VOICE_CMD_BACK = "back";
    public static final String VOICE_CMD_GO_BACK = "go back";
    public static final String VOICE_CMD_REPEAT = "repeat";
}
