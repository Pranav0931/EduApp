package com.hdaf.eduapp.data.remote.api

import com.hdaf.eduapp.data.remote.dto.AuthResponse
import com.hdaf.eduapp.data.remote.dto.LoginRequest
import com.hdaf.eduapp.data.remote.dto.OtpVerifyRequest
import com.hdaf.eduapp.data.remote.dto.RefreshTokenRequest
import com.hdaf.eduapp.data.remote.dto.RegisterRequest
import com.hdaf.eduapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit API interface for Authentication endpoints.
 */
interface AuthApi {

    /**
     * Register a new user.
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    /**
     * Login with email/password.
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * Request OTP for phone login.
     */
    @POST("auth/otp/request")
    suspend fun requestOtp(
        @Body request: Map<String, String> // {"phone": "+91XXXXXXXXXX"}
    ): Response<Map<String, String>>

    /**
     * Verify OTP and login.
     */
    @POST("auth/otp/verify")
    suspend fun verifyOtp(
        @Body request: OtpVerifyRequest
    ): Response<AuthResponse>

    /**
     * Refresh access token.
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    /**
     * Logout and invalidate tokens.
     */
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authToken: String
    ): Response<Unit>

    /**
     * Get current user profile.
     */
    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authToken: String
    ): Response<UserDto>

    /**
     * Update user profile.
     */
    @PUT("auth/me")
    suspend fun updateProfile(
        @Header("Authorization") authToken: String,
        @Body updates: Map<String, Any>
    ): Response<UserDto>

    /**
     * Delete user account.
     */
    @POST("auth/delete-account")
    suspend fun deleteAccount(
        @Header("Authorization") authToken: String
    ): Response<Unit>

    /**
     * Link parent account.
     */
    @POST("auth/link-parent")
    suspend fun linkParent(
        @Header("Authorization") authToken: String,
        @Body request: Map<String, String>
    ): Response<Unit>
}
