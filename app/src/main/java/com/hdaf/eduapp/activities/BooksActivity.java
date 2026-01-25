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
import com.hdaf.eduapp.adapters.BooksAdapter;
import com.hdaf.eduapp.models.BookModel;
import com.hdaf.eduapp.supabase.ContentRepository;
import com.hdaf.eduapp.utils.Constants;
import com.hdaf.eduapp.utils.PreferenceManager;

import java.util.List;

/**
 * Books selection screen - displays books for selected class.
 * Loads books from Supabase or falls back to sample data.
 */
public class BooksActivity extends AppCompatActivity implements BooksAdapter.OnBookClickListener {

    private RecyclerView booksRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;
    private TextView classBadge;
    private TextView headerTitle;

    private String currentMode;
    private String classId;
    private String className;
    private PreferenceManager prefManager;
    private ContentRepository repository;
    private BooksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        prefManager = PreferenceManager.getInstance(this);
        repository = ContentRepository.getInstance();

        // Get extras from intent
        currentMode = getIntent().getStringExtra(Constants.EXTRA_MODE);
        classId = getIntent().getStringExtra(Constants.EXTRA_CLASS_ID);
        className = getIntent().getStringExtra(Constants.EXTRA_CLASS_NAME);

        initializeViews();
        loadBooks();
    }

    private void initializeViews() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        headerTitle = findViewById(R.id.headerTitle);
        classBadge = findViewById(R.id.classBadge);
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyText = findViewById(R.id.emptyText);

        // Set class badge text
        classBadge.setText(getString(R.string.std_format, className));

        // Setup RecyclerView
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadBooks() {
        loadingProgress.setVisibility(View.VISIBLE);
        booksRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Fetch from Supabase (or local sample data if not configured)
        repository.getBooks(classId, new ContentRepository.DataCallback<List<BookModel>>() {
            @Override
            public void onSuccess(List<BookModel> books) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (books.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        booksRecyclerView.setVisibility(View.VISIBLE);
                        adapter = new BooksAdapter(books, BooksActivity.this);
                        booksRecyclerView.setAdapter(adapter);
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
    public void onBookClick(BookModel book, int position) {
        prefManager.setLastBook(book.getId());

        // Announce for accessibility
        announceForAccessibility(getString(R.string.talkback_book_selected, book.getName()));

        Intent intent = new Intent(this, ChaptersActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, currentMode);
        intent.putExtra(Constants.EXTRA_CLASS_ID, classId);
        intent.putExtra(Constants.EXTRA_CLASS_NAME, className);
        intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());
        intent.putExtra(Constants.EXTRA_BOOK_NAME, book.getName());
        startActivity(intent);
    }

    private void announceForAccessibility(String message) {
        getWindow().getDecorView().announceForAccessibility(message);
    }
}
