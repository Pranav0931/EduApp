package com.hdaf.eduapp.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for secure data storage.
 */
interface SecurePreferences {
    fun getAuthToken(): String?
    fun setAuthToken(token: String)
    fun clearAuthToken()
    
    fun getRefreshToken(): String?
    fun setRefreshToken(token: String)
    fun clearRefreshToken()
    
    fun getUserId(): String?
    fun setUserId(userId: String)
    fun clearUserId()
    
    fun getApiKey(keyName: String): String?
    fun setApiKey(keyName: String, key: String)
    
    fun getString(key: String, default: String? = null): String?
    fun setString(key: String, value: String)
    
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun putBoolean(key: String, value: Boolean)
    
    fun getLong(key: String, default: Long = 0L): Long
    fun setLong(key: String, value: Long)
    fun putLong(key: String, value: Long)
    
    fun getInt(key: String, default: Int = 0): Int
    fun putInt(key: String, value: Int)
    
    fun putString(key: String, value: String)
    
    fun remove(key: String)
    fun clearAll()
}

/**
 * Implementation of SecurePreferences using EncryptedSharedPreferences.
 * 
 * Uses AES256-GCM encryption with Android Keystore for key management.
 * This provides strong security for sensitive data at rest.
 */
@Singleton
class SecurePreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurePreferences {

    companion object {
        private const val PREFS_NAME = "eduapp_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PREFIX_API = "api_key_"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create EncryptedSharedPreferences, falling back to regular")
            // Fallback to regular SharedPreferences if encryption fails
            // This should only happen on devices with broken KeyStore
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ==================== Auth Token ====================
    
    override fun getAuthToken(): String? {
        return getString(KEY_AUTH_TOKEN)
    }

    override fun setAuthToken(token: String) {
        setString(KEY_AUTH_TOKEN, token)
    }

    override fun clearAuthToken() {
        remove(KEY_AUTH_TOKEN)
    }

    // ==================== Refresh Token ====================
    
    override fun getRefreshToken(): String? {
        return getString(KEY_REFRESH_TOKEN)
    }

    override fun setRefreshToken(token: String) {
        setString(KEY_REFRESH_TOKEN, token)
    }

    override fun clearRefreshToken() {
        remove(KEY_REFRESH_TOKEN)
    }

    // ==================== User ID ====================
    
    override fun getUserId(): String? {
        return getString(KEY_USER_ID)
    }

    override fun setUserId(userId: String) {
        setString(KEY_USER_ID, userId)
    }

    override fun clearUserId() {
        remove(KEY_USER_ID)
    }

    // ==================== API Keys ====================
    
    override fun getApiKey(keyName: String): String? {
        return getString(KEY_PREFIX_API + keyName)
    }

    override fun setApiKey(keyName: String, key: String) {
        setString(KEY_PREFIX_API + keyName, key)
    }

    // ==================== Generic Operations ====================
    
    override fun getString(key: String, default: String?): String? {
        return try {
            sharedPreferences.getString(key, default)
        } catch (e: Exception) {
            Timber.e(e, "Error reading string for key: $key")
            default
        }
    }

    override fun setString(key: String, value: String) {
        try {
            sharedPreferences.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error writing string for key: $key")
        }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return try {
            sharedPreferences.getBoolean(key, default)
        } catch (e: Exception) {
            Timber.e(e, "Error reading boolean for key: $key")
            default
        }
    }

    override fun setBoolean(key: String, value: Boolean) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error writing boolean for key: $key")
        }
    }

    override fun putBoolean(key: String, value: Boolean) = setBoolean(key, value)

    override fun getLong(key: String, default: Long): Long {
        return try {
            sharedPreferences.getLong(key, default)
        } catch (e: Exception) {
            Timber.e(e, "Error reading long for key: $key")
            default
        }
    }

    override fun setLong(key: String, value: Long) {
        try {
            sharedPreferences.edit().putLong(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error writing long for key: $key")
        }
    }
    
    override fun putLong(key: String, value: Long) = setLong(key, value)
    
    override fun getInt(key: String, default: Int): Int {
        return try {
            sharedPreferences.getInt(key, default)
        } catch (e: Exception) {
            Timber.e(e, "Error reading int for key: $key")
            default
        }
    }
    
    override fun putInt(key: String, value: Int) {
        try {
            sharedPreferences.edit().putInt(key, value).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error writing int for key: $key")
        }
    }
    
    override fun putString(key: String, value: String) = setString(key, value)

    override fun remove(key: String) {
        try {
            sharedPreferences.edit().remove(key).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error removing key: $key")
        }
    }

    override fun clearAll() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            Timber.e(e, "Error clearing all preferences")
        }
    }
}
