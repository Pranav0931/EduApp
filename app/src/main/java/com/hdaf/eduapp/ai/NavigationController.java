package com.hdaf.eduapp.ai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hdaf.eduapp.activities.AudioPlayerActivity;
import com.hdaf.eduapp.activities.BooksActivity;
import com.hdaf.eduapp.activities.ChaptersActivity;
import com.hdaf.eduapp.activities.ClassSelectionActivity;
import com.hdaf.eduapp.activities.QuizActivity;
import com.hdaf.eduapp.utils.Constants;

/**
 * Controller for handling programmatic navigation based on EduIntent.
 * Translates parsed intents into actual app navigation actions.
 */
public class NavigationController {

    private static final String TAG = "NavigationController";

    public boolean navigate(Context context, EduIntent intent) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or intent is null");
            return false;
        }

        EduIntent.ActionType action = intent.getActionType();
        Log.d(TAG, "Navigating with action: " + action);

        switch (action) {
            case OPEN_CLASS:
                return navigateToClass(context, intent);

            case OPEN_SUBJECT:
                return navigateToSubject(context, intent);

            case OPEN_CHAPTER:
                return navigateToChapter(context, intent);

            case PLAY_AUDIO:
                return playChapterAudio(context, intent);

            case START_QUIZ:
                return startQuiz(context, intent);

            case NAVIGATION:
                return handleGenericNavigation(context, intent);

            default:
                Log.w(TAG, "No navigation handler for action: " + action);
                return false;
        }
    }

    private boolean navigateToClass(Context context, EduIntent intent) {
        Intent navIntent = new Intent(context, ClassSelectionActivity.class);
        navIntent.putExtra(Constants.EXTRA_MODE, Constants.MODE_AUDIO);
        navIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(navIntent);
        return true;
    }

    private boolean navigateToSubject(Context context, EduIntent intent) {
        if (!intent.hasClass()) return false;

        String classId = "class_" + intent.getClassNumber();
        String className = getClassDisplayName(intent.getClassNumberInt());

        Intent navIntent = new Intent(context, BooksActivity.class);
        navIntent.putExtra(Constants.EXTRA_MODE, Constants.MODE_AUDIO);
        navIntent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        navIntent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        navIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(navIntent);

        return true;
    }

    private boolean navigateToChapter(Context context, EduIntent intent) {
        if (!intent.hasClass() || !intent.hasSubject()) return false;

        String classId = "class_" + intent.getClassNumber();
        String className = getClassDisplayName(intent.getClassNumberInt());
        String bookId = generateBookId(intent.getClassNumberInt(), intent.getSubject());
        String bookName = intent.getSubject().toUpperCase();

        Intent navIntent = new Intent(context, ChaptersActivity.class);
        navIntent.putExtra(Constants.EXTRA_MODE, Constants.MODE_AUDIO);
        navIntent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        navIntent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        navIntent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
        navIntent.putExtra(Constants.EXTRA_BOOK_NAME, bookName);
        navIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(navIntent);

        return true;
    }

    private boolean playChapterAudio(Context context, EduIntent intent) {
        if (!intent.hasClass() || !intent.hasSubject()) return false;

        String classId = "class_" + intent.getClassNumber();
        String className = getClassDisplayName(intent.getClassNumberInt());
        String bookId = generateBookId(intent.getClassNumberInt(), intent.getSubject());
        String bookName = intent.getSubject().toUpperCase();

        String chapterId = intent.hasChapter()
                ? generateChapterId(bookId, intent.getChapterNumberInt())
                : null;

        String chapterName = intent.hasChapter()
                ? "UNIT " + intent.getChapterNumber()
                : "UNIT 1";

        Intent navIntent = new Intent(context, AudioPlayerActivity.class);
        navIntent.putExtra(Constants.EXTRA_MODE, Constants.MODE_AUDIO);
        navIntent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        navIntent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        navIntent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
        navIntent.putExtra(Constants.EXTRA_BOOK_NAME, bookName);

        if (chapterId != null) {
            navIntent.putExtra(Constants.EXTRA_CHAPTER_ID, chapterId);
        }

        navIntent.putExtra(Constants.EXTRA_CHAPTER_NAME, chapterName);
        navIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(navIntent);

        return true;
    }

    private boolean startQuiz(Context context, EduIntent intent) {
        Intent quizIntent = new Intent(context, QuizActivity.class);

        if (intent.hasSubject()) {
            quizIntent.putExtra("extra_subject", intent.getSubject());
        }

        if (intent.hasChapter()) {
            quizIntent.putExtra("extra_chapter", "Chapter " + intent.getChapterNumber());
        } else if (intent.hasTopic()) {
            quizIntent.putExtra("extra_topic", intent.getTopic());
        }

        quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(quizIntent);

        return true;
    }

    private boolean handleGenericNavigation(Context context, EduIntent intent) {
        if (intent.hasChapter() && intent.hasSubject() && intent.hasClass()) {
            return navigateToChapter(context, intent);
        } else if (intent.hasSubject() && intent.hasClass()) {
            return navigateToChapter(context, intent);
        } else if (intent.hasClass()) {
            return navigateToSubject(context, intent);
        } else {
            return navigateToClass(context, intent);
        }
    }

    private String getClassDisplayName(int classNumber) {
        switch (classNumber) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            default: return classNumber + "th";
        }
    }

    private String generateBookId(int classNumber, String subject) {
        String subjectKey = subject.toLowerCase()
                .replace(" ", "_")
                .replace("social science", "sst");
        return "book_" + classNumber + "_" + subjectKey;
    }

    private String generateChapterId(String bookId, int chapterNumber) {
        return bookId + "_chapter_" + chapterNumber;
    }

    public String getNavigationMessage(EduIntent intent) {
        EduIntent.ActionType action = intent.getActionType();
        StringBuilder message = new StringBuilder();

        switch (action) {
            case OPEN_CLASS:
            case OPEN_SUBJECT:
                if (intent.hasClass()) {
                    message.append("Main ")
                            .append(getClassDisplayName(intent.getClassNumberInt()))
                            .append(" class khol raha hoon");
                } else {
                    message.append("Class selection khol raha hoon");
                }
                break;

            case OPEN_CHAPTER:
                message.append("Sure! Main ");
                if (intent.hasClass()) {
                    message.append(getClassDisplayName(intent.getClassNumberInt())).append(" class ka ");
                }
                if (intent.hasSubject()) {
                    message.append(intent.getSubject()).append(" ka ");
                }
                if (intent.hasChapter()) {
                    message.append("chapter ").append(intent.getChapterNumber()).append(" ");
                }
                message.append("khol raha hoon 📚✨");
                break;

            case PLAY_AUDIO:
                message.append("Done! Main ab audio lesson play kar raha hoon 🎧");
                break;

            case START_QUIZ:
                message.append("Great! Chalo quiz start karte hain 😄📝");
                break;

            default:
                message.append("Navigating...");
        }

        return message.toString();
    }
}
