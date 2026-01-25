package com.hdaf.eduapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.adapters.ChaptersAdapter;
import com.hdaf.eduapp.models.ChapterModel;
import com.hdaf.eduapp.supabase.ContentRepository;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

import java.util.List;

/**
 * Chapters screen - displays chapters/units for selected book.
 * Loads chapters from Supabase or falls back to sample data.
 */
public class ChaptersActivity extends AppCompatActivity implements ChaptersAdapter.OnChapterClickListener {

    private RecyclerView chaptersRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;
    private TextView headerTitle;
    private TextView chaptersBadge;

    private String currentMode;
    private String classId;
    private String className;
    private String bookId;
    private String bookName;
    private PreferenceManager prefManager;
    private ContentRepository repository;
    private ChaptersAdapter adapter;
    private List<ChapterModel> chapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);

        prefManager = PreferenceManager.getInstance(this);
        repository = ContentRepository.getInstance();

        // Get extras from intent
        currentMode = getIntent().getStringExtra(Constants.EXTRA_MODE);
        classId = getIntent().getStringExtra(Constants.EXTRA_CLASS_ID);
        className = getIntent().getStringExtra(Constants.EXTRA_CLASS_NAME);
        bookId = getIntent().getStringExtra(Constants.EXTRA_BOOK_ID);
        bookName = getIntent().getStringExtra(Constants.EXTRA_BOOK_NAME);

        initializeViews();
        loadChapters();
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        headerTitle = findViewById(R.id.headerTitle);
        chaptersBadge = findViewById(R.id.chaptersBadge);
        chaptersRecyclerView = findViewById(R.id.chaptersRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyText = findViewById(R.id.emptyText);

        // Set header title to book name (simplified)
        if (bookName != null && bookName.contains("ENGLISH")) {
            headerTitle.setText("ENGLISH");
        } else if (bookName != null) {
            headerTitle.setText(bookName.length() > 15 ? bookName.substring(0, 15) : bookName);
        }

        // Setup RecyclerView
        chaptersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadChapters() {
        loadingProgress.setVisibility(View.VISIBLE);
        chaptersRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Fetch from Supabase (or local sample data if not configured)
        repository.getChapters(bookId, new ContentRepository.DataCallback<List<ChapterModel>>() {
            @Override
            public void onSuccess(List<ChapterModel> chapterList) {
                runOnUiThread(() -> {
                    chapters = chapterList;
                    loadingProgress.setVisibility(View.GONE);

                    if (chapters.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        chaptersRecyclerView.setVisibility(View.VISIBLE);
                        adapter = new ChaptersAdapter(chapters, ChaptersActivity.this);
                        chaptersRecyclerView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText(getString(R.string.error_loading_content));
                });
            }
        });
    }

    @Override
    public void onChapterClick(ChapterModel chapter, int position) {
        prefManager.setLastChapter(chapter.getId());

        // Announce for accessibility
        announceForAccessibility(getString(R.string.talkback_chapter_selected, chapter.getName()));

        // Navigate based on mode
        Intent intent;
        if (Constants.MODE_VIDEO.equals(currentMode)) {
            intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra(Constants.EXTRA_VIDEO_URL, chapter.getVideoUrl());
        } else {
            intent = new Intent(this, AudioPlayerActivity.class);
            intent.putExtra(Constants.EXTRA_LESSON_CONTENT, chapter.getLessonContent());
        }

        intent.putExtra(Constants.EXTRA_MODE, currentMode);
        intent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        intent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        intent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
        intent.putExtra(Constants.EXTRA_BOOK_NAME, bookName);
        intent.putExtra(Constants.EXTRA_CHAPTER_ID, chapter.getId());
        intent.putExtra(Constants.EXTRA_CHAPTER_NAME, chapter.getName());

        startActivity(intent);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }
}
