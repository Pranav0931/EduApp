package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.core.security.SecurePreferences
import com.hdaf.eduapp.data.local.dao.UserProgressDao
import com.hdaf.eduapp.data.mapper.toDomain
import com.hdaf.eduapp.data.remote.api.AuthApi
import com.hdaf.eduapp.data.remote.dto.LoginRequest
import com.hdaf.eduapp.data.remote.dto.OtpVerifyRequest
import com.hdaf.eduapp.data.remote.dto.RefreshTokenRequest
import com.hdaf.eduapp.data.remote.dto.RegisterRequest
import com.hdaf.eduapp.domain.model.AccessibilityMode
import com.hdaf.eduapp.domain.model.User
import com.hdaf.eduapp.domain.model.UserRole
import com.hdaf.eduapp.domain.model.UserSession
import com.hdaf.eduapp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository.
 * Handles authentication with Supabase and local session management.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val securePreferences: SecurePreferences,
    private val networkMonitor: NetworkMonitor,
    private val userProgressDao: UserProgressDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_CLASS = "user_class"
        private const val KEY_USER_MEDIUM = "user_medium"
        private const val KEY_ACCESSIBILITY_MODE = "accessibility_mode"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override suspend fun register(
        name: String,
        phone: String,
        classLevel: Int,
        medium: String,
        accessibilityMode: String
    ): Resource<UserSession> = withContext(ioDispatcher) {
        try {
            val request = RegisterRequest(
                email = "${phone}@eduapp.local", // Use phone as email base for now
                password = phone, // Temporary - should be proper auth
                name = name,
                phone = phone,
                classLevel = classLevel
            )
            
            val response = authApi.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val user = authResponse.user.toDomain()
                val session = UserSession(
                    user = user,
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                )
                
                // Save session securely
                saveSession(session)
                
                // Save medium and accessibility mode separately
                securePreferences.putString(KEY_USER_MEDIUM, medium)
                securePreferences.putString(KEY_ACCESSIBILITY_MODE, accessibilityMode)
                
                Resource.Success(session)
            } else {
                Resource.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Resource.Error("Registration failed: ${e.message}")
        }
    }

    override suspend fun login(phone: String, otp: String): Resource<UserSession> = withContext(ioDispatcher) {
        try {
            val request = LoginRequest(
                email = "${phone}@eduapp.local",
                password = otp
            )
            val response = authApi.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val user = authResponse.user.toDomain()
                val session = UserSession(
                    user = user,
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                )
                
                saveSession(session)
                Resource.Success(session)
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Resource.Error("Login failed: ${e.message}")
        }
    }

    override suspend fun requestOtp(phone: String): Resource<Boolean> = withContext(ioDispatcher) {
        try {
            val request = mapOf("phone" to phone)
            val response = authApi.requestOtp(request)
            
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to send OTP: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "OTP request error")
            Resource.Error("Failed to send OTP: ${e.message}")
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Resource<Boolean> = withContext(ioDispatcher) {
        try {
            val request = OtpVerifyRequest(phone = phone, otp = otp)
            val response = authApi.verifyOtp(request)
            
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(true)
            } else {
                Resource.Error("OTP verification failed")
            }
        } catch (e: Exception) {
            Timber.e(e, "OTP verification error")
            Resource.Error("OTP verification failed: ${e.message}")
        }
    }

    override suspend fun refreshToken(refreshToken: String): Resource<UserSession> = withContext(ioDispatcher) {
        try {
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val response = authApi.refreshToken(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                val user = authResponse.user.toDomain()
                val session = UserSession(
                    user = user,
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                )
                
                saveSession(session)
                Resource.Success(session)
            } else {
                // Clear session on refresh failure
                clearSession()
                Resource.Error("Session expired, please login again")
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh error")
            clearSession()
            Resource.Error("Session expired: ${e.message}")
        }
    }

    override suspend fun logout(): Resource<Unit> = withContext(ioDispatcher) {
        try {
            val token = securePreferences.getString(KEY_ACCESS_TOKEN)
            
            // Try to logout on server
            if (token != null) {
                try {
                    authApi.logout("Bearer $token")
                } catch (e: Exception) {
                    Timber.w(e, "Server logout failed, clearing local session")
                }
            }
            
            // Always clear local session
            clearSession()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            clearSession()
            Resource.Success(Unit) // Still consider it success
        }
    }

    override fun getCurrentSession(): Flow<UserSession?> = flow {
        val accessToken = securePreferences.getString(KEY_ACCESS_TOKEN)
        val refreshToken = securePreferences.getString(KEY_REFRESH_TOKEN)
        val expiresAt = securePreferences.getLong(KEY_TOKEN_EXPIRY)
        
        if (accessToken != null && refreshToken != null) {
            val user = loadUserFromPrefs()
            if (user != null) {
                val session = UserSession(
                    user = user,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt
                )
                
                // Check if token needs refresh
                if (session.isExpiringSoon && networkMonitor.isConnectedNow()) {
                    when (val refreshResult = refreshToken(refreshToken)) {
                        is Resource.Success -> emit(refreshResult.data)
                        is Resource.Error -> emit(null)
                        is Resource.Loading -> emit(session)
                    }
                } else {
                    emit(session)
                }
            } else {
                emit(null)
            }
        } else {
            emit(null)
        }
    }.flowOn(ioDispatcher)

    override fun isLoggedIn(): Flow<Boolean> {
        return getCurrentSession().map { it != null }
    }

    override suspend fun getCurrentUser(): Resource<User> = withContext(ioDispatcher) {
        try {
            val token = securePreferences.getString(KEY_ACCESS_TOKEN)
                ?: return@withContext Resource.Error("Not logged in")
            
            val response = authApi.getCurrentUser("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomain()
                // Update local cache
                saveUserToPrefs(user)
                Resource.Success(user)
            } else {
                // Return cached user
                val cachedUser = loadUserFromPrefs()
                if (cachedUser != null) {
                    Resource.Success(cachedUser)
                } else {
                    Resource.Error("Failed to get user: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Get current user error")
            val cachedUser = loadUserFromPrefs()
            if (cachedUser != null) {
                Resource.Success(cachedUser)
            } else {
                Resource.Error("Failed to get user: ${e.message}")
            }
        }
    }

    override suspend fun updateProfile(user: User): Resource<User> = withContext(ioDispatcher) {
        try {
            val token = securePreferences.getString(KEY_ACCESS_TOKEN)
                ?: return@withContext Resource.Error("Not logged in")
            
            // Update user on server
            // val response = authApi.updateProfile("Bearer $token", userDto)
            
            // Update local cache
            saveUserToPrefs(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Update profile error")
            Resource.Error("Failed to update profile: ${e.message}")
        }
    }

    override suspend fun linkParentAccount(parentPhone: String): Resource<Boolean> = withContext(ioDispatcher) {
        try {
            val token = securePreferences.getString(KEY_ACCESS_TOKEN)
                ?: return@withContext Resource.Error("Not logged in")
            
            val response = authApi.linkParent("Bearer $token", mapOf("parent_phone" to parentPhone))
            
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to link parent: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Link parent error")
            Resource.Error("Failed to link parent: ${e.message}")
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> = withContext(ioDispatcher) {
        try {
            val token = securePreferences.getString(KEY_ACCESS_TOKEN)
                ?: return@withContext Resource.Error("Not logged in")
            
            val response = authApi.deleteAccount("Bearer $token")
            
            if (response.isSuccessful) {
                clearSession()
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to delete account: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Delete account error")
            Resource.Error("Failed to delete account: ${e.message}")
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return securePreferences.getString(KEY_ACCESS_TOKEN) != null
    }

    override suspend fun hasCompletedOnboarding(): Boolean {
        return securePreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        securePreferences.setBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }

    override suspend fun saveAccessibilityMode(mode: String) {
        securePreferences.putString(KEY_ACCESSIBILITY_MODE, mode)
    }

    override suspend fun getAccessibilityMode(): String? {
        return securePreferences.getString(KEY_ACCESSIBILITY_MODE)
    }

    // ==================== Private Helpers ====================

    private fun saveSession(session: UserSession) {
        securePreferences.putString(KEY_ACCESS_TOKEN, session.accessToken)
        securePreferences.putString(KEY_REFRESH_TOKEN, session.refreshToken)
        securePreferences.putLong(KEY_TOKEN_EXPIRY, session.expiresAt)
        saveUserToPrefs(session.user)
    }

    private fun clearSession() {
        securePreferences.remove(KEY_ACCESS_TOKEN)
        securePreferences.remove(KEY_REFRESH_TOKEN)
        securePreferences.remove(KEY_TOKEN_EXPIRY)
        securePreferences.remove(KEY_USER_ID)
        securePreferences.remove(KEY_USER_NAME)
        securePreferences.remove(KEY_USER_PHONE)
        securePreferences.remove(KEY_USER_CLASS)
        securePreferences.remove(KEY_USER_MEDIUM)
        securePreferences.remove(KEY_ACCESSIBILITY_MODE)
    }

    private fun saveUserToPrefs(user: User) {
        securePreferences.putString(KEY_USER_ID, user.id)
        securePreferences.putString(KEY_USER_NAME, user.name)
        user.phone?.let { securePreferences.putString(KEY_USER_PHONE, it) }
        securePreferences.putInt(KEY_USER_CLASS, user.classLevel)
        securePreferences.putString(KEY_USER_MEDIUM, user.medium)
        securePreferences.putString(KEY_ACCESSIBILITY_MODE, user.accessibilityMode.name)
    }

    private fun loadUserFromPrefs(): User? {
        val userId = securePreferences.getString(KEY_USER_ID) ?: return null
        val userName = securePreferences.getString(KEY_USER_NAME) ?: return null
        
        return User(
            id = userId,
            name = userName,
            phone = securePreferences.getString(KEY_USER_PHONE),
            classLevel = securePreferences.getInt(KEY_USER_CLASS, 1),
            medium = securePreferences.getString(KEY_USER_MEDIUM) ?: "hindi",
            role = UserRole.STUDENT,
            accessibilityMode = AccessibilityMode.fromString(
                securePreferences.getString(KEY_ACCESSIBILITY_MODE) ?: "VISUAL"
            )
        )
    }
}
