package com.hdaf.eduapp.supabase;

import android.util.Log;

import com.hdaf.eduapp.models.BookModel;
import com.hdaf.eduapp.models.ChapterModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for fetching educational content.
 * Uses Supabase when configured, falls back to local sample data otherwise.
 */
public class ContentRepository {

    private static final String TAG = "ContentRepository";
    private static ContentRepository instance;
    private final SupabaseApi api;
    private final boolean useSupabase;

    public interface DataCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }

    private ContentRepository() {
        useSupabase = SupabaseClient.isConfigured();
        api = SupabaseClient.getInstance().getApi();

        if (useSupabase) {
            Log.d(TAG, "Using Supabase for data");
        } else {
            Log.d(TAG, "Supabase not configured, using local sample data");
        }
    }

    public static synchronized ContentRepository getInstance() {
        if (instance == null) {
            instance = new ContentRepository();
        }
        return instance;
    }

    /**
     * Fetch books for a given class.
     */
    public void getBooks(String classId, DataCallback<List<BookModel>> callback) {
        if (!useSupabase) {
            // Return sample data
            callback.onSuccess(BookModel.getSampleBooks(classId));
            return;
        }

        // Fetch from Supabase
        // PostgREST filter format: eq.value
        String filter = "eq." + classId;
        api.getBooksByClass(filter, "order_index.asc").enqueue(new Callback<List<BookModel>>() {
            @Override
            public void onResponse(Call<List<BookModel>> call, Response<List<BookModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookModel> books = response.body();
                    if (books.isEmpty()) {
                        // Fall back to sample data if no data in Supabase
                        callback.onSuccess(BookModel.getSampleBooks(classId));
                    } else {
                        callback.onSuccess(books);
                    }
                } else {
                    Log.e(TAG, "Error fetching books: " + response.code());
                    // Fall back to sample data on error
                    callback.onSuccess(BookModel.getSampleBooks(classId));
                }
            }

            @Override
            public void onFailure(Call<List<BookModel>> call, Throwable t) {
                Log.e(TAG, "Network error fetching books", t);
                // Fall back to sample data on network error
                callback.onSuccess(BookModel.getSampleBooks(classId));
            }
        });
    }

    /**
     * Fetch chapters for a given book.
     */
    public void getChapters(String bookId, DataCallback<List<ChapterModel>> callback) {
        if (!useSupabase) {
            // Return sample data
            callback.onSuccess(ChapterModel.getSampleChapters(bookId));
            return;
        }

        // Fetch from Supabase
        String filter = "eq." + bookId;
        api.getChaptersByBook(filter, "order_index.asc").enqueue(new Callback<List<ChapterModel>>() {
            @Override
            public void onResponse(Call<List<ChapterModel>> call, Response<List<ChapterModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChapterModel> chapters = response.body();
                    if (chapters.isEmpty()) {
                        // Fall back to sample data
                        callback.onSuccess(ChapterModel.getSampleChapters(bookId));
                    } else {
                        callback.onSuccess(chapters);
                    }
                } else {
                    Log.e(TAG, "Error fetching chapters: " + response.code());
                    callback.onSuccess(ChapterModel.getSampleChapters(bookId));
                }
            }

            @Override
            public void onFailure(Call<List<ChapterModel>> call, Throwable t) {
                Log.e(TAG, "Network error fetching chapters", t);
                callback.onSuccess(ChapterModel.getSampleChapters(bookId));
            }
        });
    }

    /**
     * Get a single chapter by ID.
     */
    public void getChapter(String chapterId, DataCallback<ChapterModel> callback) {
        if (!useSupabase) {
            // Return null - caller should handle
            callback.onError("Supabase not configured");
            return;
        }

        String filter = "eq." + chapterId;
        api.getChapterById(filter).enqueue(new Callback<List<ChapterModel>>() {
            @Override
            public void onResponse(Call<List<ChapterModel>> call, Response<List<ChapterModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Chapter not found");
                }
            }

            @Override
            public void onFailure(Call<List<ChapterModel>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Check if Supabase is being used.
     */
    public boolean isUsingSupabase() {
        return useSupabase;
    }
}
