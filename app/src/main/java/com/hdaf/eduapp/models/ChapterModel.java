package com.hdaf.eduapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chapter/unit within a book.
 * Gson annotations map to Supabase table column names.
 */
public class ChapterModel {

    @SerializedName("id")
    private String id;

    @SerializedName("book_id")
    private String bookId;

    @SerializedName("name")
    private String name;

    @SerializedName("order_index")
    private int orderIndex;

    @SerializedName("audio_url")
    private String audioUrl;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("transcript")
    private String transcript;

    @SerializedName("lesson_content")
    private String lessonContent;

    public ChapterModel() {
        // Required empty constructor for Gson
    }

    public ChapterModel(String id, String bookId, String name, int orderIndex) {
        this.id = id;
        this.bookId = bookId;
        this.name = name;
        this.orderIndex = orderIndex;
    }

    public ChapterModel(String id, String bookId, String name, int orderIndex,
            String audioUrl, String videoUrl, String transcript, String lessonContent) {
        this.id = id;
        this.bookId = bookId;
        this.name = name;
        this.orderIndex = orderIndex;
        this.audioUrl = audioUrl;
        this.videoUrl = videoUrl;
        this.transcript = transcript;
        this.lessonContent = lessonContent;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getLessonContent() {
        return lessonContent;
    }

    public void setLessonContent(String lessonContent) {
        this.lessonContent = lessonContent;
    }

    /**
     * Returns sample chapters for a given book (mock data for testing)
     */
    public static List<ChapterModel> getSampleChapters(String bookId) {
        List<ChapterModel> chapters = new ArrayList<>();

        // Sample lesson content for TTS
        String[] lessonContents = {
                "Welcome to Unit 1. In this lesson, we will learn about basic greetings and introductions. " +
                        "Hello means a friendly way to greet someone. Good morning is used in the early part of the day.",

                "Unit 2 covers family members and relationships. " +
                        "Mother, Father, Brother, Sister are important family words. " +
                        "My family is special to me.",

                "In Unit 3, we explore colors and shapes. " +
                        "Red, Blue, Green, Yellow are primary colors. " +
                        "Circle, Square, Triangle are basic shapes.",

                "Unit 4 teaches us about animals and their sounds. " +
                        "Dogs bark, cats meow, and birds chirp. " +
                        "Animals are our friends.",

                "Unit 5 is about numbers and counting. " +
                        "One, Two, Three, Four, Five. " +
                        "Counting helps us in everyday life.",

                "Unit 6 covers the days of the week. " +
                        "Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday. " +
                        "There are seven days in a week.",

                "Unit 7 teaches months and seasons. " +
                        "January, February, March are the first three months. " +
                        "There are four seasons: Spring, Summer, Autumn, and Winter.",

                "Unit 8 is our final unit about community helpers. " +
                        "Doctors help us stay healthy. Teachers help us learn. " +
                        "Police officers keep us safe."
        };

        for (int i = 1; i <= 8; i++) {
            ChapterModel chapter = new ChapterModel(
                    "chapter_" + bookId + "_" + i,
                    bookId,
                    "UNIT " + i,
                    i,
                    null, // audioUrl - would be Supabase Storage URL in production
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", // sample
                                                                                                          // video
                    lessonContents[i - 1], // transcript
                    lessonContents[i - 1] // lesson content for TTS
            );
            chapters.add(chapter);
        }

        return chapters;
    }
}
