package com.hdaf.eduapp.domain.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.User
import com.hdaf.eduapp.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Implemented by AuthRepositoryImpl in data layer.
 */
interface AuthRepository {

    /**
     * Register a new user.
     */
    suspend fun register(
        name: String,
        phone: String,
        classLevel: Int,
        medium: String,
        accessibilityMode: String
    ): Resource<UserSession>

    /**
     * Login with phone and OTP.
     */
    suspend fun login(phone: String, otp: String): Resource<UserSession>

    /**
     * Request OTP for phone verification.
     */
    suspend fun requestOtp(phone: String): Resource<Boolean>

    /**
     * Verify OTP without logging in.
     */
    suspend fun verifyOtp(phone: String, otp: String): Resource<Boolean>

    /**
     * Refresh the access token.
     */
    suspend fun refreshToken(refreshToken: String): Resource<UserSession>

    /**
     * Logout the current user.
     */
    suspend fun logout(): Resource<Unit>

    /**
     * Get the current user session.
     */
    fun getCurrentSession(): Flow<UserSession?>

    /**
     * Check if user is logged in (Flow version).
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * Check if user is currently logged in (suspend version).
     */
    suspend fun isUserLoggedIn(): Boolean

    /**
     * Get the current user.
     */
    suspend fun getCurrentUser(): Resource<User>

    /**
     * Update user profile.
     */
    suspend fun updateProfile(user: User): Resource<User>

    /**
     * Link parent account to student.
     */
    suspend fun linkParentAccount(parentPhone: String): Resource<Boolean>

    /**
     * Delete user account.
     */
    suspend fun deleteAccount(): Resource<Unit>

    /**
     * Check if user has completed onboarding.
     */
    suspend fun hasCompletedOnboarding(): Boolean

    /**
     * Set onboarding completion status.
     */
    suspend fun setOnboardingCompleted(completed: Boolean)

    /**
     * Save user's preferred accessibility mode.
     */
    suspend fun saveAccessibilityMode(mode: String)

    /**
     * Get user's preferred accessibility mode.
     */
    suspend fun getAccessibilityMode(): String?
}
