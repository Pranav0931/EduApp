package com.hdaf.eduapp.supabase;

import com.hdaf.eduapp.models.BookModel;
import com.hdaf.eduapp.models.ChapterModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Supabase REST API interface.
 * Defines endpoints for accessing books and chapters data.
 * 
 * Supabase uses PostgREST which provides a RESTful API for PostgreSQL.
 * Query parameters follow PostgREST syntax.
 */
public interface SupabaseApi {

    /**
     * Get all books for a specific class.
     * Example: /books?class_id=eq.class_1&order=order_index.asc
     */
    @GET("books")
    Call<List<BookModel>> getBooksByClass(
            @Query("class_id") String classIdFilter,
            @Query("order") String orderBy);

    /**
     * Get all chapters for a specific book.
     * Example: /chapters?book_id=eq.book_1&order=order_index.asc
     */
    @GET("chapters")
    Call<List<ChapterModel>> getChaptersByBook(
            @Query("book_id") String bookIdFilter,
            @Query("order") String orderBy);

    /**
     * Get a single chapter by ID.
     * Example: /chapters?id=eq.chapter_1
     */
    @GET("chapters")
    Call<List<ChapterModel>> getChapterById(
            @Query("id") String chapterIdFilter);

    /**
     * Get all books (no filter).
     */
    @GET("books")
    Call<List<BookModel>> getAllBooks(
            @Query("order") String orderBy);

    /**
     * Get all chapters (no filter).
     */
    @GET("chapters")
    Call<List<ChapterModel>> getAllChapters(
            @Query("order") String orderBy);

    /**
     * Upsert user profile (Insert or Update).
     * Uses the "Prefer: resolution=merge-duplicates" header to handle upsert.
     */
    @retrofit2.http.POST("profiles")
    @retrofit2.http.Headers("Prefer: resolution=merge-duplicates")
    Call<Void> upsertProfile(@retrofit2.http.Body com.hdaf.eduapp.supabase.models.ProfileModel profile);

    /**
     * Insert a new analytics log.
     */
    @retrofit2.http.POST("analytics_logs")
    Call<Void> createAnalyticsLog(@retrofit2.http.Body com.hdaf.eduapp.supabase.models.AnalyticsLogModel log);
}
