package com.hdaf.eduapp.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper class for managing app locale/language settings.
 * Provides functionality to change app language at runtime.
 */
object LocaleHelper {
    
    private const val PREF_KEY_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "en"
    
    /**
     * Get saved language code from SharedPreferences
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("edu_app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Save language code to SharedPreferences
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("edu_app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Apply locale to context
     */
    fun applyLocale(context: Context): Context {
        val languageCode = getLanguage(context)
        return setLocale(context, languageCode)
    }
    
    /**
     * Set locale for the given context
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Restart activity to apply new locale
     */
    fun restartActivity(activity: Activity) {
        activity.recreate()
    }
    
    /**
     * Get display name for language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "hi" -> "हिंदी"
            "en" -> "English"
            else -> "English"
        }
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "hi" to "हिंदी (Hindi)"
        )
    }
}
