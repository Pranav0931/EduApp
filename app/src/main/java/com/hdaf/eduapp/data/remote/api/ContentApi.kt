package com.hdaf.eduapp.data.remote.api

import com.hdaf.eduapp.data.remote.dto.BookDto
import com.hdaf.eduapp.data.remote.dto.ChapterDto
import com.hdaf.eduapp.data.remote.dto.ChapterProgressUpdateDto
import com.hdaf.eduapp.data.remote.dto.LeaderboardEntryDto
import com.hdaf.eduapp.data.remote.dto.UserProgressDto
import com.hdaf.eduapp.data.remote.dto.UserRankDto
import com.hdaf.eduapp.data.remote.dto.XpUpdateDto
import com.hdaf.eduapp.data.remote.dto.XpUpdateResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Content endpoints (Supabase).
 */
interface ContentApi {

    // ==================== Books ====================

    /**
     * Get books by class ID.
     * 
     * Uses Supabase PostgREST filter syntax.
     */
    @GET("books")
    suspend fun getBooksByClass(
        @Query("class_id") classIdFilter: String, // eq.class_5
        @Query("order") order: String = "order_index.asc",
        @Query("is_active") isActive: String = "eq.true"
    ): Response<List<BookDto>>

    /**
     * Get a single book by ID.
     */
    @GET("books")
    suspend fun getBookById(
        @Query("id") idFilter: String, // eq.book_id
        @Query("limit") limit: Int = 1
    ): Response<List<BookDto>>

    /**
     * Get all books.
     */
    @GET("books")
    suspend fun getAllBooks(
        @Query("order") order: String = "class_id.asc,order_index.asc",
        @Query("is_active") isActive: String = "eq.true"
    ): Response<List<BookDto>>

    // ==================== Chapters ====================

    /**
     * Get chapters by book ID.
     */
    @GET("chapters")
    suspend fun getChaptersByBook(
        @Query("book_id") bookIdFilter: String, // eq.book_id
        @Query("order") order: String = "order_index.asc",
        @Query("is_active") isActive: String = "eq.true"
    ): Response<List<ChapterDto>>

    /**
     * Get a single chapter by ID.
     */
    @GET("chapters")
    suspend fun getChapterById(
        @Query("id") idFilter: String // eq.chapter_id
    ): Response<List<ChapterDto>>

    /**
     * Search chapters by title.
     */
    @GET("chapters")
    suspend fun searchChapters(
        @Query("title") titleFilter: String, // ilike.*search_term*
        @Query("is_active") isActive: String = "eq.true",
        @Query("limit") limit: Int = 20
    ): Response<List<ChapterDto>>

    /**
     * Get full chapter content by ID.
     */
    @GET("chapters")
    suspend fun getChapterContent(
        @Query("id") idFilter: String,
        @Query("select") select: String = "*,content"
    ): Response<List<ChapterDto>>

    /**
     * Update chapter progress.
     */
    @POST("rpc/update_chapter_progress")
    suspend fun updateChapterProgress(
        @Body progressUpdate: ChapterProgressUpdateDto
    ): Response<Unit>

    // ==================== User Progress ====================

    /**
     * Get user progress by user ID.
     */
    @GET("user_progress/{userId}")
    suspend fun getUserProgress(
        @Path("userId") userId: String
    ): UserProgressDto

    /**
     * Add XP to user.
     */
    @POST("rpc/add_xp")
    suspend fun addXp(
        @Body xpUpdate: XpUpdateDto
    ): XpUpdateResponseDto

    // ==================== Leaderboard ====================

    /**
     * Get leaderboard entries.
     */
    @GET("rpc/get_leaderboard")
    suspend fun getLeaderboard(
        @Query("scope") scope: String, // "global", "weekly", "monthly"
        @Query("limit") limit: Int = 100
    ): List<LeaderboardEntryDto>

    /**
     * Get user's rank information.
     */
    @GET("rpc/get_user_rank")
    suspend fun getUserRank(
        @Query("user_id") userId: String
    ): UserRankDto
}
